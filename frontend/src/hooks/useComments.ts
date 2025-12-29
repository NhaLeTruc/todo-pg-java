import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { commentService } from '../services/commentService';
import { Comment, CommentCreateRequest, CommentUpdateRequest } from '../types/comment';

const COMMENTS_QUERY_KEY = 'comments';

/**
 * Hook for fetching task comments
 */
export function useTaskComments(taskId: number) {
  return useQuery({
    queryKey: [COMMENTS_QUERY_KEY, taskId],
    queryFn: () => commentService.getTaskComments(taskId),
    enabled: !!taskId,
    staleTime: 10000, // Consider data fresh for 10 seconds
  });
}

/**
 * Hook for creating a comment with optimistic update
 */
export function useCreateComment(taskId: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CommentCreateRequest) => commentService.createComment(taskId, data),
    onMutate: async (newComment) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: [COMMENTS_QUERY_KEY, taskId] });

      // Snapshot the previous value
      const previousComments = queryClient.getQueryData([COMMENTS_QUERY_KEY, taskId]);

      // Optimistically update to the new value
      queryClient.setQueryData([COMMENTS_QUERY_KEY, taskId], (old: Comment[] | undefined) => {
        if (!old) return old;
        const optimisticComment: Comment = {
          id: Date.now(), // Temporary ID
          taskId,
          content: newComment.content,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          authorId: 1, // Default user ID
          authorEmail: 'you@example.com', // Placeholder
          isEdited: false,
        };
        return [...old, optimisticComment];
      });

      return { previousComments };
    },
    onError: (_err, _newComment, context) => {
      // Rollback on error
      if (context?.previousComments) {
        queryClient.setQueryData([COMMENTS_QUERY_KEY, taskId], context.previousComments);
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [COMMENTS_QUERY_KEY, taskId] });
    },
  });
}

/**
 * Hook for updating a comment with optimistic update
 */
export function useUpdateComment(taskId: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ commentId, data }: { commentId: number; data: CommentUpdateRequest }) =>
      commentService.updateComment(commentId, data),
    onMutate: async ({ commentId, data }) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: [COMMENTS_QUERY_KEY, taskId] });

      // Snapshot the previous value
      const previousComments = queryClient.getQueryData([COMMENTS_QUERY_KEY, taskId]);

      // Optimistically update the comment
      queryClient.setQueryData([COMMENTS_QUERY_KEY, taskId], (old: Comment[] | undefined) => {
        if (!old) return old;
        return old.map((comment) =>
          comment.id === commentId
            ? {
                ...comment,
                ...data,
                updatedAt: new Date().toISOString(),
              }
            : comment
        );
      });

      return { previousComments };
    },
    onError: (_err, _variables, context) => {
      // Rollback on error
      if (context?.previousComments) {
        queryClient.setQueryData([COMMENTS_QUERY_KEY, taskId], context.previousComments);
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [COMMENTS_QUERY_KEY, taskId] });
    },
  });
}

/**
 * Hook for deleting a comment with optimistic update
 */
export function useDeleteComment(taskId: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (commentId: number) => commentService.deleteComment(commentId),
    onMutate: async (commentId) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: [COMMENTS_QUERY_KEY, taskId] });

      // Snapshot the previous value
      const previousComments = queryClient.getQueryData([COMMENTS_QUERY_KEY, taskId]);

      // Optimistically remove the comment
      queryClient.setQueryData([COMMENTS_QUERY_KEY, taskId], (old: Comment[] | undefined) => {
        if (!old) return old;
        return old.filter((comment) => comment.id !== commentId);
      });

      return { previousComments };
    },
    onError: (_err, _commentId, context) => {
      // Rollback on error
      if (context?.previousComments) {
        queryClient.setQueryData([COMMENTS_QUERY_KEY, taskId], context.previousComments);
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [COMMENTS_QUERY_KEY, taskId] });
    },
  });
}
