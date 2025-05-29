export interface User {
  user_id: number;
  username: string;
  email: string;
  bio?: string;
  profile_picture?: string;
  auth_type: string;
  roles: string[];
  show_online_status: boolean;
  hasUnreadMessages?: boolean;
  lastMessage?: string;
  is_online: boolean;
}