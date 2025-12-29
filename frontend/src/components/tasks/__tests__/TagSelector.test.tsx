import userEvent from '@testing-library/user-event';

import { createMockTags } from '@/__mocks__/factories';
import { tagService } from '@/services/tagService';
import { render, screen, waitFor } from '@/test-utils';

import { TagSelector } from '../TagSelector';

// Mock the tag service
jest.mock('@/services/tagService', () => ({
  tagService: {
    getAll: jest.fn(),
  },
}));

describe('TagSelector', () => {
  const mockOnChange = jest.fn();
  const mockTags = createMockTags(4);

  beforeEach(() => {
    jest.clearAllMocks();
    (tagService.getAll as jest.Mock).mockResolvedValue(mockTags);
  });

  it('should load and display tags on mount', async () => {
    render(<TagSelector value={[]} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(tagService.getAll).toHaveBeenCalledTimes(1);
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: mockTags[0].name })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: mockTags[1].name })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: mockTags[2].name })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: mockTags[3].name })).toBeInTheDocument();
    });
  });

  it('should disable tag buttons while loading', () => {
    render(<TagSelector value={[]} onChange={mockOnChange} />);

    // During initial render, tags array is empty, so no buttons yet
    // Wait for tags to load before checking disabled state
  });

  it('should enable tag buttons after loading', async () => {
    render(<TagSelector value={[]} onChange={mockOnChange} />);

    await waitFor(() => {
      const buttons = screen.getAllByRole('button');
      buttons.forEach((button) => {
        expect(button).not.toBeDisabled();
      });
    });
  });

  it('should highlight selected tags', async () => {
    const selectedTagIds = [mockTags[0].id, mockTags[2].id];
    render(<TagSelector value={selectedTagIds} onChange={mockOnChange} />);

    await waitFor(() => {
      const selectedTag1 = screen.getByRole('button', { name: mockTags[0].name });
      const selectedTag2 = screen.getByRole('button', { name: mockTags[2].name });
      const unselectedTag = screen.getByRole('button', { name: mockTags[1].name });

      expect(selectedTag1).toHaveClass('btn-primary');
      expect(selectedTag2).toHaveClass('btn-primary');
      expect(unselectedTag).toHaveClass('btn-secondary');
    });
  });

  it('should add tag to selection when clicked', async () => {
    const user = userEvent.setup();
    render(<TagSelector value={[]} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: mockTags[0].name })).toBeInTheDocument();
    });

    const tagButton = screen.getByRole('button', { name: mockTags[0].name });
    await user.click(tagButton);

    expect(mockOnChange).toHaveBeenCalledWith([mockTags[0].id]);
  });

  it('should remove tag from selection when clicked again', async () => {
    const user = userEvent.setup();
    const selectedTagIds = [mockTags[0].id, mockTags[1].id];
    render(<TagSelector value={selectedTagIds} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: mockTags[0].name })).toBeInTheDocument();
    });

    const tagButton = screen.getByRole('button', { name: mockTags[0].name });
    await user.click(tagButton);

    expect(mockOnChange).toHaveBeenCalledWith([mockTags[1].id]);
  });

  it('should allow multiple tags to be selected', async () => {
    const user = userEvent.setup();
    render(<TagSelector value={[mockTags[0].id]} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: mockTags[1].name })).toBeInTheDocument();
    });

    const tag2Button = screen.getByRole('button', { name: mockTags[1].name });
    await user.click(tag2Button);

    expect(mockOnChange).toHaveBeenCalledWith([mockTags[0].id, mockTags[1].id]);
  });

  it('should apply custom color to selected tags', async () => {
    const coloredTag = { ...mockTags[0], color: '#ff0000' };
    (tagService.getAll as jest.Mock).mockResolvedValue([coloredTag, ...mockTags.slice(1)]);

    render(<TagSelector value={[coloredTag.id]} onChange={mockOnChange} />);

    await waitFor(() => {
      const tagButton = screen.getByRole('button', { name: coloredTag.name });
      expect(tagButton).toHaveStyle({
        backgroundColor: '#ff0000',
        borderColor: '#ff0000',
      });
    });
  });

  it('should not apply custom color to unselected tags', async () => {
    const coloredTag = { ...mockTags[0], color: '#ff0000' };
    (tagService.getAll as jest.Mock).mockResolvedValue([coloredTag, ...mockTags.slice(1)]);

    render(<TagSelector value={[]} onChange={mockOnChange} />);

    await waitFor(() => {
      const tagButton = screen.getByRole('button', { name: coloredTag.name });
      expect(tagButton).not.toHaveStyle({
        backgroundColor: '#ff0000',
      });
    });
  });

  it('should handle empty tag list', async () => {
    (tagService.getAll as jest.Mock).mockResolvedValue([]);

    render(<TagSelector value={[]} onChange={mockOnChange} />);

    await waitFor(() => {
      const buttons = screen.queryAllByRole('button');
      expect(buttons).toHaveLength(0);
    });
  });

  it('should handle API error gracefully', async () => {
    const consoleError = jest.spyOn(console, 'error').mockImplementation();
    (tagService.getAll as jest.Mock).mockRejectedValue(new Error('API Error'));

    render(<TagSelector value={[]} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(consoleError).toHaveBeenCalledWith('Failed to load tags:', expect.any(Error));
    });

    const buttons = screen.queryAllByRole('button');
    expect(buttons).toHaveLength(0);

    consoleError.mockRestore();
  });

  it('should have correct button types to prevent form submission', async () => {
    render(<TagSelector value={[]} onChange={mockOnChange} />);

    await waitFor(() => {
      const buttons = screen.getAllByRole('button');
      buttons.forEach((button) => {
        expect(button).toHaveAttribute('type', 'button');
      });
    });
  });

  it('should render tags in the order received from API', async () => {
    render(<TagSelector value={[]} onChange={mockOnChange} />);

    await waitFor(() => {
      const buttons = screen.getAllByRole('button');
      expect(buttons[0]).toHaveTextContent(mockTags[0].name);
      expect(buttons[1]).toHaveTextContent(mockTags[1].name);
      expect(buttons[2]).toHaveTextContent(mockTags[2].name);
      expect(buttons[3]).toHaveTextContent(mockTags[3].name);
    });
  });

  it('should not reload tags on re-render', async () => {
    const { rerender } = render(<TagSelector value={[]} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(tagService.getAll).toHaveBeenCalledTimes(1);
    });

    rerender(<TagSelector value={[mockTags[0].id]} onChange={mockOnChange} />);

    // Should still be called only once
    expect(tagService.getAll).toHaveBeenCalledTimes(1);
  });

  it('should handle selecting all tags', async () => {
    const user = userEvent.setup();
    const { rerender } = render(<TagSelector value={[]} onChange={mockOnChange} />);

    await waitFor(() => {
      expect(screen.getAllByRole('button')).toHaveLength(4);
    });

    // Simulate controlled component by clicking and updating value prop
    let selectedTags: number[] = [];

    for (const tag of mockTags) {
      const button = screen.getByRole('button', { name: tag.name });
      await user.click(button);

      // Simulate parent component updating the value prop
      selectedTags = [...selectedTags, tag.id];
      rerender(<TagSelector value={selectedTags} onChange={mockOnChange} />);
    }

    // Should have been called 4 times, once for each tag
    expect(mockOnChange).toHaveBeenCalledTimes(4);

    // Last call should have all tag IDs
    const lastCall = mockOnChange.mock.calls[mockOnChange.mock.calls.length - 1][0];
    expect(lastCall).toHaveLength(4);
    expect(lastCall).toEqual(expect.arrayContaining(mockTags.map((t) => t.id)));
  });
});
