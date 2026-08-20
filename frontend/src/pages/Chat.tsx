import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { isApiError, messagesApi, reportsApi, roomsApi } from '../lib/api'
import { REPORT_REASONS, type Message, type ReportReason } from '../lib/types'
import { clockTime } from '../lib/time'
import { Spinner } from '../components/Spinner'
import { ErrorState } from '../components/ErrorState'

const PAGE_SIZE = 30

function MessageBubble({
  message,
  onDelete,
  onReport,
}: {
  message: Message
  onDelete: (id: string) => void
  onReport: (id: string, reason: ReportReason) => void
}) {
  const [menuOpen, setMenuOpen] = useState(false)

  return (
    <div className={`slide-in flex ${message.mine ? 'justify-end' : 'justify-start'}`}>
      <div
        className={`relative max-w-[80%] px-4 py-2.5 text-sm ${
          message.mine ? 'rounded-lg bg-paper-deep text-ink' : 'rounded-lg border border-hairline bg-surface text-ink'
        }`}
      >
        <div className="mb-1 flex items-baseline gap-2">
          <span className={`text-[11px] font-medium ${message.mine ? 'text-ink-soft' : 'text-ink'}`}>
            {message.mine ? 'You' : message.displayName}
          </span>
          <span className="mono text-[10px] text-ink-soft">{clockTime(message.createdAt)}</span>
        </div>

        {message.deleted ? (
          <p className="italic text-ink-soft line-through decoration-1">Message deleted</p>
        ) : (
          <p className="whitespace-pre-wrap break-words leading-relaxed">{message.content}</p>
        )}

        {!message.deleted && message.mine && (
          <div className="mt-1">
            <button
              onClick={() => onDelete(message.id)}
              className="text-[11px] text-ink-soft underline decoration-dotted underline-offset-2 hover:text-danger"
            >
              Delete
            </button>
          </div>
        )}

        {!message.deleted && !message.mine && (
          <div className="relative mt-1">
            <button
              onClick={() => setMenuOpen((open) => !open)}
              className="text-[11px] text-ink-soft underline decoration-dotted underline-offset-2 hover:text-danger"
            >
              Report
            </button>
            {menuOpen && (
              <div className="paper-card absolute bottom-full left-0 z-20 mb-2 w-48 overflow-hidden rounded-lg shadow-sm">
                {(Object.keys(REPORT_REASONS) as ReportReason[]).map((reason) => (
                  <button
                    key={reason}
                    onClick={() => {
                      onReport(message.id, reason)
                      setMenuOpen(false)
                    }}
                    className="block w-full border-b border-hairline px-3 py-2 text-left text-xs text-ink last:border-b-0 hover:bg-paper-deep/60"
                  >
                    {REPORT_REASONS[reason]}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

export default function Chat() {
  const { roomId } = useParams<{ roomId: string }>()
  const queryClient = useQueryClient()
  const [draft, setDraft] = useState('')
  const [notice, setNotice] = useState<string | null>(null)
  const scrollRef = useRef<HTMLDivElement>(null)

  const roomQuery = useQuery({
    queryKey: ['room', roomId],
    queryFn: () => roomsApi.get(roomId!),
    enabled: !!roomId,
  })

  const messagesQuery = useInfiniteQuery({
    queryKey: ['messages', roomId],
    queryFn: ({ pageParam }) => messagesApi.list(roomId!, pageParam, PAGE_SIZE),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.hasNext ? lastPage.page + 1 : undefined),
    enabled: !!roomId,
  })

  const visibleMessages = useMemo(() => {
    if (!messagesQuery.data) return []
    const flat = messagesQuery.data.pages.flatMap((page) => page.content)
    return [...flat].reverse()
  }, [messagesQuery.data])

  const scrollToBottom = useCallback(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight })
  }, [])

  useEffect(() => {
    if (!messagesQuery.isFetching) {
      requestAnimationFrame(scrollToBottom)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visibleMessages.length, roomId])

  useEffect(() => {
    if (!notice) return
    const timer = setTimeout(() => setNotice(null), 4000)
    return () => clearTimeout(timer)
  }, [notice])

  const sendMutation = useMutation({
    mutationFn: (content: string) =>
      messagesApi.send({ roomId: roomId!, content }),
    onSuccess: async () => {
      setDraft('')
      await queryClient.invalidateQueries({ queryKey: ['messages', roomId] })
      await queryClient.invalidateQueries({ queryKey: ['rooms'] })
    },
    onError: (error) => {
      if (isApiError(error, 'RATE_LIMIT_EXCEEDED')) {
        setNotice('Rate limit reached — wait a minute before sending more messages.')
      } else if (isApiError(error, 'MESSAGE_TOO_LONG')) {
        setNotice('Messages must be 1000 characters or fewer.')
      } else {
        setNotice('Could not send the message. Please try again.')
      }
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => messagesApi.delete(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['messages', roomId] })
    },
  })

  const reportMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: ReportReason }) =>
      reportsApi.create(id, reason),
    onSuccess: () => setNotice('Message reported. Thanks for keeping the campus safe.'),
    onError: () => setNotice('Could not submit the report.'),
  })

  const send = () => {
    const content = draft.trim()
    if (!content || sendMutation.isPending) return
    sendMutation.mutate(content)
  }

  return (
    <div className="paper-bg flex h-screen flex-col">
      <header className="border-b border-hairline bg-paper/95 backdrop-blur">
        <div className="mx-auto flex h-14 max-w-3xl items-center justify-between px-6">
          <div className="flex items-center gap-4">
            <Link
              to="/rooms"
              className="text-ink-soft transition-colors hover:text-ink"
              aria-label="Back to rooms"
            >
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
              </svg>
            </Link>
            <div>
              <h1 className="text-sm font-medium text-ink">
                {roomQuery.data?.name ?? 'Room'}
              </h1>
              <p className="max-w-60 truncate text-xs text-ink-soft">
                {roomQuery.data?.description}
              </p>
            </div>
          </div>
          <span className="stamp stamp-ok">
            <span className="blink-dot">●</span> Verified campus
          </span>
        </div>
      </header>

      {notice && (
        <div className="mx-auto w-full max-w-3xl px-6 pt-3">
          <div className="rounded-md border border-warn/40 bg-warn/10 px-4 py-2 text-sm text-warn">
            {notice}
          </div>
        </div>
      )}

      <div
        ref={scrollRef}
        className="mx-auto flex w-full max-w-3xl flex-1 flex-col gap-3 overflow-y-auto px-6 py-5"
      >
        {messagesQuery.isLoading && <Spinner label="Loading messages..." />}
        {messagesQuery.error && (
          <ErrorState error={messagesQuery.error} onRetry={() => messagesQuery.refetch()} />
        )}

        {messagesQuery.data && messagesQuery.data.pages[0].content.length === 0 && (
          <div className="flex flex-1 flex-col items-center justify-center gap-2 py-10 text-center">
            <p className="text-base font-medium text-ink">No messages yet</p>
            <p className="text-sm text-ink-soft">Be the first to start the conversation.</p>
          </div>
        )}

        {messagesQuery.hasNextPage && (
          <button
            onClick={() => messagesQuery.fetchNextPage()}
            disabled={messagesQuery.isFetchingNextPage}
            className="label mx-auto px-4 py-1.5 text-ink-soft transition-colors hover:text-ink disabled:opacity-40"
          >
            {messagesQuery.isFetchingNextPage ? 'Loading…' : 'Load older messages'}
          </button>
        )}

        {visibleMessages.map((message) => (
          <MessageBubble
            key={message.id}
            message={message}
            onDelete={(id) => deleteMutation.mutate(id)}
            onReport={(id, reason) => reportMutation.mutate({ id, reason })}
          />
        ))}
      </div>

      <footer className="border-t border-hairline bg-paper pb-[max(1rem,env(safe-area-inset-bottom))]">
        <div className="mx-auto flex w-full max-w-3xl items-end gap-3 px-6 pt-3">
          <textarea
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter' && !event.shiftKey) {
                event.preventDefault()
                send()
              }
            }}
            rows={1}
            placeholder="Write a message…"
            maxLength={1000}
            className="field max-h-32 min-h-11 flex-1 resize-none"
          />
          <button
            onClick={send}
            disabled={!draft.trim() || sendMutation.isPending}
            className="ink-slab slab-hover inline-flex h-11 shrink-0 items-center px-5 text-sm font-medium text-white disabled:cursor-not-allowed disabled:opacity-40"
          >
            {sendMutation.isPending ? 'Sending…' : 'Send'}
          </button>
        </div>
        <p className="mx-auto w-full max-w-3xl px-6 pt-2 text-xs text-ink-soft">
          Up to 10 messages per minute · Messages are anonymous · 1000 character limit
        </p>
      </footer>
    </div>
  )
}