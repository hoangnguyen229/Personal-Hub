export interface Like {
    postId: number;
}
  
export interface LikeResponse {
    like_id: number;
    post_id: number;
    user_id: number;
    created_at?: string;
    deleted_at?: string;
}