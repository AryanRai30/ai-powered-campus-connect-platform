/**
 * Navigation utility functions for role-based routing and redirection.
 */
export const getDefaultDashboardForRoles = (roles?: string[]): string => {
  const roleNames = roles || [];
  if (roleNames.some((r) => ['ADMIN', 'SUPER_ADMIN', 'CLUB_ADMIN'].includes(r))) {
    return '/admin/dashboard';
  }
  if (roleNames.includes('FACULTY')) {
    return '/faculty-dashboard';
  }
  return '/dashboard';
};
