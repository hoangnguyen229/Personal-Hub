export interface Comment {
    content: string;
    postId: number;
  }
  
  export interface CommentResponse {
    comment_id: number;
    content: string;
    created_at: string;
    deleted_at?: string;
    user?: {
      username: string;
      email?: string;
      profile_picture?: string;
    }
    showActions?: boolean;
    isEditing?: boolean;
  }