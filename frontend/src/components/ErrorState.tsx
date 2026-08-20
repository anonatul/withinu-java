import { ApiClientError } from '../lib/api'

export function ErrorState({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  const message =
    error instanceof ApiClientError
      ? error.message
      : 'Something went wrong. Please try again.'
  return (
    <div className="flex flex-col items-center gap-4 rounded-md border border-danger/30 bg-danger/5 px-6 py-8 text-center">
      <p className="text-sm text-danger">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="ink-slab slab-hover px-4 py-2 text-sm font-medium text-white"
        >
          Retry
        </button>
      )}
    </div>
  )
}