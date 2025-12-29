import { createMockTask } from '@/__mocks__/factories';
import { render, screen } from '@/test-utils';

import { SubtaskProgressBar } from '../SubtaskProgressBar';

describe('SubtaskProgressBar', () => {
  it('should not render when progress is 0 and subtaskProgress is 0', () => {
    const task = createMockTask({ subtaskProgress: 0 });
    const { container } = render(<SubtaskProgressBar task={task} />);

    expect(container.firstChild).toBeNull();
  });

  it('should render when progress is greater than 0', () => {
    const task = createMockTask({ subtaskProgress: 50 });
    render(<SubtaskProgressBar task={task} />);

    expect(screen.getByText('Subtask Progress')).toBeInTheDocument();
    expect(screen.getByText('50%')).toBeInTheDocument();
  });

  it('should display correct progress percentage', () => {
    const task = createMockTask({ subtaskProgress: 75 });
    render(<SubtaskProgressBar task={task} />);

    expect(screen.getByText('75%')).toBeInTheDocument();
  });

  it('should display 0% when progress is null but explicitly set to 0', () => {
    const task = createMockTask({ subtaskProgress: 0 });
    // Even though subtaskProgress is 0, if it's explicitly provided, we check the condition
    // Based on the component logic: progress === 0 && task.subtaskProgress === 0 returns null
    const { container } = render(<SubtaskProgressBar task={task} />);

    expect(container.firstChild).toBeNull();
  });

  it('should render progress bar with correct width', () => {
    const task = createMockTask({ subtaskProgress: 60 });
    render(<SubtaskProgressBar task={task} />);

    const progressBar = screen.getByText('Subtask Progress').parentElement?.nextElementSibling
      ?.firstElementChild;
    expect(progressBar).toHaveStyle({ width: '60%' });
  });

  it('should show blue progress bar when progress is less than 100', () => {
    const task = createMockTask({ subtaskProgress: 50 });
    render(<SubtaskProgressBar task={task} />);

    const progressBar = screen.getByText('Subtask Progress').parentElement?.nextElementSibling
      ?.firstElementChild;
    expect(progressBar).toHaveClass('bg-blue-500');
  });

  it('should show green progress bar when progress is 100', () => {
    const task = createMockTask({ subtaskProgress: 100 });
    render(<SubtaskProgressBar task={task} />);

    const progressBar = screen.getByText('Subtask Progress').parentElement?.nextElementSibling
      ?.firstElementChild;
    expect(progressBar).toHaveClass('bg-green-500');
  });

  it('should not show green progress bar when progress is 99', () => {
    const task = createMockTask({ subtaskProgress: 99 });
    render(<SubtaskProgressBar task={task} />);

    const progressBar = screen.getByText('Subtask Progress').parentElement?.nextElementSibling
      ?.firstElementChild;
    expect(progressBar).toHaveClass('bg-blue-500');
    expect(progressBar).not.toHaveClass('bg-green-500');
  });

  it('should apply custom className when provided', () => {
    const task = createMockTask({ subtaskProgress: 50 });
    const { container } = render(<SubtaskProgressBar task={task} className="custom-class" />);

    const rootElement = container.firstChild as HTMLElement;
    expect(rootElement).toHaveClass('custom-class');
  });

  it('should have correct styling classes', () => {
    const task = createMockTask({ subtaskProgress: 50 });
    const { container } = render(<SubtaskProgressBar task={task} />);

    const rootElement = container.firstChild as HTMLElement;
    expect(rootElement).toHaveClass('space-y-1');
  });

  it('should display "Subtask Progress" label', () => {
    const task = createMockTask({ subtaskProgress: 50 });
    render(<SubtaskProgressBar task={task} />);

    const label = screen.getByText('Subtask Progress');
    expect(label).toBeInTheDocument();
    // The parent div has the text classes, not the span itself
    expect(label.parentElement).toHaveClass('text-xs', 'text-gray-600');
  });

  it('should display progress percentage with font-medium class', () => {
    const task = createMockTask({ subtaskProgress: 50 });
    render(<SubtaskProgressBar task={task} />);

    const percentageElement = screen.getByText('50%');
    expect(percentageElement).toHaveClass('font-medium');
  });

  it('should have rounded-full progress bar container', () => {
    const task = createMockTask({ subtaskProgress: 50 });
    render(<SubtaskProgressBar task={task} />);

    const progressBarContainer = screen.getByText('Subtask Progress').parentElement
      ?.nextElementSibling;
    expect(progressBarContainer).toHaveClass('rounded-full', 'bg-gray-200', 'h-2', 'w-full');
  });

  it('should have transition-all on progress bar', () => {
    const task = createMockTask({ subtaskProgress: 50 });
    render(<SubtaskProgressBar task={task} />);

    const progressBar = screen.getByText('Subtask Progress').parentElement?.nextElementSibling
      ?.firstElementChild;
    expect(progressBar).toHaveClass('transition-all', 'duration-300');
  });

  it('should handle progress value of 1', () => {
    const task = createMockTask({ subtaskProgress: 1 });
    render(<SubtaskProgressBar task={task} />);

    expect(screen.getByText('1%')).toBeInTheDocument();
    const progressBar = screen.getByText('Subtask Progress').parentElement?.nextElementSibling
      ?.firstElementChild;
    expect(progressBar).toHaveStyle({ width: '1%' });
  });

  it('should handle progress value of 99', () => {
    const task = createMockTask({ subtaskProgress: 99 });
    render(<SubtaskProgressBar task={task} />);

    expect(screen.getByText('99%')).toBeInTheDocument();
    const progressBar = screen.getByText('Subtask Progress').parentElement?.nextElementSibling
      ?.firstElementChild;
    expect(progressBar).toHaveStyle({ width: '99%' });
  });

  it('should update when task progress changes', () => {
    const task = createMockTask({ subtaskProgress: 50 });
    const { rerender } = render(<SubtaskProgressBar task={task} />);

    expect(screen.getByText('50%')).toBeInTheDocument();

    const updatedTask = createMockTask({ subtaskProgress: 75 });
    rerender(<SubtaskProgressBar task={updatedTask} />);

    expect(screen.getByText('75%')).toBeInTheDocument();
  });

  it('should handle task with undefined subtaskProgress', () => {
    const task = createMockTask({ subtaskProgress: undefined });
    render(<SubtaskProgressBar task={task} />);

    // undefined is falsy, so progress = 0, and since task.subtaskProgress is undefined (not 0),
    // the condition progress === 0 && task.subtaskProgress === 0 is false
    // Wait, let me re-check: progress = task.subtaskProgress || 0
    // If subtaskProgress is undefined, progress = 0
    // Then: if (progress === 0 && task.subtaskProgress === 0) return null
    // 0 === 0 && undefined === 0 -> true && false -> false
    // So it should render with 0%
    expect(screen.getByText('0%')).toBeInTheDocument();
  });

  it('should combine custom className with default classes', () => {
    const task = createMockTask({ subtaskProgress: 50 });
    const { container } = render(
      <SubtaskProgressBar task={task} className="mt-4 custom-spacing" />
    );

    const rootElement = container.firstChild as HTMLElement;
    expect(rootElement).toHaveClass('space-y-1', 'mt-4', 'custom-spacing');
  });
});
