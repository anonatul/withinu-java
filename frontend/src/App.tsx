import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/AuthContext'
import Chat from './pages/Chat'
import GeoVerify from './pages/GeoVerify'
import Landing from './pages/Landing'
import RoomList from './pages/RoomList'
import AdminDashboard from './pages/admin/AdminDashboard'
import AdminLayout from './pages/admin/AdminLayout'
import AdminLogin from './pages/admin/AdminLogin'
import AdminMessages from './pages/admin/AdminMessages'
import AdminReports from './pages/admin/AdminReports'
import AdminRooms from './pages/admin/AdminRooms'

function UserRoute({ children }: { children: React.ReactNode }) {
  const { token } = useAuth()
  if (!token) return <Navigate to="/" replace />
  return <>{children}</>
}

function AdminRoute({ children }: { children: React.ReactNode }) {
  const { adminToken } = useAuth()
  if (!adminToken) return <Navigate to="/admin/login" replace />
  return <>{children}</>
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/verify" element={<GeoVerify />} />
      <Route
        path="/rooms"
        element={
          <UserRoute>
            <RoomList />
          </UserRoute>
        }
      />
      <Route
        path="/rooms/:roomId"
        element={
          <UserRoute>
            <Chat />
          </UserRoute>
        }
      />
      <Route path="/admin/login" element={<AdminLogin />} />
      <Route
        path="/admin"
        element={
          <AdminRoute>
            <AdminLayout />
          </AdminRoute>
        }
      >
        <Route index element={<AdminDashboard />} />
        <Route path="rooms" element={<AdminRooms />} />
        <Route path="messages" element={<AdminMessages />} />
        <Route path="reports" element={<AdminReports />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}