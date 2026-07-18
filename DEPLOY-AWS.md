# Deploying to AWS — single EC2 + Docker Compose

This runbook deploys the whole platform (frontend, backend, PostgreSQL, MinIO)
to **one EC2 instance** using `docker-compose.prod.yml`. It's the simplest,
cheapest option and needs only one AWS resource plus a security group.

```
                Internet
                   |  http :80
             ┌─────▼──────────────────────────────┐
             │  EC2 (Amazon Linux 2023)            │
             │  ┌──────────┐  /api  ┌───────────┐  │
             │  │  nginx   ├───────►│  backend  │  │
             │  │(frontend)│        │(SpringBoot)│ │
             │  │          │/storage└─────┬─────┘  │
             │  └────┬─────┘              │        │
             │       │            ┌───────▼─────┐  │
             │       └───────────►│    minio    │  │
             │                    ├─────────────┤  │
             │                    │  postgres   │  │
             │                    └─────────────┘  │
             │      (data on EBS named volumes)    │
             └─────────────────────────────────────┘
```

> **Cost:** roughly **US$15–30/month** (t3.small–t3.medium + 20 GB gp3 + Elastic IP).
> This stack does **not** fit the free-tier `t3.micro` (1 GB RAM).

---

## 1. Launch the EC2 instance

Console → **EC2 → Launch instance**:

| Setting | Value |
|---|---|
| Name | `msamp-prod` |
| AMI | **Amazon Linux 2023** (x86_64) |
| Instance type | **t3.medium** (4 GB) recommended · t3.small (2 GB) works with swap |
| Key pair | create/select one (you'll SSH with it) |
| Storage | **20 GB gp3** |
| Security group | see below |

**Security group** (inbound rules):

| Type | Port | Source | Why |
|---|---|---|---|
| SSH | 22 | **My IP** | admin access |
| HTTP | 80 | 0.0.0.0/0 | the app |

Do **not** expose 5432/9000/9001/8080 — those stay internal to the Docker network.

**Elastic IP (recommended):** EC2 → Elastic IPs → Allocate → Associate with the
instance, so the public IP doesn't change on reboot.

---

## 2. Connect and install Docker

```bash
ssh -i your-key.pem ec2-user@<ELASTIC_IP>

# Install Docker + Compose plugin + git
sudo dnf update -y
sudo dnf install -y docker git
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user
# Compose v2 plugin
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# Log out & back in so the docker group applies
exit
```

**Add 2 GB swap (do this if you picked t3.small — the Maven image build is memory-hungry):**

```bash
sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

---

## 3. Get the code onto the server

**Option A — git** (if the repo is on GitHub/GitLab):

```bash
ssh -i your-key.pem ec2-user@<ELASTIC_IP>
git clone <your-repo-url> msamp && cd msamp
```

**Option B — copy from your machine** (no remote repo). From your Windows PC:

```powershell
# PowerShell, from the project root
scp -i your-key.pem -r `
  backend frontend docker-compose.prod.yml .env.prod.example `
  ec2-user@<ELASTIC_IP>:/home/ec2-user/msamp/
```

---

## 4. Configure secrets

```bash
cd ~/msamp
cp .env.prod.example .env
nano .env      # fill in real values

# Generate strong secrets:
openssl rand -base64 24   # -> POSTGRES_PASSWORD
openssl rand -base64 24   # -> MINIO_ROOT_PASSWORD
openssl rand -base64 48   # -> JWT_SECRET
# Set INITIAL_ADMIN_PASSWORD to something you'll remember (or leave blank to
# have one generated and printed in the backend logs on first start).
```

---

## 5. Build and start

```bash
docker compose --env-file .env -f docker-compose.prod.yml up -d --build
```

First build takes a few minutes (Maven + npm). Check status and logs:

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend   # Ctrl-C to stop tailing
```

Wait for the backend log line `Started BackendApplication`. If you left the admin
password blank, grab the generated one from the log (search for `Generated a temporary password`).

---

## 6. Verify

```bash
curl -I http://localhost/                        # nginx serving the SPA -> 200
# API reachability: 401 means the request reached the backend and it rejected the
# dummy credentials — i.e. the /api proxy and backend are working.
curl -s -o /dev/null -w "api -> %{http_code}\n" -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" -d '{"identifier":"x","password":"x"}'
```

Then open **`http://<ELASTIC_IP>/`** in a browser and sign in with the admin
credentials from your `.env`. Register/login, create media, upload a thumbnail,
add a target site — all should work.

---

## 7. Day-2 operations

```bash
# View logs
docker compose -f docker-compose.prod.yml logs -f

# Update after pulling new code (git pull / re-scp), rebuild changed images
docker compose --env-file .env -f docker-compose.prod.yml up -d --build

# Stop / start (data is preserved on volumes)
docker compose -f docker-compose.prod.yml down       # keeps volumes
docker compose --env-file .env -f docker-compose.prod.yml up -d

# Backups
docker compose -f docker-compose.prod.yml exec -T postgres \
  pg_dump -U msamp msamp > backup-$(date +%F).sql      # database
# Also take periodic EBS snapshots of the instance volume (covers Postgres + MinIO).
```

**Auto-start on reboot:** the services use `restart: unless-stopped`, so they come
back automatically after an instance reboot (Docker is enabled at boot).

---

## 8. HTTPS (important before real users)

Right now the app runs over **plain HTTP** — fine for evaluation, but JWTs travel
unencrypted, so don't put real accounts on it yet. To add HTTPS you need a domain
name. Two easy paths once you have one (point an A record at the Elastic IP):

- **Caddy in front** — automatic Let's Encrypt certificates. Add a Caddy service
  that reverse-proxies `:443` → `frontend:80`; Caddy fetches/renews certs for you.
- **Cloudflare** (free) — proxy your domain through Cloudflare and enable "Full"
  TLS; you get HTTPS at the edge without touching the server.

Ask me and I'll wire up whichever you choose.

---

## 9. Optional hardening / upgrades (later)

- **Managed data:** move PostgreSQL to **RDS** and thumbnails to **S3** (the backend
  already uses the AWS SDK — set `S3_ENDPOINT` to the real S3 URL and drop the MinIO
  container). More durable, less to babysit; costs a bit more.
- **Systemd unit** or an AMI so redeploys are one command.
- **CloudWatch agent** for logs/metrics; **fail2ban** for SSH.
