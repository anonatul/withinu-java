import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { ADMIN_TOKEN_KEY, TOKEN_KEY } from '../lib/api'

interface AuthContextValue {
  token: string | null
  adminToken: string | null
  displayName: string
  setToken: (token: string) => void
  setAdminToken: (token: string) => void
  clearTokens: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

function displayNameFromToken(token: string | null): string {
  if (!token) return 'Anonymous'
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const id: string = payload.sub
    return `Anonymous #${id.slice(0, 4).toUpperCase()}`
  } catch {
    return 'Anonymous'
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(() =>
    localStorage.getItem(TOKEN_KEY),
  )
  const [adminToken, setAdminTokenState] = useState<string | null>(() =>
    localStorage.getItem(ADMIN_TOKEN_KEY),
  )

  const setToken = useCallback((value: string) => {
    localStorage.setItem(TOKEN_KEY, value)
    setTokenState(value)
  }, [])

  const setAdminToken = useCallback((value: string) => {
    localStorage.setItem(ADMIN_TOKEN_KEY, value)
    setAdminTokenState(value)
  }, [])

  const clearTokens = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(ADMIN_TOKEN_KEY)
    setTokenState(null)
    setAdminTokenState(null)
  }, [])

  const value = useMemo(
    () => ({
      token,
      adminToken,
      displayName: displayNameFromToken(token),
      setToken,
      setAdminToken,
      clearTokens,
    }),
    [token, adminToken, setToken, setAdminToken, clearTokens],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}