import { useState } from 'react'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Alert, Box, Button, Card, CardContent, Link, Stack, TextField, Typography } from '@mui/material'
import { useAuth } from '../auth/AuthProvider'
import { apiErrorMessage } from '../api/client'
import { SIDEBAR_BG } from '../theme'

const schema = z
  .object({
    email: z.string().min(1, 'Email is required').email('Enter a valid email'),
    username: z
      .string()
      .min(3, 'At least 3 characters')
      .max(50)
      .regex(/^[A-Za-z0-9_.-]+$/, 'Letters, numbers, and . _ - only'),
    password: z.string().min(8, 'At least 8 characters'),
    confirmPassword: z.string().min(1, 'Please confirm your password'),
  })
  .refine((v) => v.password === v.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  })
type FormValues = z.infer<typeof schema>

export function RegisterPage() {
  const { register: registerUser } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: '', username: '', password: '', confirmPassword: '' },
  })

  const onSubmit = async (values: FormValues) => {
    setError(null)
    try {
      await registerUser(values)
      navigate('/', { replace: true })
    } catch (e) {
      setError(apiErrorMessage(e, 'Registration failed'))
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
      <Card sx={{ width: 420, maxWidth: '100%' }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h5" gutterBottom>Create your account</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Start organizing and tracking your stock media.
          </Typography>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <form onSubmit={handleSubmit(onSubmit)}>
            <Stack spacing={2.5}>
              <TextField label="Email" fullWidth autoFocus {...register('email')}
                error={!!errors.email} helperText={errors.email?.message} />
              <TextField label="Username" fullWidth {...register('username')}
                error={!!errors.username} helperText={errors.username?.message} />
              <TextField label="Password" type="password" fullWidth {...register('password')}
                error={!!errors.password} helperText={errors.password?.message} />
              <TextField label="Confirm password" type="password" fullWidth {...register('confirmPassword')}
                error={!!errors.confirmPassword} helperText={errors.confirmPassword?.message} />
              <Button type="submit" variant="contained" size="large" disabled={isSubmitting}>
                {isSubmitting ? 'Creating account…' : 'Create account'}
              </Button>
            </Stack>
          </form>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 3, textAlign: 'center' }}>
            Already have an account?{' '}
            <Link component={RouterLink} to="/login">Sign in</Link>
          </Typography>
        </CardContent>
      </Card>
    </Box>
  )
}
