import { Link, Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const facts = [
  { label: 'Presence', value: 'Verified on campus' },
  { label: 'Identity', value: 'Anonymous' },
  { label: 'Rooms', value: 'Moderated' },
]

export default function Landing() {
  const { token } = useAuth()
  if (token) return <Navigate to="/rooms" replace />

  return (
    <div className="paper-bg flex min-h-screen flex-col">
      <header className="mx-auto flex w-full max-w-5xl items-center justify-between px-6 py-8">
        <span className="rise text-sm font-medium tracking-tight text-ink" style={{ animationDelay: '0ms' }}>
          WithinU
        </span>
        <Link
          to="/admin/login"
          className="label rise text-ink-soft transition-colors hover:text-ink"
          style={{ animationDelay: '40ms' }}
        >
          Admin
        </Link>
      </header>

      <main className="mx-auto grid w-full max-w-5xl flex-1 items-center gap-16 px-6 pb-24 md:grid-cols-[1.4fr_0.6fr]">
        <div>
          <h1
            className="rise display text-5xl text-ink sm:text-6xl md:text-7xl"
            style={{ animationDelay: '80ms' }}
          >
            Anonymous campus conversations.
          </h1>
          <p
            className="rise mt-6 max-w-md text-base leading-relaxed text-ink-soft"
            style={{ animationDelay: '160ms' }}
          >
            Your location is verified. Your name never is. No emails, no phone
            numbers — an anonymous identity that only exists while you're on
            campus.
          </p>
          <Link
            to="/verify"
            className="rise ink-slab slab-hover mt-10 inline-flex items-center gap-3 px-6 py-3 text-sm font-medium text-white"
            style={{ animationDelay: '240ms' }}
          >
            Enter Campus
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M13 5l7 7-7 7M5 12h14" />
            </svg>
          </Link>
        </div>

        <div className="rise border-t border-hairline" style={{ animationDelay: '200ms' }}>
          {facts.map((fact) => (
            <div
              key={fact.label}
              className="flex items-baseline justify-between border-b border-hairline py-4"
            >
              <span className="label text-ink-soft">{fact.label}</span>
              <span className="text-sm text-ink">{fact.value}</span>
            </div>
          ))}
          <p className="pt-4 text-xs text-ink-soft">
            You'll be asked to share your location once, for verification.
          </p>
        </div>
      </main>

      <footer className="border-t border-hairline">
        <div className="mx-auto flex w-full max-w-5xl items-center justify-between px-6 py-5">
          <span className="label text-ink-soft">WithinU — Anonymous campus conversations</span>
          <span className="label text-ink-soft">2026</span>
        </div>
      </footer>
    </div>
  )
}