import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { adminApi } from '../../lib/api'
import { clockTime } from '../../lib/time'
import { Spinner } from '../../components/Spinner'
import { ErrorState } from '../../components/ErrorState'

export default function AdminMessages() {
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['admin-messages', page],
    queryFn: () => adminApi.messages.list(page),
  })

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['admin-messages'] })
    queryClient.invalidateQueries({ queryKey: ['admin-dashboard'] })
  }

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminApi.messages.delete(id),
    onSuccess: invalidate,
  })

  if (isLoading) return <Spinner label="Loading messages..." />
  if (error) return <ErrorState error={error} onRetry={() => refetch()} />

  return (
    <div className="rise">
      <h1 className="display text-3xl text-ink">Messages</h1>
      <p className="mt-2 text-sm text-ink-soft">
        Browse and moderate all campus messages.
      </p>

      <div className="mt-6 divide-y divide-hairline border-t border-hairline">
        {data!.content.length === 0 && (
          <p className="py-8 text-center text-sm text-ink-soft">No messages found.</p>
        )}
        {data!.content.map((message) => (
          <div key={message.id} className="flex items-center justify-between gap-3 py-4">
            <div className="min-w-0">
              <div className="flex items-center gap-3">
                <p className="text-xs font-medium text-ink">{message.displayName}</p>
                <p className="mono text-xs text-ink-soft">{clockTime(message.createdAt)}</p>
                {message.deleted && <span className="stamp stamp-danger">Deleted</span>}
              </div>
              <p className="mt-1 truncate text-sm text-ink">
                {message.deleted ? (
                  <span className="italic text-ink-soft line-through decoration-1">
                    Message deleted
                  </span>
                ) : (
                  message.content
                )}
              </p>
            </div>
            {!message.deleted && (
              <button
                onClick={() => deleteMutation.mutate(message.id)}
                className="shrink-0 rounded-md px-3 py-1.5 text-xs text-danger transition-colors hover:bg-danger/5"
              >
                Delete
              </button>
            )}
          </div>
        ))}
      </div>

      <div className="mt-4 flex items-center justify-between text-sm">
        <p className="mono text-xs text-ink-soft">
          Page {page + 1} of {Math.max(data!.totalPages, 1)} · {data!.totalElements} messages
        </p>
        <div className="flex gap-2">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className="rounded-md border border-hairline bg-surface px-3 py-1.5 text-xs text-ink-soft transition-colors hover:text-ink disabled:opacity-40"
          >
            Previous
          </button>
          <button
            onClick={() => setPage((p) => p + 1)}
            disabled={!data!.hasNext}
            className="rounded-md border border-hairline bg-surface px-3 py-1.5 text-xs text-ink-soft transition-colors hover:text-ink disabled:opacity-40"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  )
}