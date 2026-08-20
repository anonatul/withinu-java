import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { adminApi } from '../../lib/api'
import { Spinner } from '../../components/Spinner'
import { ErrorState } from '../../components/ErrorState'

interface RoomForm {
  name: string
  slug: string
  description: string
  active: boolean
}

const emptyForm: RoomForm = { name: '', slug: '', description: '', active: true }

export default function AdminRooms() {
  const queryClient = useQueryClient()
  const [editingId, setEditingId] = useState<string | null>(null)
  const [form, setForm] = useState<RoomForm>(emptyForm)
  const [error, setError] = useState<string | null>(null)

  const { data, isLoading, error: queryError, refetch } = useQuery({
    queryKey: ['admin-rooms'],
    queryFn: adminApi.rooms.list,
  })

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['admin-rooms'] })
    queryClient.invalidateQueries({ queryKey: ['admin-dashboard'] })
  }

  const saveMutation = useMutation({
    mutationFn: () =>
      editingId
        ? adminApi.rooms.update(editingId, form)
        : adminApi.rooms.create(form),
    onSuccess: () => {
      invalidate()
      setForm(emptyForm)
      setEditingId(null)
    },
    onError: (e: unknown) =>
      setError(e instanceof Error ? e.message : 'Could not save the room'),
  })

  const deactivateMutation = useMutation({
    mutationFn: (id: string) => adminApi.rooms.deactivate(id),
    onSuccess: invalidate,
  })

  const submit = (event: FormEvent) => {
    event.preventDefault()
    setError(null)
    saveMutation.mutate()
  }

  const startEdit = (id: string) => {
    const room = data?.find((r) => r.id === id)
    if (!room) return
    setEditingId(id)
    setForm({
      name: room.name,
      slug: room.slug,
      description: room.description ?? '',
      active: true,
    })
  }

  return (
    <div className="rise">
      <h1 className="display text-3xl text-ink">Rooms</h1>
      <p className="mt-2 text-sm text-ink-soft">
        Create, update, and deactivate discussion rooms.
      </p>

      <form onSubmit={submit} className="paper-card mt-6 rounded-lg p-5">
        <h2 className="text-base font-medium text-ink">{editingId ? 'Edit room' : 'New room'}</h2>
        <div className="mt-4 grid gap-4 sm:grid-cols-2">
          <label className="block">
            <span className="label text-ink-soft">Name</span>
            <input
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
              className="field mt-1"
            />
          </label>
          <label className="block">
            <span className="label text-ink-soft">Slug (optional)</span>
            <input
              value={form.slug}
              onChange={(e) => setForm({ ...form, slug: e.target.value })}
              placeholder="auto-generated"
              className="field mt-1"
            />
          </label>
        </div>
        <label className="mt-4 block">
          <span className="label text-ink-soft">Description</span>
          <textarea
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            rows={2}
            className="field mt-1"
          />
        </label>
        <div className="mt-4 flex items-center justify-between">
          <label className="flex cursor-pointer items-center gap-2 text-sm text-ink">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
              className="h-4 w-4 rounded-sm border-hairline text-ink focus:ring-ink"
            />
            Active
          </label>
          <div className="flex gap-2">
            {editingId && (
              <button
                type="button"
                onClick={() => {
                  setEditingId(null)
                  setForm(emptyForm)
                }}
                className="rounded-md px-3 py-2 text-sm text-ink-soft transition-colors hover:bg-paper-deep/60 hover:text-ink"
              >
                Cancel
              </button>
            )}
            <button
              type="submit"
              disabled={saveMutation.isPending}
              className="ink-slab slab-hover rounded-md px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
            >
              {saveMutation.isPending ? 'Saving…' : editingId ? 'Save changes' : 'Create room'}
            </button>
          </div>
        </div>
        {error && <p className="mt-2 text-xs text-danger">{error}</p>}
      </form>

      {isLoading && <Spinner label="Loading rooms..." />}
      {queryError && <ErrorState error={queryError} onRetry={() => refetch()} />}

      {data && (
        <div className="mt-6 divide-y divide-hairline border-t border-hairline">
          {data.map((room) => (
            <div key={room.id} className="flex items-center justify-between gap-3 py-4">
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <p className="text-sm font-medium text-ink">{room.name}</p>
                  <span className={`stamp ${room.active ? 'stamp-ok' : 'stamp-mute'}`}>
                    {room.active ? 'Active' : 'Inactive'}
                  </span>
                </div>
                <p className="mt-0.5 truncate text-xs text-ink-soft">
                  /{room.slug}
                  {room.description ? ` · ${room.description}` : ''}
                </p>
              </div>
              <div className="flex shrink-0 gap-2">
                <button
                  onClick={() => startEdit(room.id)}
                  className="rounded-md px-3 py-1.5 text-xs text-ink-soft transition-colors hover:bg-paper-deep/60 hover:text-ink"
                >
                  Edit
                </button>
                {room.active && (
                  <button
                    onClick={() => deactivateMutation.mutate(room.id)}
                    className="rounded-md px-3 py-1.5 text-xs text-danger transition-colors hover:bg-danger/5"
                  >
                    Deactivate
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}