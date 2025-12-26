import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

import { Priority } from '@/types/task';

import { TaskForm } from '../TaskForm';

describe('TaskForm', () => {
  const mockOnSubmit = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render form with input, priority selector, and submit button', () => {
    render(<TaskForm onSubmit={mockOnSubmit} />);

    expect(screen.getByPlaceholderText('What needs to be done?')).toBeInTheDocument();
    expect(screen.getByRole('combobox')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /add task/i })).toBeInTheDocument();
  });

  it('should have MEDIUM as default priority', () => {
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const select = screen.getByRole('combobox') as HTMLSelectElement;
    expect(select.value).toBe('MEDIUM');
  });

  it('should allow user to type task description', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('What needs to be done?') as HTMLInputElement;
    await user.type(input, 'Buy groceries');

    expect(input.value).toBe('Buy groceries');
  });

  it('should allow user to change priority', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const select = screen.getByRole('combobox') as HTMLSelectElement;
    await user.selectOptions(select, 'HIGH');

    expect(select.value).toBe('HIGH');
  });

  it('should disable submit button when description is empty', () => {
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const submitButton = screen.getByRole('button', { name: /add task/i });
    expect(submitButton).toBeDisabled();
  });

  it('should enable submit button when description has content', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('What needs to be done?');
    const submitButton = screen.getByRole('button', { name: /add task/i });

    await user.type(input, 'Buy milk');

    expect(submitButton).toBeEnabled();
  });

  it('should show validation error when submitting empty description', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('What needs to be done?');
    const submitButton = screen.getByRole('button', { name: /add task/i });

    await user.type(input, ' ');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/task description cannot be empty/i)).toBeInTheDocument();
    });

    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('should show validation error on blur if description is invalid', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('What needs to be done?');

    await user.type(input, '   ');
    fireEvent.blur(input);

    await waitFor(() => {
      expect(screen.getByText(/task description cannot be empty/i)).toBeInTheDocument();
    });
  });

  it('should submit form with valid data', async () => {
    const user = userEvent.setup();
    mockOnSubmit.mockResolvedValue(undefined);

    render(<TaskForm onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('What needs to be done?');
    const select = screen.getByRole('combobox');
    const submitButton = screen.getByRole('button', { name: /add task/i });

    await user.type(input, 'Buy groceries');
    await user.selectOptions(select, 'HIGH');
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        description: 'Buy groceries',
        priority: 'HIGH' as Priority,
      });
    });
  });

  it('should trim whitespace from description before submitting', async () => {
    const user = userEvent.setup();
    mockOnSubmit.mockResolvedValue(undefined);

    render(<TaskForm onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('What needs to be done?');
    await user.type(input, '  Buy groceries  ');
    await user.click(screen.getByRole('button', { name: /add task/i }));

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        description: 'Buy groceries',
        priority: 'MEDIUM',
      });
    });
  });

  it('should reset form after successful submission', async () => {
    const user = userEvent.setup();
    mockOnSubmit.mockResolvedValue(undefined);

    render(<TaskForm onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('What needs to be done?') as HTMLInputElement;
    const select = screen.getByRole('combobox') as HTMLSelectElement;

    await user.type(input, 'Buy groceries');
    await user.selectOptions(select, 'HIGH');
    await user.click(screen.getByRole('button', { name: /add task/i }));

    await waitFor(() => {
      expect(input.value).toBe('');
      expect(select.value).toBe('MEDIUM');
    });
  });

  it('should disable form while submitting', () => {
    render(<TaskForm onSubmit={mockOnSubmit} isLoading={true} />);

    const input = screen.getByPlaceholderText('What needs to be done?');
    const select = screen.getByRole('combobox');
    const submitButton = screen.getByRole('button', { name: /add task/i });

    expect(input).toBeDisabled();
    expect(select).toBeDisabled();
    expect(submitButton).toBeDisabled();
  });

  it('should enforce maxLength of 5000 characters on input', () => {
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('What needs to be done?') as HTMLInputElement;

    // The maxLength attribute prevents typing more than 5000 characters
    expect(input.maxLength).toBe(5000);
  });

  it('should handle form submission with Enter key', async () => {
    const user = userEvent.setup();
    mockOnSubmit.mockResolvedValue(undefined);

    render(<TaskForm onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('What needs to be done?');
    await user.type(input, 'Buy groceries{Enter}');

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        description: 'Buy groceries',
        priority: 'MEDIUM',
      });
    });
  });
});
