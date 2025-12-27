export const formatDateForDisplay = (dateString: string | null): string => {
  if (!dateString) return 'No due date';

  try {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    }).format(date);
  } catch {
    return 'Invalid date';
  }
};

export const isDateOverdue = (dateString: string | null): boolean => {
  if (!dateString) return false;
  try {
    return new Date(dateString) < new Date();
  } catch {
    return false;
  }
};
