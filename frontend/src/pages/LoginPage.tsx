import { useState } from 'react'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Alert, Box, Button, Card, CardContent, Link, Stack, TextField, Typography } from '@mui/material'
import { useAuth } from '../auth/AuthProvider'
import { apiErrorMessage } from '../api/client'
import { SIDEBAR_BG } from '../theme'

const schema = z.object({
  identifier: z.string().min(1, 'Email or username is required'),
  password: z.string().min(1, 'Password is required'),
})
type FormValues = z.infer<typeof schema>

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { identifier: '', password: '' } })

  const onSubmit = async (values: FormValues) => {
    setError(null)
    try {
      await login(values.identifier, values.password)
      navigate('/', { replace: true })
    } catch (e) {
      setError(apiErrorMessage(e, 'Invalid credentials'))
    }
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: `linear-gradient(135deg, ${SIDEBAR_BG} 0%, #1e293b 100%)`,
        p: 2,
      }}
    >
      <Card sx={{ width: 400, maxWidth: '100%' }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h5" gutterBottom>Stock Media Manager</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Sign in to your account
          </Typography>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <form onSubmit={handleSubmit(onSubmit)}>
            <Stack spacing={2.5}>
              <TextField
                label="Email or username"
                fullWidth
                autoFocus
                {...register('identifier')}
                error={!!errors.identifier}
                helperText={errors.identifier?.message}
              />
              <TextField
                label="Password"
                type="password"
                fullWidth
                {...register('password')}
                error={!!errors.password}
                helperText={errors.password?.message}
              />
              <Button type="submit" variant="contained" size="large" disabled={isSubmitting}>
                {isSubmitting ? 'Signing in…' : 'Sign in'}
              </Button>
            </Stack>
          </form>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 3, textAlign: 'center' }}>
            Don&apos;t have an account?{' '}
            <Link component={RouterLink} to="/register">Create one</Link>
          </Typography>
          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 2, textAlign: 'center' }}>
            Demo admin: admin / Admin123!
          </Typography>
        </CardContent>
      </Card>
    </Box>
  )
}
