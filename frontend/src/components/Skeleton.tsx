import './Skeleton.css';

interface SkeletonProps {
  width?: string;
  height?: string;
  variant?: 'text' | 'circular' | 'rectangular';
  animation?: 'pulse' | 'wave' | 'none';
  className?: string;
}

/**
 * Skeleton component for loading states.
 * Provides visual placeholders while content is loading.
 */
export function Skeleton({
  width = '100%',
  height = '1rem',
  variant = 'text',
  animation = 'pulse',
  className = '',
}: SkeletonProps) {
  const classes = `skeleton skeleton-${variant} skeleton-${animation} ${className}`;

  return (
    <div
      className={classes}
      style={{
        width,
        height,
      }}
    />
  );
}

interface SkeletonTaskCardProps {
  count?: number;
}

/**
 * Skeleton for task card loading state.
 */
export function SkeletonTaskCard({ count = 1 }: SkeletonTaskCardProps) {
  return (
    <>
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className="skeleton-task-card">
          <div className="skeleton-task-card-header">
            <Skeleton width="60%" height="1.5rem" />
            <Skeleton variant="circular" width="2rem" height="2rem" />
          </div>
          <div className="skeleton-task-card-body">
            <Skeleton width="100%" height="1rem" />
            <Skeleton width="80%" height="1rem" />
          </div>
          <div className="skeleton-task-card-footer">
            <Skeleton width="30%" height="0.875rem" />
            <Skeleton width="30%" height="0.875rem" />
          </div>
        </div>
      ))}
    </>
  );
}

interface SkeletonTableProps {
  rows?: number;
  columns?: number;
}

/**
 * Skeleton for table loading state.
 */
export function SkeletonTable({ rows = 5, columns = 4 }: SkeletonTableProps) {
  return (
    <div className="skeleton-table">
      <div className="skeleton-table-header">
        {Array.from({ length: columns }).map((_, index) => (
          <div key={index} className="skeleton-table-header-cell">
            <Skeleton width="80%" height="1rem" />
          </div>
        ))}
      </div>
      <div className="skeleton-table-body">
        {Array.from({ length: rows }).map((_, rowIndex) => (
          <div key={rowIndex} className="skeleton-table-row">
            {Array.from({ length: columns }).map((_, colIndex) => (
              <div key={colIndex} className="skeleton-table-cell">
                <Skeleton width="90%" height="1rem" />
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

/**
 * Skeleton for notification item loading state.
 */
export function SkeletonNotification({ count = 3 }: { count?: number }) {
  return (
    <>
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className="skeleton-notification">
          <div className="skeleton-notification-icon">
            <Skeleton variant="circular" width="2.5rem" height="2.5rem" />
          </div>
          <div className="skeleton-notification-content">
            <Skeleton width="70%" height="1rem" />
            <Skeleton width="50%" height="0.875rem" />
          </div>
          <div className="skeleton-notification-actions">
            <Skeleton variant="circular" width="1.5rem" height="1.5rem" />
          </div>
        </div>
      ))}
    </>
  );
}

/**
 * Skeleton for form loading state.
 */
export function SkeletonForm({ fields = 4 }: { fields?: number }) {
  return (
    <div className="skeleton-form">
      {Array.from({ length: fields }).map((_, index) => (
        <div key={index} className="skeleton-form-field">
          <Skeleton width="30%" height="1rem" />
          <Skeleton width="100%" height="2.5rem" variant="rectangular" />
        </div>
      ))}
      <div className="skeleton-form-actions">
        <Skeleton width="6rem" height="2.5rem" variant="rectangular" />
        <Skeleton width="6rem" height="2.5rem" variant="rectangular" />
      </div>
    </div>
  );
}

/**
 * Skeleton for list loading state.
 */
export function SkeletonList({ items = 5 }: { items?: number }) {
  return (
    <div className="skeleton-list">
      {Array.from({ length: items }).map((_, index) => (
        <div key={index} className="skeleton-list-item">
          <Skeleton variant="circular" width="2.5rem" height="2.5rem" />
          <div className="skeleton-list-item-content">
            <Skeleton width="60%" height="1rem" />
            <Skeleton width="40%" height="0.875rem" />
          </div>
        </div>
      ))}
    </div>
  );
}
