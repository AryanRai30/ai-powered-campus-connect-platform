/**
 * Navigation utility functions for role-based routing and redirection.
 */
export const getDefaultDashboardForRoles = (roles?: string[]): string => {
  const roleNames = roles || [];
  if (roleNames.some((r) => ['ADMIN', 'SUPER_ADMIN', 'CLUB_ADMIN'].includes(r))) {
    return '/admin/dashboard';
  }
  if (roleNames.includes('FACULTY')) {
    return '/faculty/dashboard';
  }
  return '/dashboard';
};

export const isPathAllowedForRoles = (pathname: string, roles?: string[]): boolean => {
  const roleNames = roles || [];
  const isAdmin = roleNames.some((r) => ['ADMIN', 'SUPER_ADMIN', 'CLUB_ADMIN'].includes(r));
  const isFaculty = roleNames.includes('FACULTY');

  if (pathname.startsWith('/admin')) {
    return isAdmin;
  }
  if (pathname.startsWith('/faculty')) {
    return isFaculty;
  }
  return true;
};

