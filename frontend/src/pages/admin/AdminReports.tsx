import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { adminApi } from '../../lib/api'
import { REPORT_REASONS, type ReportStatus } from '../../lib/types'
import { timeAgo } from '../../lib/time'
import { Spinner } from '../../components/Spinner'
import { ErrorState } from '../../components/ErrorState'

const tabs: { value: ReportStatus; label: string }[] = [
  { value: 'PENDING', label: 'Pending' },
  { value: 'RESOLVED', label: 'Resolved' },
  { value: 'DISMISSED', label: 'Dismissed' },
]

export default function AdminReports() {
  const queryClient = useQueryClient()
  const [status, setStatus] = useState<ReportStatus>('PENDING')
  const [page, setPage] = useState(0)

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['admin-reports', status, page],
    queryFn: () => adminApi.reports.list(status, page),
  })

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['admin-reports'] })
    queryClient.invalidateQueries({ queryKey: ['admin-dashboard'] })
  }

  const resolveMutation = useMutation({
    mutationFn: ({ id, next }: { id: string; next: ReportStatus }) =>
      adminApi.reports.resolve(id, next),
    onSuccess: invalidate,
  })

  const deleteMessageMutation = useMutation({
    mutationFn: (messageId: string) => adminApi.messages.delete(messageId),
    onSuccess: invalidate,
  })

  if (isLoading) return <Spinner label="Loading reports..." />
  if (error) return <ErrorState error={error} onRetry={() => refetch()} />

  return (
    <div className="rise">
      <h1 className="display text-3xl text-ink">Reports</h1>
      <p className="mt-2 text-sm text-ink-soft">
        Review reported messages and take action.
      </p>

      <div className="mt-5 flex gap-1 border-b border-hairline">
        {tabs.map((tab) => (
          <button
            key={tab.value}
            onClick={() => {
              setStatus(tab.value)
              setPage(0)
            }}
            className={`-mb-px border-b-2 px-4 py-2 text-sm transition-colors ${
              status === tab.value
                ? 'border-ink font-medium text-ink'
                : 'border-transparent text-ink-soft hover:text-ink'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="mt-4 divide-y divide-hairline border-t border-hairline">
        {data!.content.length === 0 && (
          <p className="py-10 text-center text-sm text-ink-soft">
            No {status.toLowerCase()} reports.
          </p>
        )}
        {data!.content.map((report) => (
          <div key={report.id} className="py-4">
            <div className="flex items-center justify-between gap-3">
              <div className="min-w-0">
                <p className="truncate text-sm text-ink">
                  {report.contentPreview ?? 'Message deleted'}
                </p>
                <p className="mt-0.5 text-xs text-ink-soft">
                  {REPORT_REASONS[report.reason]} · {timeAgo(report.createdAt)}
                </p>
              </div>
              {status === 'PENDING' && (
                <div className="flex shrink-0 gap-2">
                  <button
                    onClick={() => deleteMessageMutation.mutate(report.messageId)}
                    className="accent-slab slab-hover rounded-md px-3 py-1.5 text-xs font-medium text-white"
                  >
                    Delete message
                  </button>
                  <button
                    onClick={() => resolveMutation.mutate({ id: report.id, next: 'RESOLVED' })}
                    className="rounded-md px-3 py-1.5 text-xs text-ok transition-colors hover:bg-ok/5"
                  >
                    Resolve
                  </button>
                  <button
                    onClick={() => resolveMutation.mutate({ id: report.id, next: 'DISMISSED' })}
                    className="rounded-md px-3 py-1.5 text-xs text-ink-soft transition-colors hover:bg-paper-deep/60 hover:text-ink"
                  >
                    Dismiss
                  </button>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      <div className="mt-4 flex items-center justify-between text-sm">
        <p className="mono text-xs text-ink-soft">
          Page {page + 1} of {Math.max(data!.totalPages, 1)} · {data!.totalElements} reports
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