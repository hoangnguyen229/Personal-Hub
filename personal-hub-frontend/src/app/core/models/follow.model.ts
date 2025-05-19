import { User } from './user.model';

export interface Follow{
    followingID: number;
}

export interface FollowResponse {
    follow_id: number;
    follower_user?: User;
    following_user?: User;
    created_at: string;
    updated_at: string;
    deleted_at: string | null;
}