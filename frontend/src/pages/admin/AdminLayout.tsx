import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'

const nav = [
  { to: '/admin', label: 'Overview', end: true },
  { to: '/admin/rooms', label: 'Rooms', end: false },
  { to: '/admin/messages', label: 'Messages', end: false },
  { to: '/admin/reports', label: 'Reports', end: false },
]

export default function AdminLayout() {
  const { clearTokens } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="paper-bg flex min-h-screen">
      <aside className="flex w-56 flex-col border-r border-hairline bg-surface">
        <div className="px-6 py-6">
          <p className="text-sm font-medium tracking-tight text-ink">WithinU</p>
          <p className="label mt-1 text-ink-soft">Admin</p>
        </div>
        <nav className="flex flex-1 flex-col gap-0.5 px-3">
          {nav.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `rounded-md px-3 py-2 text-sm transition-colors ${
                  isActive
                    ? 'bg-paper-deep font-medium text-ink'
                    : 'text-ink-soft hover:bg-paper-deep/60 hover:text-ink'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="px-3 py-5">
          <button
            onClick={() => {
              clearTokens()
              navigate('/')
            }}
            className="w-full rounded-md px-3 py-2 text-left text-sm text-ink-soft transition-colors hover:bg-paper-deep/60 hover:text-ink"
          >
            Sign out
          </button>
        </div>
      </aside>
      <main className="flex-1 px-8 py-8">
        <div className="mx-auto max-w-4xl">
          <Outlet />
        </div>
      </main>
    </div>
  )
}