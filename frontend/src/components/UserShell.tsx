import type { ReactNode } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function UserShell({ children }: { children: ReactNode }) {
  const { displayName, clearTokens } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="paper-bg flex min-h-screen flex-col">
      <header className="sticky top-0 z-10 border-b border-hairline bg-paper/95 backdrop-blur">
        <div className="mx-auto flex h-14 max-w-3xl items-center justify-between px-6">
          <Link to="/rooms" className="text-sm font-medium tracking-tight text-ink">
            WithinU
          </Link>
          <div className="flex items-center gap-5">
            <span className="max-w-44 truncate text-sm text-ink-soft" title={displayName}>
              {displayName}
            </span>
            <button
              onClick={() => {
                clearTokens()
                navigate('/')
              }}
              className="label text-ink-soft transition-colors hover:text-ink"
            >
              Sign out
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto w-full max-w-3xl flex-1 px-6 py-10">{children}</main>
    </div>
  )
}