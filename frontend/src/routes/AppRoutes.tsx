import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import MainLayout from '../layouts/MainLayout';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import DashboardPage from '../pages/DashboardPage';
import StudentProfilePage from '../pages/StudentProfilePage';
import EventsPage from '../pages/EventsPage';
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
import ProtectedRoute from './ProtectedRoute';

const RootRedirect: React.FC = () => {
  const { isAuthenticated, user } = useAuth();
  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }
  const roleNames = user.roles || [];
  if (roleNames.includes('FACULTY')) {
    return <Navigate to="/faculty-dashboard" replace />;
  }
  if (roleNames.includes('SUPER_ADMIN') || roleNames.includes('CLUB_ADMIN')) {
    return <Navigate to="/admin/dashboard" replace />;
  }
  return <Navigate to="/dashboard" replace />;
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
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/faculty-dashboard"
          element={
            <ProtectedRoute allowedRoles={['FACULTY']}>
              <FacultyDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/faculty/dashboard"
          element={<Navigate to="/faculty-dashboard" replace />}
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
