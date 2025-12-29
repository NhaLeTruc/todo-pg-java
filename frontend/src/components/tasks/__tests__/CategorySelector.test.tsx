import userEvent from '@testing-library/user-event';

import { createMockCategories } from '@/__mocks__/factories';
import { categoryService } from '@/services/categoryService';
import { render, screen, waitFor } from '@/test-utils';

import { CategorySelector } from '../CategorySelector';

// Mock the category service
jest.mock('@/services/categoryService', () => ({
  categoryService: {
    getAll: jest.fn(),
  },
}));

describe('CategorySelector', () => {
  const mockOnChange = jest.fn();
  const mockCategories = createMockCategories(3);

  beforeEach(() => {
    jest.clearAllMocks();
    (categoryService.getAll as jest.Mock).mockResolvedValue(mockCategories);
  });

  it('should render with "No Category" as default option', async () => {
    render(<CategorySelector value={null} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(screen.getByRole('combobox')).toBeInTheDocument();
    });

    expect(screen.getByRole('option', { name: /no category/i })).toBeInTheDocument();
  });

  it('should load and display categories on mount', async () => {
    render(<CategorySelector value={null} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(categoryService.getAll).toHaveBeenCalledTimes(1);
    });

    await waitFor(() => {
      expect(screen.getByRole('option', { name: mockCategories[0].name })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: mockCategories[1].name })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: mockCategories[2].name })).toBeInTheDocument();
    });
  });

  it('should disable select while loading', () => {
    render(<CategorySelector value={null} onChange={mockOnChange} />);

    const select = screen.getByRole('combobox');
    expect(select).toBeDisabled();
  });

  it('should enable select after loading', async () => {
    render(<CategorySelector value={null} onChange={mockOnChange} />);

    await waitFor(() => {
      const select = screen.getByRole('combobox');
      expect(select).not.toBeDisabled();
    });
  });

  it('should display selected category', async () => {
    const selectedCategory = mockCategories[1];
    render(<CategorySelector value={selectedCategory.id} onChange={mockOnChange} />);

    await waitFor(() => {
      const select = screen.getByRole('combobox') as HTMLSelectElement;
      expect(select.value).toBe(selectedCategory.id.toString());
    });
  });

  it('should call onChange with category ID when a category is selected', async () => {
    const user = userEvent.setup();
    render(<CategorySelector value={null} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(screen.getByRole('combobox')).not.toBeDisabled();
    });

    const select = screen.getByRole('combobox');
    await user.selectOptions(select, mockCategories[0].id.toString());

    expect(mockOnChange).toHaveBeenCalledWith(mockCategories[0].id);
  });

  it('should call onChange with null when "No Category" is selected', async () => {
    const user = userEvent.setup();
    render(<CategorySelector value={mockCategories[0].id} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(screen.getByRole('combobox')).not.toBeDisabled();
    });

    const select = screen.getByRole('combobox');
    await user.selectOptions(select, '');

    expect(mockOnChange).toHaveBeenCalledWith(null);
  });

  it('should handle empty category list', async () => {
    (categoryService.getAll as jest.Mock).mockResolvedValue([]);

    render(<CategorySelector value={null} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(screen.getByRole('combobox')).not.toBeDisabled();
    });

    const options = screen.getAllByRole('option');
    expect(options).toHaveLength(1); // Only "No Category"
    expect(options[0]).toHaveTextContent(/no category/i);
  });

  it('should handle API error gracefully', async () => {
    const consoleError = jest.spyOn(console, 'error').mockImplementation();
    (categoryService.getAll as jest.Mock).mockRejectedValue(new Error('API Error'));

    render(<CategorySelector value={null} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(screen.getByRole('combobox')).not.toBeDisabled();
    });

    expect(consoleError).toHaveBeenCalledWith(
      'Failed to load categories:',
      expect.any(Error)
    );

    const options = screen.getAllByRole('option');
    expect(options).toHaveLength(1); // Only "No Category"

    consoleError.mockRestore();
  });

  it('should have correct accessibility attributes', async () => {
    render(<CategorySelector value={null} onChange={mockOnChange} />);

    await waitFor(() => {
      const select = screen.getByRole('combobox');
      expect(select).toHaveAttribute('id', 'category');
    });

    const label = screen.getByText('Category');
    expect(label).toHaveAttribute('for', 'category');
  });

  it('should render categories in the order received from API', async () => {
    render(<CategorySelector value={null} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(screen.getByRole('combobox')).not.toBeDisabled();
    });

    const options = screen.getAllByRole('option');
    // First option is "No Category"
    expect(options[1]).toHaveTextContent(mockCategories[0].name);
    expect(options[2]).toHaveTextContent(mockCategories[1].name);
    expect(options[3]).toHaveTextContent(mockCategories[2].name);
  });

  it('should not reload categories on re-render', async () => {
    const { rerender } = render(<CategorySelector value={null} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(categoryService.getAll).toHaveBeenCalledTimes(1);
    });

    rerender(<CategorySelector value={mockCategories[0].id} onChange={mockOnChange} />);

    // Should still be called only once
    expect(categoryService.getAll).toHaveBeenCalledTimes(1);
  });
});
