import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { login as apiLogin, logout as apiLogout, register as apiRegister } from '../api/auth'
import { setOnAuthFailure, tokenStore } from '../api/client'
import type { User } from '../api/types'

interface RegisterInput {
  email: string
  username: string
  password: string
  confirmPassword: string
}

interface AuthContextValue {
  user: User | null
  isAuthenticated: boolean
  isAdmin: boolean
  login: (identifier: string, password: string) => Promise<void>
  register: (input: RegisterInput) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => tokenStore.user)

  useEffect(() => {
    // If refresh fails anywhere in the app, drop the session.
    setOnAuthFailure(() => setUser(null))
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: !!user,
      isAdmin: user?.role === 'ADMIN',
      async login(identifier, password) {
        const tokens = await apiLogin(identifier, password)
        tokenStore.set(tokens)
        setUser(tokens.user)
      },
      async register(input) {
        const tokens = await apiRegister(input) // registration auto-authenticates
        tokenStore.set(tokens)
        setUser(tokens.user)
      },
      logout() {
        const refresh = tokenStore.refresh
        if (refresh) void apiLogout(refresh).catch(() => undefined)
        tokenStore.clear()
        setUser(null)
      },
    }),
    [user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
