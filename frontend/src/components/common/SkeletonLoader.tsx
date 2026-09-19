import React from 'react';

interface SkeletonProps {
  type?: 'card' | 'table' | 'stat' | 'list';
  count?: number;
}

export const SkeletonLoader: React.FC<SkeletonProps> = ({ type = 'card', count = 3 }) => {
  const items = Array.from({ length: count });

  if (type === 'stat') {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {items.map((_, i) => (
          <div key={i} className="bg-white rounded-2xl p-5 border border-slate-200/80 animate-pulse">
            <div className="flex justify-between items-center mb-3">
              <div className="h-4 bg-slate-200 rounded w-1/2"></div>
              <div className="w-10 h-10 bg-slate-200 rounded-xl"></div>
            </div>
            <div className="h-7 bg-slate-200 rounded w-1/3 mb-2"></div>
            <div className="h-3 bg-slate-100 rounded w-2/3"></div>
          </div>
        ))}
      </div>
    );
  }

  if (type === 'table') {
    return (
      <div className="bg-white rounded-2xl border border-slate-200/80 p-5 space-y-4 animate-pulse">
        <div className="h-8 bg-slate-200 rounded w-1/4 mb-4"></div>
        {items.map((_, i) => (
          <div key={i} className="flex items-center space-x-4">
            <div className="w-10 h-10 bg-slate-200 rounded-full"></div>
            <div className="flex-1 space-y-2">
              <div className="h-4 bg-slate-200 rounded w-3/4"></div>
              <div className="h-3 bg-slate-100 rounded w-1/2"></div>
            </div>
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {items.map((_, i) => (
        <div key={i} className="bg-white rounded-2xl p-6 border border-slate-200/80 space-y-4 animate-pulse">
          <div className="flex items-center space-x-3">
            <div className="w-12 h-12 bg-slate-200 rounded-xl"></div>
            <div className="space-y-2 flex-1">
              <div className="h-4 bg-slate-200 rounded w-2/3"></div>
              <div className="h-3 bg-slate-100 rounded w-1/3"></div>
            </div>
          </div>
          <div className="h-16 bg-slate-100 rounded-xl"></div>
          <div className="flex justify-between pt-2">
            <div className="h-4 bg-slate-200 rounded w-1/4"></div>
            <div className="h-8 bg-slate-200 rounded-xl w-1/3"></div>
          </div>
        </div>
      ))}
    </div>
  );
};
