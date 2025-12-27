import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

import { Priority, Task } from '@/types/task';

import { TaskItem } from '../TaskItem';

describe('TaskItem', () => {
  const mockOnToggleComplete = jest.fn();
  const mockOnDelete = jest.fn();
  const mockOnEdit = jest.fn();

  const baseTask: Task = {
    id: 1,
    description: 'Test task',
    priority: Priority.MEDIUM,
    isCompleted: false,
    isOverdue: false,
    createdAt: '2024-01-01T10:00:00Z',
    updatedAt: '2024-01-01T10:00:00Z',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render task description', () => {
    render(
      <TaskItem
        task={baseTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    expect(screen.getByText('Test task')).toBeInTheDocument();
  });

  it('should display priority badge', () => {
    render(
      <TaskItem
        task={baseTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
  });

  it('should show incomplete checkbox for incomplete tasks', () => {
    render(
      <TaskItem
        task={baseTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    const checkbox = screen.getByRole('button', { name: /mark complete/i });
    expect(checkbox).toBeInTheDocument();
  });

  it('should show complete checkbox for completed tasks', () => {
    const completedTask: Task = { ...baseTask, isCompleted: true };

    render(
      <TaskItem
        task={completedTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    const checkbox = screen.getByRole('button', { name: /mark incomplete/i });
    expect(checkbox).toBeInTheDocument();
  });

  it('should apply strikethrough to completed tasks', () => {
    const completedTask: Task = { ...baseTask, isCompleted: true };

    render(
      <TaskItem
        task={completedTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    const description = screen.getByText('Test task');
    expect(description).toHaveClass('line-through');
  });

  it('should call onToggleComplete when checkbox is clicked', async () => {
    const user = userEvent.setup();

    render(
      <TaskItem
        task={baseTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    const checkbox = screen.getByRole('button', { name: /mark complete/i });
    await user.click(checkbox);

    expect(mockOnToggleComplete).toHaveBeenCalledWith(1, true);
  });

  it('should call onToggleComplete with false when uncompleting a completed task', async () => {
    const user = userEvent.setup();
    const completedTask: Task = { ...baseTask, isCompleted: true };

    render(
      <TaskItem
        task={completedTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    const checkbox = screen.getByRole('button', { name: /mark incomplete/i });
    await user.click(checkbox);

    expect(mockOnToggleComplete).toHaveBeenCalledWith(1, false);
  });

  it('should call onDelete when delete button is clicked', async () => {
    const user = userEvent.setup();

    render(
      <TaskItem
        task={baseTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    const deleteButton = screen.getByRole('button', { name: /delete task/i });
    await user.click(deleteButton);

    expect(mockOnDelete).toHaveBeenCalledWith(1);
  });

  it('should call onEdit when edit button is clicked', async () => {
    const user = userEvent.setup();

    render(
      <TaskItem
        task={baseTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    const editButton = screen.getByRole('button', { name: /edit task/i });
    await user.click(editButton);

    expect(mockOnEdit).toHaveBeenCalledWith(1);
  });

  it('should display due date when present', () => {
    const taskWithDueDate: Task = { ...baseTask, dueDate: '2024-01-15T10:00:00Z' };

    render(
      <TaskItem
        task={taskWithDueDate}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    expect(screen.getByText(/due:/i)).toBeInTheDocument();
    expect(screen.getByText(/jan 15, 2024/i)).toBeInTheDocument();
  });

  it('should highlight overdue tasks in red', () => {
    const overdueTask: Task = {
      ...baseTask,
      dueDate: '2024-01-15T10:00:00Z',
      isOverdue: true,
    };

    render(
      <TaskItem
        task={overdueTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    const dueText = screen.getByText(/due:/i);
    expect(dueText).toHaveClass('text-red-600');
  });

  it('should not highlight overdue completed tasks', () => {
    const completedOverdueTask: Task = {
      ...baseTask,
      dueDate: '2024-01-15T10:00:00Z',
      isCompleted: true,
      isOverdue: true,
    };

    render(
      <TaskItem
        task={completedOverdueTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    const dueText = screen.getByText(/due:/i);
    expect(dueText).not.toHaveClass('text-red-600');
  });

  it('should display category name when present', () => {
    const taskWithCategory: Task = { ...baseTask, categoryName: 'Work' };

    render(
      <TaskItem
        task={taskWithCategory}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    expect(screen.getByText('Work')).toBeInTheDocument();
  });

  it('should display creation date', () => {
    render(
      <TaskItem
        task={baseTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    expect(screen.getByText(/created jan 1, 2024/i)).toBeInTheDocument();
  });

  it('should apply different badge styles for priority levels', () => {
    const { rerender } = render(
      <TaskItem
        task={{ ...baseTask, priority: Priority.HIGH }}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    expect(screen.getByText('HIGH')).toHaveClass('badge-danger');

    rerender(
      <TaskItem
        task={{ ...baseTask, priority: Priority.MEDIUM }}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    expect(screen.getByText('MEDIUM')).toHaveClass('badge-primary');

    rerender(
      <TaskItem
        task={{ ...baseTask, priority: Priority.LOW }}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    expect(screen.getByText('LOW')).toHaveClass('badge-gray');
  });

  it('should have border-left highlight for overdue incomplete tasks', () => {
    const overdueTask: Task = {
      ...baseTask,
      dueDate: '2024-01-15T10:00:00Z',
      isOverdue: true,
      isCompleted: false,
    };

    const { container } = render(
      <TaskItem
        task={overdueTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    const taskCard = container.firstChild as HTMLElement;
    expect(taskCard).toHaveClass('border-l-4', 'border-red-500');
  });

  it('should apply gray background to completed tasks', () => {
    const completedTask: Task = { ...baseTask, isCompleted: true };

    const { container } = render(
      <TaskItem
        task={completedTask}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );

    const taskCard = container.firstChild as HTMLElement;
    expect(taskCard).toHaveClass('bg-gray-50');
  });
});
