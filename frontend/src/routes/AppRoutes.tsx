import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getDefaultDashboardForRoles } from '../utils/navigationUtils';
import MainLayout from '../layouts/MainLayout';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import DashboardPage from '../pages/DashboardPage';
import StudentProfilePage from '../pages/StudentProfilePage';
import EventsPage from '../pages/EventsPage';
import MyEventsPage from '../pages/MyEventsPage';
import AnnouncementsPage from '../pages/AnnouncementsPage';
import ClubsPage from '../pages/ClubsPage';
import MyClubsPage from '../pages/MyClubsPage';
import ResourcesPage from '../pages/ResourcesPage';
import OpportunitiesPage from '../pages/OpportunitiesPage';
import MyOpportunitiesPage from '../pages/MyOpportunitiesPage';
import FacultyDashboardPage from '../pages/FacultyDashboardPage';
import FacultyResourcesPage from '../pages/FacultyResourcesPage';
import FacultyAnnouncementsPage from '../pages/FacultyAnnouncementsPage';
import FacultyEventsPage from '../pages/FacultyEventsPage';
import FacultyOpportunitiesPage from '../pages/FacultyOpportunitiesPage';
import FacultyClubsPage from '../pages/FacultyClubsPage';
import FacultyStudentsPage from '../pages/FacultyStudentsPage';
import AdminDashboardPage from '../pages/admin/AdminDashboardPage';
import AdminFacultyPage from '../pages/admin/AdminFacultyPage';
import AdminStudentsPage from '../pages/admin/AdminStudentsPage';
import ProtectedRoute from './ProtectedRoute';

const RootRedirect: React.FC = () => {
  const { isAuthenticated, user } = useAuth();
  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }
  return <Navigate to={getDefaultDashboardForRoles(user.roles)} replace />;
};

const StudentDashboardGuard: React.FC = () => {
  const { user } = useAuth();
  const roleNames = user?.roles || [];
  if (roleNames.some((r) => ['ADMIN', 'SUPER_ADMIN', 'CLUB_ADMIN'].includes(r))) {
    return <Navigate to="/admin/dashboard" replace />;
  }
  if (roleNames.includes('FACULTY')) {
    return <Navigate to="/faculty/dashboard" replace />;
  }
  return <DashboardPage />;
};

/**
 * Main Routing Configuration
 */
export const AppRoutes: React.FC = () => {
  return (
    <MainLayout>
      <Routes>
        <Route path="/" element={<RootRedirect />} />
        <Route path="/dev/health" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/admin/dashboard"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'SUPER_ADMIN', 'CLUB_ADMIN']}>
              <AdminDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/faculty"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'SUPER_ADMIN', 'CLUB_ADMIN']}>
              <AdminFacultyPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/students"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'SUPER_ADMIN', 'CLUB_ADMIN']}>
              <AdminStudentsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <StudentDashboardGuard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/faculty/dashboard"
          element={
            <ProtectedRoute allowedRoles={['FACULTY']}>
              <FacultyDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/faculty-dashboard"
          element={<Navigate to="/faculty/dashboard" replace />}
        />
        <Route
          path="/faculty/resources"
          element={
            <ProtectedRoute allowedRoles={['FACULTY']}>
              <FacultyResourcesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/faculty/announcements"
          element={
            <ProtectedRoute allowedRoles={['FACULTY']}>
              <FacultyAnnouncementsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/faculty/events"
          element={
            <ProtectedRoute allowedRoles={['FACULTY']}>
              <FacultyEventsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/faculty/opportunities"
          element={
            <ProtectedRoute allowedRoles={['FACULTY']}>
              <FacultyOpportunitiesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/faculty/clubs"
          element={
            <ProtectedRoute allowedRoles={['FACULTY']}>
              <FacultyClubsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/faculty/students"
          element={
            <ProtectedRoute allowedRoles={['FACULTY']}>
              <FacultyStudentsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/student-profile"
          element={
            <ProtectedRoute>
              <StudentProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/events"
          element={
            <ProtectedRoute>
              <EventsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-events"
          element={
            <ProtectedRoute>
              <MyEventsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/announcements"
          element={
            <ProtectedRoute>
              <AnnouncementsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/clubs"
          element={
            <ProtectedRoute>
              <ClubsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-clubs"
          element={
            <ProtectedRoute>
              <MyClubsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/resources"
          element={
            <ProtectedRoute>
              <ResourcesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/opportunities"
          element={
            <ProtectedRoute>
              <OpportunitiesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-opportunities"
          element={
            <ProtectedRoute>
              <MyOpportunitiesPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </MainLayout>
  );
};

export default AppRoutes;
