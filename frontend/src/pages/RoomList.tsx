import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { roomsApi } from '../lib/api'
import { timeAgo } from '../lib/time'
import { UserShell } from '../components/UserShell'
import { Spinner } from '../components/Spinner'
import { ErrorState } from '../components/ErrorState'

export default function RoomList() {
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['rooms'],
    queryFn: roomsApi.list,
  })

  return (
    <UserShell>
      <div className="rise">
        <h1 className="display text-4xl text-ink">Rooms</h1>
        <p className="mt-2 text-sm text-ink-soft">
          Choose a room to start an anonymous conversation.
        </p>
      </div>

      {isLoading && <Spinner label="Loading rooms..." />}
      {error && <ErrorState error={error} onRetry={() => refetch()} />}

      {data && (
        <div className="rise mt-8 border-t border-hairline" style={{ animationDelay: '80ms' }}>
          {data.length === 0 && (
            <p className="py-12 text-center text-sm text-ink-soft">
              No active rooms right now.
            </p>
          )}
          {data.map((room, index) => (
            <Link
              key={room.id}
              to={`/rooms/${room.id}`}
              className="group flex items-center justify-between gap-4 border-b border-hairline py-5 transition-colors hover:bg-paper-deep/60"
            >
              <div className="flex min-w-0 items-baseline gap-4">
                <span className="mono text-xs text-ink-soft">
                  {String(index + 1).padStart(2, '0')}
                </span>
                <div className="min-w-0">
                  <h2 className="text-base font-medium text-ink transition-colors group-hover:text-accent">
                    {room.name}
                  </h2>
                  {room.description && (
                    <p className="mt-0.5 truncate text-sm text-ink-soft">
                      {room.description}
                    </p>
                  )}
                </div>
                <span className="hidden text-xs text-ink-soft sm:inline">/{room.slug}</span>
              </div>
              <div className="shrink-0 text-right">
                <span className="mono text-sm text-ink">{room.messageCount}</span>
                <span className="ml-2 text-xs text-ink-soft">messages</span>
                <p className="mono mt-0.5 text-xs text-ink-soft">{timeAgo(room.lastActivity)}</p>
              </div>
            </Link>
          ))}
        </div>
      )}
    </UserShell>
  )
}