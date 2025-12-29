import userEvent from '@testing-library/user-event';

import { createMockTask, createCompletedTask, createOverdueTask } from '@/__mocks__/factories';
import { render, screen } from '@/test-utils';
import type { Task } from '@/types/task';

import { TaskList } from '../TaskList';

describe('TaskList', () => {
  const mockOnToggleComplete = jest.fn();
  const mockOnDelete = jest.fn();
  const mockOnEdit = jest.fn();

  const mockTasks: Task[] = [
    createMockTask({
      id: 1,
      description: 'Buy groceries',
      priority: 'HIGH',
      dueDate: '2025-12-31T10:00:00',
      createdAt: '2025-12-26T10:00:00',
      updatedAt: '2025-12-26T10:00:00',
    }),
    createCompletedTask({
      id: 2,
      description: 'Call dentist',
      priority: 'MEDIUM',
      completedAt: '2025-12-25T14:30:00',
      categoryId: 1,
      categoryName: 'Health',
      estimatedDurationMinutes: 15,
      actualDurationMinutes: 10,
      createdAt: '2025-12-20T10:00:00',
      updatedAt: '2025-12-25T14:30:00',
    }),
    createOverdueTask({
      id: 3,
      description: 'Submit report',
      priority: 'LOW',
      dueDate: '2025-12-20T17:00:00',
      estimatedDurationMinutes: 120,
      createdAt: '2025-12-15T10:00:00',
      updatedAt: '2025-12-15T10:00:00',
    }),
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render loading skeleton when isLoading is true', () => {
    render(
      <TaskList
        tasks={[]}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={true}
      />
    );

    const skeletons = screen
      .getAllByRole('generic')
      .filter((el) => el.className.includes('animate-pulse'));
    expect(skeletons.length).toBeGreaterThan(0);
  });

  it('should render empty state when no tasks', () => {
    render(
      <TaskList
        tasks={[]}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={false}
      />
    );

    expect(screen.getByText(/no tasks/i)).toBeInTheDocument();
    expect(screen.getByText(/get started by creating a new task/i)).toBeInTheDocument();
  });

  it('should render all tasks when provided', () => {
    render(
      <TaskList
        tasks={mockTasks}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={false}
      />
    );

    expect(screen.getByText('Buy groceries')).toBeInTheDocument();
    expect(screen.getByText('Call dentist')).toBeInTheDocument();
    expect(screen.getByText('Submit report')).toBeInTheDocument();
  });

  it('should display priority badges for each task', () => {
    render(
      <TaskList
        tasks={mockTasks}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={false}
      />
    );

    expect(screen.getByText('HIGH')).toBeInTheDocument();
    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
    expect(screen.getByText('LOW')).toBeInTheDocument();
  });

  it('should display category name when present', () => {
    render(
      <TaskList
        tasks={mockTasks}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={false}
      />
    );

    expect(screen.getByText('Health')).toBeInTheDocument();
  });

  it('should display due dates formatted correctly', () => {
    render(
      <TaskList
        tasks={mockTasks}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={false}
      />
    );

    expect(screen.getByText(/Due: Dec 31, 2025/)).toBeInTheDocument();
    expect(screen.getByText(/Due: Dec 20, 2025/)).toBeInTheDocument();
  });

  it('should show completed tasks with strikethrough', () => {
    render(
      <TaskList
        tasks={mockTasks}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={false}
      />
    );

    const completedTaskText = screen.getByText('Call dentist');
    expect(completedTaskText.className).toContain('line-through');
  });

  it('should highlight overdue tasks', () => {
    render(
      <TaskList
        tasks={mockTasks}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={false}
      />
    );

    const overdueTask = screen.getByText('Submit report').closest('.card');
    expect(overdueTask?.className).toContain('border-red-500');
  });

  it('should call onToggleComplete when checkbox is clicked', async () => {
    const user = userEvent.setup();
    render(
      <TaskList
        tasks={mockTasks}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={false}
      />
    );

    const checkboxButtons = screen.getAllByRole('button', {
      name: /mark (complete|incomplete)/i,
    });
    await user.click(checkboxButtons.at(0) as HTMLElement);

    expect(mockOnToggleComplete).toHaveBeenCalledWith(1, true);
  });

  it('should call onEdit when edit button is clicked', async () => {
    const user = userEvent.setup();
    render(
      <TaskList
        tasks={mockTasks}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={false}
      />
    );

    const editButtons = screen.getAllByRole('button', { name: /edit task/i });
    await user.click(editButtons.at(0) as HTMLElement);

    expect(mockOnEdit).toHaveBeenCalledWith(1);
  });

  it('should call onDelete when delete button is clicked', async () => {
    const user = userEvent.setup();
    render(
      <TaskList
        tasks={mockTasks}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={false}
      />
    );

    const deleteButtons = screen.getAllByRole('button', { name: /delete task/i });
    await user.click(deleteButtons.at(0) as HTMLElement);

    expect(mockOnDelete).toHaveBeenCalledWith(1);
  });

  it('should render tasks in correct order', () => {
    const { container } = render(
      <TaskList
        tasks={mockTasks}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={false}
      />
    );

    const taskElements = Array.from(container.querySelectorAll('.card'));

    expect(taskElements.at(0)?.textContent).toContain('Buy groceries');
    expect(taskElements.at(1)?.textContent).toContain('Call dentist');
    expect(taskElements.at(2)?.textContent).toContain('Submit report');
  });

  it('should handle single task correctly', () => {
    const singleTask = mockTasks.at(0);
    expect(singleTask).toBeDefined();

    render(
      <TaskList
        tasks={[singleTask as Task]}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={false}
      />
    );

    expect(screen.getByText('Buy groceries')).toBeInTheDocument();
    expect(screen.queryByText('Call dentist')).not.toBeInTheDocument();
  });

  it('should not render empty state when loading', () => {
    render(
      <TaskList
        tasks={[]}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={true}
      />
    );

    expect(screen.queryByText(/no tasks/i)).not.toBeInTheDocument();
  });

  it('should show creation date for all tasks', () => {
    render(
      <TaskList
        tasks={mockTasks}
        onToggleComplete={mockOnToggleComplete}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
        isLoading={false}
      />
    );

    expect(screen.getByText(/Created Dec 26, 2025/)).toBeInTheDocument();
    expect(screen.getByText(/Created Dec 20, 2025/)).toBeInTheDocument();
    expect(screen.getByText(/Created Dec 15, 2025/)).toBeInTheDocument();
  });
});
