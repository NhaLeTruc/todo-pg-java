import userEvent from '@testing-library/user-event';

import { render, screen } from '@/test-utils';

import { DatePicker } from '../DatePicker';

describe('DatePicker', () => {
  const mockOnChange = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render with label when provided', () => {
    render(<DatePicker value={null} onChange={mockOnChange} label="Due Date" />);

    expect(screen.getByText('Due Date')).toBeInTheDocument();
  });

  it('should render without label when not provided', () => {
    render(<DatePicker value={null} onChange={mockOnChange} />);

    expect(screen.queryByText('Due Date')).not.toBeInTheDocument();
  });

  it('should render datetime-local input', () => {
    render(<DatePicker value={null} onChange={mockOnChange} />);

    const input = screen.getByPlaceholderText('Select date and time');
    expect(input).toBeInTheDocument();
    expect(input).toHaveAttribute('type', 'datetime-local');
  });

  it('should display formatted value when provided', () => {
    const isoDate = '2024-12-31T23:59:00.000Z';
    render(<DatePicker value={isoDate} onChange={mockOnChange} />);

    const input = screen.getByPlaceholderText('Select date and time') as HTMLInputElement;
    // Should be formatted as datetime-local (first 16 chars without seconds/timezone)
    expect(input.value).toBe('2024-12-31T23:59');
  });

  it('should display empty value when null', () => {
    render(<DatePicker value={null} onChange={mockOnChange} />);

    const input = screen.getByPlaceholderText('Select date and time') as HTMLInputElement;
    expect(input.value).toBe('');
  });

  it('should call onChange with ISO string when date is selected', async () => {
    const user = userEvent.setup();
    render(<DatePicker value={null} onChange={mockOnChange} />);

    const input = screen.getByPlaceholderText('Select date and time');
    // Simulate selecting a datetime
    await user.type(input, '2024-12-31T23:59');

    // Should convert to ISO string (with timezone)
    expect(mockOnChange).toHaveBeenCalled();
    const lastCall = mockOnChange.mock.calls[mockOnChange.mock.calls.length - 1][0];
    // Check that it's a valid ISO string format
    expect(lastCall).toMatch(/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z/);
    expect(lastCall).toContain('2024-12-31');
  });

  it('should show Clear button when value is set', () => {
    render(<DatePicker value="2024-12-31T23:59:00Z" onChange={mockOnChange} />);

    expect(screen.getByRole('button', { name: /clear/i })).toBeInTheDocument();
  });

  it('should not show Clear button when value is null', () => {
    render(<DatePicker value={null} onChange={mockOnChange} />);

    expect(screen.queryByRole('button', { name: /clear/i })).not.toBeInTheDocument();
  });

  it('should call onChange with null when Clear button is clicked', async () => {
    const user = userEvent.setup();
    render(<DatePicker value="2024-12-31T23:59:00Z" onChange={mockOnChange} />);

    const clearButton = screen.getByRole('button', { name: /clear/i });
    await user.click(clearButton);

    expect(mockOnChange).toHaveBeenCalledWith(null);
  });

  it('should disable input when disabled prop is true', () => {
    render(<DatePicker value={null} onChange={mockOnChange} disabled={true} />);

    const input = screen.getByPlaceholderText('Select date and time');
    expect(input).toBeDisabled();
  });

  it('should not show Clear button when disabled', () => {
    render(<DatePicker value="2024-12-31T23:59:00Z" onChange={mockOnChange} disabled={true} />);

    expect(screen.queryByRole('button', { name: /clear/i })).not.toBeInTheDocument();
  });

  it('should apply minDate attribute when provided', () => {
    const minDate = '2024-01-01';
    render(<DatePicker value={null} onChange={mockOnChange} minDate={minDate} />);

    const input = screen.getByPlaceholderText('Select date and time');
    expect(input).toHaveAttribute('min', minDate);
  });

  it('should not have minDate attribute when not provided', () => {
    render(<DatePicker value={null} onChange={mockOnChange} />);

    const input = screen.getByPlaceholderText('Select date and time');
    expect(input).not.toHaveAttribute('min');
  });

  it('should use custom placeholder when provided', () => {
    render(
      <DatePicker value={null} onChange={mockOnChange} placeholder="Pick a deadline" />
    );

    expect(screen.getByPlaceholderText('Pick a deadline')).toBeInTheDocument();
  });

  it('should use default placeholder when not provided', () => {
    render(<DatePicker value={null} onChange={mockOnChange} />);

    expect(screen.getByPlaceholderText('Select date and time')).toBeInTheDocument();
  });

  it('should have correct button type to prevent form submission', () => {
    render(<DatePicker value="2024-12-31T23:59:00Z" onChange={mockOnChange} />);

    const clearButton = screen.getByRole('button', { name: /clear/i });
    expect(clearButton).toHaveAttribute('type', 'button');
  });

  it('should apply disabled styles when disabled', () => {
    render(<DatePicker value={null} onChange={mockOnChange} disabled={true} />);

    const input = screen.getByPlaceholderText('Select date and time');
    expect(input).toHaveClass('disabled:cursor-not-allowed', 'disabled:bg-gray-100');
  });

  it('should handle formatForInput with null gracefully', () => {
    render(<DatePicker value={null} onChange={mockOnChange} />);

    const input = screen.getByPlaceholderText('Select date and time') as HTMLInputElement;
    expect(input.value).toBe('');
  });

  it('should handle formatForInput with valid ISO string', () => {
    const isoDate = '2024-12-31T23:59:59.999Z';
    render(<DatePicker value={isoDate} onChange={mockOnChange} />);

    const input = screen.getByPlaceholderText('Select date and time') as HTMLInputElement;
    // Should truncate to 16 characters (remove seconds and timezone)
    expect(input.value).toBe('2024-12-31T23:59');
  });

  it('should have accessible structure with label', () => {
    render(<DatePicker value={null} onChange={mockOnChange} label="Due Date" />);

    const label = screen.getByText('Due Date');
    expect(label).toHaveClass('text-sm', 'font-medium', 'text-gray-700');
  });

  it('should have focus styles on input', () => {
    render(<DatePicker value={null} onChange={mockOnChange} />);

    const input = screen.getByPlaceholderText('Select date and time');
    expect(input).toHaveClass(
      'focus:border-blue-500',
      'focus:outline-none',
      'focus:ring-2',
      'focus:ring-blue-500'
    );
  });

  it('should have focus styles on Clear button', () => {
    render(<DatePicker value="2024-12-31T23:59:00Z" onChange={mockOnChange} />);

    const clearButton = screen.getByRole('button', { name: /clear/i });
    expect(clearButton).toHaveClass('focus:outline-none', 'focus:ring-2', 'focus:ring-gray-500');
  });

  it('should handle empty string input', async () => {
    const user = userEvent.setup();
    render(<DatePicker value="2024-12-31T23:59:00Z" onChange={mockOnChange} />);

    const input = screen.getByPlaceholderText('Select date and time');
    await user.clear(input);

    // When cleared, should call onChange with null
    expect(mockOnChange).toHaveBeenCalledWith(null);
  });

  it('should update value when re-rendered with new value', () => {
    const { rerender } = render(<DatePicker value={null} onChange={mockOnChange} />);

    let input = screen.getByPlaceholderText('Select date and time') as HTMLInputElement;
    expect(input.value).toBe('');

    rerender(<DatePicker value="2024-12-31T23:59:00Z" onChange={mockOnChange} />);

    input = screen.getByPlaceholderText('Select date and time') as HTMLInputElement;
    expect(input.value).toBe('2024-12-31T23:59');
  });

  it('should show Clear button after value changes from null to date', () => {
    const { rerender } = render(<DatePicker value={null} onChange={mockOnChange} />);

    expect(screen.queryByRole('button', { name: /clear/i })).not.toBeInTheDocument();

    rerender(<DatePicker value="2024-12-31T23:59:00Z" onChange={mockOnChange} />);

    expect(screen.getByRole('button', { name: /clear/i })).toBeInTheDocument();
  });

  it('should hide Clear button after value changes from date to null', () => {
    const { rerender } = render(
      <DatePicker value="2024-12-31T23:59:00Z" onChange={mockOnChange} />
    );

    expect(screen.getByRole('button', { name: /clear/i })).toBeInTheDocument();

    rerender(<DatePicker value={null} onChange={mockOnChange} />);

    expect(screen.queryByRole('button', { name: /clear/i })).not.toBeInTheDocument();
  });
});
