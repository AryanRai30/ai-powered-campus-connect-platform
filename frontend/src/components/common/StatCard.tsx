import React from 'react';
import { IconProps } from './Icons';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: React.FC<IconProps>;
  description?: string;
  trend?: {
    value: string;
    positive?: boolean;
  };
  color?: 'blue' | 'purple' | 'cyan' | 'emerald' | 'amber' | 'rose';
  onClick?: () => void;
}

const colorMap = {
  blue: {
    bg: 'bg-blue-50',
    iconBg: 'bg-blue-100/80',
    iconColor: 'text-blue-600',
    border: 'border-blue-100',
  },
  purple: {
    bg: 'bg-purple-50',
    iconBg: 'bg-purple-100/80',
    iconColor: 'text-purple-600',
    border: 'border-purple-100',
  },
  cyan: {
    bg: 'bg-cyan-50',
    iconBg: 'bg-cyan-100/80',
    iconColor: 'text-cyan-600',
    border: 'border-cyan-100',
  },
  emerald: {
    bg: 'bg-emerald-50',
    iconBg: 'bg-emerald-100/80',
    iconColor: 'text-emerald-600',
    border: 'border-emerald-100',
  },
  amber: {
    bg: 'bg-amber-50',
    iconBg: 'bg-amber-100/80',
    iconColor: 'text-amber-600',
    border: 'border-amber-100',
  },
  rose: {
    bg: 'bg-rose-50',
    iconBg: 'bg-rose-100/80',
    iconColor: 'text-rose-600',
    border: 'border-rose-100',
  },
};

export const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  icon: Icon,
  description,
  trend,
  color = 'blue',
  onClick,
}) => {
  const styles = colorMap[color] || colorMap.blue;

  return (
    <div
      onClick={onClick}
      className={`bg-white rounded-2xl p-5 border border-slate-200/80 shadow-subtle hover:shadow-card-hover hover:-translate-y-0.5 transition-all duration-200 ${
        onClick ? 'cursor-pointer' : ''
      }`}
    >
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1">
            {title}
          </p>
          <h3 className="text-2xl font-bold text-slate-900 tracking-tight">{value}</h3>
        </div>
        <div className={`p-3 rounded-xl ${styles.iconBg} ${styles.iconColor}`}>
          <Icon size={24} />
        </div>
      </div>

      {(description || trend) && (
        <div className="mt-3 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
          {description && <span className="text-slate-500 font-medium">{description}</span>}
          {trend && (
            <span
              className={`font-semibold flex items-center gap-1 ${
                trend.positive ? 'text-emerald-600' : 'text-slate-500'
              }`}
            >
              {trend.value}
            </span>
          )}
        </div>
      )}
    </div>
  );
};
