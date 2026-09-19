import React from 'react';
import { IconProps, SparklesIcon } from './Icons';

interface EmptyStateProps {
  title: string;
  description: string;
  icon?: React.FC<IconProps>;
  action?: {
    label: string;
    onClick: () => void;
  };
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  title,
  description,
  icon: Icon = SparklesIcon,
  action,
}) => {
  return (
    <div className="bg-white border border-slate-200/80 rounded-2xl p-10 text-center max-w-lg mx-auto my-8 shadow-subtle animate-fade-in">
      <div className="w-14 h-14 bg-brand-50 text-brand-600 rounded-2xl flex items-center justify-center mx-auto mb-4 border border-brand-100">
        <Icon size={28} />
      </div>
      <h4 className="text-lg font-bold text-slate-900 mb-1">{title}</h4>
      <p className="text-sm text-slate-500 max-w-sm mx-auto mb-6">{description}</p>
      {action && (
        <button
          onClick={action.onClick}
          className="inline-flex items-center justify-center px-4 py-2.5 bg-brand-600 hover:bg-brand-700 text-white font-medium text-sm rounded-xl transition-all shadow-sm hover:shadow active:scale-95"
        >
          {action.label}
        </button>
      )}
    </div>
  );
};
