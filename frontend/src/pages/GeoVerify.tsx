import { useCallback, useEffect, useRef, useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { authApi, err, isApiError } from '../lib/api'
import { Spinner } from '../components/Spinner'

type Status =
  | 'idle'
  | 'checking'
  | 'verified'
  | 'outside'
  | 'denied'
  | 'error'

const STATUS_TONE: Record<Status, string> = {
  idle: 'text-ink',
  checking: 'text-ink',
  verified: 'text-ok',
  outside: 'text-warn',
  denied: 'text-danger',
  error: 'text-danger',
}

export default function GeoVerify() {
  const { token, setToken } = useAuth()
  const navigate = useNavigate()
  const [status, setStatus] = useState<Status>('idle')
  const [message, setMessage] = useState('')
  const started = useRef(false)

  const requestVerification = useCallback(() => {
    setStatus('checking')
    setMessage('Checking your campus location...')

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const { latitude, longitude } = position.coords
        try {
          const response = await authApi.geoToken({ latitude, longitude })
          setToken(response.token)
          setStatus('verified')
          setMessage(`Welcome to campus. Your anonymous identity is ready.`)
          setTimeout(() => navigate('/rooms'), 800)
        } catch (error) {
          if (isApiError(error, 'OUTSIDE_CAMPUS')) {
            setStatus('outside')
            setMessage('You must be inside the campus to access WithinU')
          } else if (isApiError(error, 'RATE_LIMIT_EXCEEDED')) {
            setStatus('error')
            setMessage('Too many attempts. Please wait a minute and try again.')
          } else {
            setStatus('error')
            setMessage('Something went wrong while verifying your location.')
            err(error, 'Verification failed')
          }
        }
      },
      (geoError) => {
        if (geoError.code === geoError.PERMISSION_DENIED) {
          setStatus('denied')
          setMessage(
            'Location permission was denied. Enable location access and try again.',
          )
        } else {
          setStatus('error')
          setMessage('Unable to get your location. Please try again.')
        }
      },
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 30000 },
    )
  }, [navigate, setToken])

  useEffect(() => {
    if (started.current) return
    started.current = true
    requestVerification()
  }, [requestVerification])

  if (token) return <Navigate to="/rooms" replace />

  const titles: Record<Status, string> = {
    idle: '',
    checking: 'Checking',
    verified: "You're in",
    outside: 'Outside campus',
    denied: 'Permission denied',
    error: 'Something went wrong',
  }

  return (
    <div className="paper-bg flex min-h-screen flex-col items-center justify-center px-6">
      <div className="rise w-full max-w-sm">
        <p className="mb-8 text-center">
          <span className="label text-ink-soft">WithinU — Campus verification</span>
        </p>

        <div className="paper-card p-8">
          {status === 'checking' ? (
            <div className="text-center">
              <Spinner label={message} />
            </div>
          ) : (
            <div className="text-center">
              <h1 className={`display text-4xl ${STATUS_TONE[status]}`}>{titles[status]}</h1>
              <p className="mt-3 text-sm leading-relaxed text-ink-soft">{message}</p>
              {status !== 'verified' && (
                <button
                  onClick={requestVerification}
                  className="ink-slab slab-hover mt-7 w-full px-4 py-3 text-sm font-medium text-white"
                >
                  Try again
                </button>
              )}
            </div>
          )}
        </div>

        <p className="mt-4 text-center text-xs text-ink-soft">
          Your exact coordinates are never shown or stored — only the verified
          result inside/outside the campus boundary.
        </p>
      </div>
    </div>
  )
}