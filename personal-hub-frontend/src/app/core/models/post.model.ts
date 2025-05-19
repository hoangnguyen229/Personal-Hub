import { LikeResponse } from "./like.model";

export interface Post {
  title: string;
  excerpt: string;
  imageUrl: string;
  publishDate: string;
  commentCount: number;
  author?: {
    name: string;
    avatarUrl: string;
  };
  topic?: {
    name: string;
    iconUrl: string;
  };
}

export interface PostResponse {
  id: number;
  post_id?: number;
  title: string;
  content: string;
  created_at?: string;
  createdAt?: string;
  user?: {
    user_id: number;
    username: string;
    profile_picture?: string;
    profilePic?: string;
    bio?: string;
    email?: string;
  };
  category?: {
    name: string;
    id?: number;
  };
  likes?: LikeResponse[];
  comments?: any[];
  tags?: Array<{ name: string; id?: number }>;
  images?: Array<{ image_url?: string; imageUrl?: string }>;
}

export interface TransformedPost {
  id: number;
  title: string;
  author: string;
  email: string;
  publishedIn: string;
  publishDate: string;
  content: string;
  image: string;
  likes: number;
  comments: number;
  tags: string[];
  authorId: number;
  authorAvatar: string;
  authorBio: string;
}