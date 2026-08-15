import React from 'react';
import { Routes, Route } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout';
import HomePage from '../pages/HomePage';

/**
 * Main Routing Configuration
 * Scalable structure for adding future phase modules (Auth, Events, Profile, AI Assistant, etc.)
 */
export const AppRoutes: React.FC = () => {
  return (
    <MainLayout>
      <Routes>
        <Route path="/" element={<HomePage />} />
        {/* Future phase routes will be added here cleanly */}
      </Routes>
    </MainLayout>
  );
};

export default AppRoutes;
