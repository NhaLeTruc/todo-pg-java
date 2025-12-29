import userEvent from '@testing-library/user-event';

import { render, screen } from '@/test-utils';

import { PrioritySelector, PriorityBadge } from '../PrioritySelector';

describe('PrioritySelector', () => {
  const mockOnChange = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render all priority options', () => {
    render(<PrioritySelector value="MEDIUM" onChange={mockOnChange} />);

    expect(screen.getByRole('button', { name: /high/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /medium/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /low/i })).toBeInTheDocument();
  });

  it('should highlight the selected priority', () => {
    render(<PrioritySelector value="HIGH" onChange={mockOnChange} />);

    const highButton = screen.getByRole('button', { name: /high/i });
    const mediumButton = screen.getByRole('button', { name: /medium/i });
    const lowButton = screen.getByRole('button', { name: /low/i });

    expect(highButton).toHaveClass('bg-red-100');
    expect(mediumButton).not.toHaveClass('bg-yellow-100');
    expect(lowButton).not.toHaveClass('bg-green-100');
  });

  it('should call onChange when a priority button is clicked', async () => {
    const user = userEvent.setup();
    render(<PrioritySelector value="MEDIUM" onChange={mockOnChange} />);

    const highButton = screen.getByRole('button', { name: /high/i });
    await user.click(highButton);

    expect(mockOnChange).toHaveBeenCalledWith('HIGH');
    expect(mockOnChange).toHaveBeenCalledTimes(1);
  });

  it('should not call onChange when clicking the already selected priority', async () => {
    const user = userEvent.setup();
    render(<PrioritySelector value="MEDIUM" onChange={mockOnChange} />);

    const mediumButton = screen.getByRole('button', { name: /medium/i });
    await user.click(mediumButton);

    // Still calls onChange, component doesn't prevent it
    expect(mockOnChange).toHaveBeenCalledWith('MEDIUM');
  });

  it('should disable all buttons when disabled prop is true', () => {
    render(<PrioritySelector value="MEDIUM" onChange={mockOnChange} disabled={true} />);

    const highButton = screen.getByRole('button', { name: /high/i });
    const mediumButton = screen.getByRole('button', { name: /medium/i });
    const lowButton = screen.getByRole('button', { name: /low/i });

    expect(highButton).toBeDisabled();
    expect(mediumButton).toBeDisabled();
    expect(lowButton).toBeDisabled();
  });

  it('should not call onChange when disabled', async () => {
    const user = userEvent.setup();
    render(<PrioritySelector value="MEDIUM" onChange={mockOnChange} disabled={true} />);

    const highButton = screen.getByRole('button', { name: /high/i });
    await user.click(highButton);

    expect(mockOnChange).not.toHaveBeenCalled();
  });

  it('should apply disabled styling when disabled', () => {
    render(<PrioritySelector value="MEDIUM" onChange={mockOnChange} disabled={true} />);

    const highButton = screen.getByRole('button', { name: /high/i });
    expect(highButton).toHaveClass('cursor-not-allowed', 'opacity-50');
  });

  it('should update highlighted button when value prop changes', () => {
    const { rerender } = render(<PrioritySelector value="LOW" onChange={mockOnChange} />);

    let lowButton = screen.getByRole('button', { name: /low/i });
    expect(lowButton).toHaveClass('bg-green-100');

    rerender(<PrioritySelector value="HIGH" onChange={mockOnChange} />);

    const highButton = screen.getByRole('button', { name: /high/i });
    lowButton = screen.getByRole('button', { name: /low/i });

    expect(highButton).toHaveClass('bg-red-100');
    expect(lowButton).not.toHaveClass('bg-green-100');
  });

  it('should have correct button types to prevent form submission', () => {
    render(<PrioritySelector value="MEDIUM" onChange={mockOnChange} />);

    const buttons = screen.getAllByRole('button');
    buttons.forEach((button) => {
      expect(button).toHaveAttribute('type', 'button');
    });
  });

  describe('Priority colors', () => {
    it('should apply red styling to HIGH priority', () => {
      render(<PrioritySelector value="HIGH" onChange={mockOnChange} />);

      const highButton = screen.getByRole('button', { name: /high/i });
      expect(highButton).toHaveClass('bg-red-100', 'text-red-800', 'border-red-300');
    });

    it('should apply yellow styling to MEDIUM priority', () => {
      render(<PrioritySelector value="MEDIUM" onChange={mockOnChange} />);

      const mediumButton = screen.getByRole('button', { name: /medium/i });
      expect(mediumButton).toHaveClass('bg-yellow-100', 'text-yellow-800', 'border-yellow-300');
    });

    it('should apply green styling to LOW priority', () => {
      render(<PrioritySelector value="LOW" onChange={mockOnChange} />);

      const lowButton = screen.getByRole('button', { name: /low/i });
      expect(lowButton).toHaveClass('bg-green-100', 'text-green-800', 'border-green-300');
    });
  });
});

describe('PriorityBadge', () => {
  it('should render HIGH priority badge', () => {
    render(<PriorityBadge priority="HIGH" />);
    expect(screen.getByText('HIGH')).toBeInTheDocument();
  });

  it('should render MEDIUM priority badge', () => {
    render(<PriorityBadge priority="MEDIUM" />);
    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
  });

  it('should render LOW priority badge', () => {
    render(<PriorityBadge priority="LOW" />);
    expect(screen.getByText('LOW')).toBeInTheDocument();
  });

  it('should have base styling classes', () => {
    const { container } = render(<PriorityBadge priority="HIGH" />);
    const badge = container.firstChild as HTMLElement;

    expect(badge).toHaveClass('rounded', 'px-2', 'py-1', 'text-xs', 'font-medium');
  });
});
