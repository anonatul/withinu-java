export function Spinner({ label }: { label?: string }) {
  return (
    <div className="flex flex-col items-center gap-3 py-10 text-ink-soft">
      <div className="h-6 w-6 animate-spin rounded-full border-2 border-hairline border-t-ink" />
      {label && <p className="text-sm">{label}</p>}
    </div>
  )
}