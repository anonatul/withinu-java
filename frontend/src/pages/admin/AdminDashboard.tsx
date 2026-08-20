import { useQuery } from '@tanstack/react-query'
import { adminApi } from '../../lib/api'
import { REPORT_REASONS, type ReportStatus } from '../../lib/types'
import { timeAgo } from '../../lib/time'
import { Spinner } from '../../components/Spinner'
import { ErrorState } from '../../components/ErrorState'

function StatCard({
  label,
  value,
  tone,
}: {
  label: string
  value: number
  tone?: 'ok' | 'warn' | 'danger' | 'ink'
}) {
  const numberColor = {
    ok: 'text-ok',
    warn: 'text-warn',
    danger: 'text-danger',
    ink: 'text-ink',
  }[tone ?? 'ink']

  return (
    <div className="paper-card rounded-lg p-4">
      <p className="label text-ink-soft">{label}</p>
      <p className={`mono mt-2 text-2xl ${numberColor}`}>{value}</p>
    </div>
  )
}

const statusStyles: Record<ReportStatus, string> = {
  PENDING: 'stamp-warn',
  RESOLVED: 'stamp-ok',
  DISMISSED: 'stamp-mute',
}

export default function AdminDashboard() {
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['admin-dashboard'],
    queryFn: adminApi.dashboard,
  })

  if (isLoading) return <Spinner label="Loading dashboard..." />
  if (error) return <ErrorState error={error} onRetry={() => refetch()} />

  return (
    <div className="rise">
      <h1 className="display text-3xl text-ink">Overview</h1>
      <p className="mt-2 text-sm text-ink-soft">
        Live moderation statistics across campus.
      </p>

      <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-3">
        <StatCard label="Rooms" value={data!.totalRooms} />
        <StatCard label="Active rooms" value={data!.activeRooms} tone="ok" />
        <StatCard label="Messages" value={data!.totalMessages} />
        <StatCard label="Active users" value={data!.activeUsers} tone="ok" />
        <StatCard label="Pending reports" value={data!.pendingReports} tone="warn" />
        <StatCard label="Deleted messages" value={data!.deletedMessages} tone="danger" />
      </div>

      <div className="mt-8">
        <div className="flex items-baseline justify-between">
          <h2 className="text-base font-medium text-ink">Recent reports</h2>
          <span className="label text-ink-soft">Last 5</span>
        </div>
        {data!.recentReports.length === 0 ? (
          <p className="paper-card mt-4 rounded-lg p-4 text-sm text-ink-soft">No reports yet.</p>
        ) : (
          <div className="mt-4 divide-y divide-hairline border-t border-hairline">
            {data!.recentReports.slice(0, 5).map((report) => (
              <div key={report.id} className="flex items-center justify-between gap-4 py-3">
                <div className="min-w-0">
                  <p className="truncate text-sm text-ink">
                    {report.contentPreview ?? 'Message deleted'}
                  </p>
                  <p className="mt-0.5 text-xs text-ink-soft">
                    {REPORT_REASONS[report.reason]} · {timeAgo(report.createdAt)}
                  </p>
                </div>
                <span className={`stamp shrink-0 ${statusStyles[report.status]}`}>
                  {report.status.toLowerCase()}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}