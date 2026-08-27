import React from 'react';
import { Routes, Route } from 'react-router-dom';
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
import ProtectedRoute from './ProtectedRoute';

/**
 * Main Routing Configuration
 */
export const AppRoutes: React.FC = () => {
  return (
    <MainLayout>
      <Routes>
        <Route path="/" element={<HomePage />} />
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
