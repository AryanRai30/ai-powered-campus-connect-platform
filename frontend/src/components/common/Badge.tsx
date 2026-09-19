import React from 'react';

export type BadgeVariant =
  | 'published'
  | 'draft'
  | 'active'
  | 'inactive'
  | 'approved'
  | 'pending'
  | 'rejected'
  | 'info'
  | 'warning'
  | 'danger'
  | 'secondary';

interface BadgeProps {
  children: React.ReactNode;
  variant?: BadgeVariant;
  size?: 'sm' | 'md';
  className?: string;
}

const variantStyles: Record<BadgeVariant, string> = {
  published: 'bg-emerald-50 text-emerald-700 border-emerald-200/80',
  active: 'bg-emerald-50 text-emerald-700 border-emerald-200/80',
  approved: 'bg-emerald-50 text-emerald-700 border-emerald-200/80',

  draft: 'bg-amber-50 text-amber-700 border-amber-200/80',
  pending: 'bg-amber-50 text-amber-700 border-amber-200/80',
  warning: 'bg-amber-50 text-amber-700 border-amber-200/80',

  inactive: 'bg-slate-100 text-slate-600 border-slate-200',
  secondary: 'bg-purple-50 text-purple-700 border-purple-200/80',

  rejected: 'bg-rose-50 text-rose-700 border-rose-200/80',
  danger: 'bg-rose-50 text-rose-700 border-rose-200/80',

  info: 'bg-blue-50 text-blue-700 border-blue-200/80',
};

export const Badge: React.FC<BadgeProps> = ({
  children,
  variant = 'info',
  size = 'md',
  className = '',
}) => {
  const sizeStyle = size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-1 text-xs font-semibold';

  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full border ${variantStyles[variant]} ${sizeStyle} ${className}`}
    >
      <span className="w-1.5 h-1.5 rounded-full bg-current opacity-75" />
      {children}
    </span>
  );
};
