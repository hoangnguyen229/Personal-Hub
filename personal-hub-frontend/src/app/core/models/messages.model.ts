import { User } from "./user.model";

export interface Messages {
    receiverId: number;
    content: string;
}

export interface MessagesResponse {
    message_id: number;
    sender?: User;
    receiver?: User;
    content: string;
    status: string;
    approval_status: string;
    approved_at?: string;
    sent_at: string;
}