export interface Notification {
    notification_id: number;
    content: string;
    is_read: boolean;
    created_at: string;
    deleted_at?: string | null;
    email?: string;
    userImageUrl?: string;
}