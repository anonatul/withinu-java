import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import { adminApi } from '../../lib/api'

export default function AdminLogin() {
  const { adminToken, setAdminToken } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (adminToken) return <Navigate to="/admin" replace />

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const response = await adminApi.login({ username, password })
      setAdminToken(response.token)
      navigate('/admin')
    } catch {
      setError('Invalid username or password')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="paper-bg flex min-h-screen flex-col items-center justify-center px-6">
      <div className="rise w-full max-w-sm">
        <div className="mb-8 text-center">
          <p className="text-sm font-medium tracking-tight text-ink">WithinU</p>
          <p className="label mt-1 text-ink-soft">Admin sign in</p>
        </div>
        <form onSubmit={submit} className="paper-card rounded-lg p-6">
          <label className="block">
            <span className="label text-ink-soft">Username</span>
            <input
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
              required
              className="field mt-1"
            />
          </label>
          <label className="mt-4 block">
            <span className="label text-ink-soft">Password</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
              className="field mt-1"
            />
          </label>
          {error && (
            <p className="mt-4 rounded-md border border-danger/30 bg-danger/5 px-3 py-2 text-xs text-danger">
              {error}
            </p>
          )}
          <button
            type="submit"
            disabled={submitting}
            className="ink-slab slab-hover mt-6 w-full px-4 py-2.5 text-sm font-medium text-white disabled:opacity-50"
          >
            {submitting ? 'Signing in…' : 'Sign in'}
          </button>
        </form>
        <p className="mt-4 text-center">
          <Link to="/" className="label text-ink-soft transition-colors hover:text-ink">
            ← Back to WithinU
          </Link>
        </p>
      </div>
    </div>
  )
}