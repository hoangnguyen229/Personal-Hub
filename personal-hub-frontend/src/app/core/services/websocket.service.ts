import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
// import * as SockJS from 'sockjs-client';
import { AuthService } from './auth.service';
import { Notification } from '../models/notification.model';
import { Messages } from '../models/messages.model';
import { User } from '../models/user.model';
import { BehaviorSubject } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { NotificationService } from './notification.service';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private stompClient: Client | undefined;
  private notificationsSubject = new BehaviorSubject<any>([]);
  private messagesSubject = new BehaviorSubject<any>([]);
  private userStatusSubject = new BehaviorSubject<User[]>([]);
  private userStatusMap = new Map<number, boolean>();
  public notifications$ = this.notificationsSubject.asObservable();
  public messages$ = this.messagesSubject.asObservable();
  public userStatus$ = this.userStatusSubject.asObservable();

  constructor(
    private authService: AuthService,
    private toastrService: ToastrService,
    private notificationService: NotificationService
  ) {}

  connect(): void {
    const token = this.authService.getAuthToken();
    if (!token) {
      console.error('No authentication token found');
      return;
    }
    debugger;
    console.log("Starting WebSocket connection...");
    // const socket = new SockJS('http://localhost:8094/ws');

    this.stompClient = new Client({
      // webSocketFactory: () => socket,
      brokerURL: 'http://hoangnguyen-dev.site/ws',
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => console.log("STOMP Debug:", str),
      onConnect: () => {
        console.log('Successfully connected to WebSocket server');
        if (this.stompClient?.connected) {
          console.log("Connection status verified as connected for user:", this.authService.getCurrentUser()?.user_id);
          this.subscribeToNotifications();
          this.subscribeToMessages();
          this.subscribeToUserStatus();
        } else {
          console.error("onConnect called but connection status is false");
        }
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      },
      onWebSocketClose: (evt) => {
        console.log("WebSocket connection closed:", evt);
        setTimeout(() => this.connect(), 5000); // Thử kết nối lại sau 5 giây
      }
    });

    this.stompClient.activate();
  }

  private subscribeToNotifications(): void {
    if (this.stompClient && this.stompClient.connected) {
      const userEmail = this.authService.getCurrentUser()?.email;
      if (userEmail) {
        console.log("Subscribing to:", `/user/${userEmail}/queue/notifications`);
        this.stompClient.subscribe(`/user/queue/notifications`, (message) => {
          try {
            const notification: Notification = JSON.parse(message.body);
            this.notificationService.addNewNotification(notification);
          } catch (e) {
            console.error("Error parsing notification message:", e);
          }
        });
        console.log("Notification subscription completed");
      } else {
        console.error("No user email found for notification subscription");
      }
    } else {
      console.error("StompClient not connected when trying to subscribe to notifications");
    }
  }

  private subscribeToMessages(): void {
    if (this.stompClient && this.stompClient.connected) {
      const userEmail = this.authService.getCurrentUser()?.email;
      if (userEmail) {
        console.log("Subscribing MESSAGE channel to:", `/user/${userEmail}/queue/messages`);
        this.stompClient.subscribe(`/user/queue/messages`, (message) => {
          try {
            const newMessage: Messages = JSON.parse(message.body);
            this.messagesSubject.next(newMessage);
          } catch (e) {
            console.error("Error parsing message:", e);
          }
        });
      } else {
        console.error("No user email found for message subscription");
      }
    } else {
      console.error("StompClient not connected when trying to subscribe to messages");
    }
  }

  private subscribeToUserStatus(): void {
    if (this.stompClient && this.stompClient.connected) {
      const currentUserId = this.authService.getCurrentUser()?.user_id;
      console.log("Subscribing to user status channel: /topic/user_status_channel for user:", currentUserId);
      this.stompClient.subscribe(`/topic/user_status_channel`, (message) => {
        try {
          const user: User = JSON.parse(message.body);
          console.log("Received user status update:", user);

          // Không cập nhật trạng thái của người dùng hiện tại
          if (currentUserId && user.user_id === currentUserId) {
            console.log(`Skipping status update for current user: ${user.user_id}`);
            return;
          }

          // Cập nhật trạng thái vào Map
          this.userStatusMap.set(user.user_id, user.is_online);

          // Lấy danh sách user hiện tại
          let currentUsers = this.userStatusSubject.value;

          // Cập nhật hoặc thêm user vào danh sách
          const updatedUsers = currentUsers.filter(u => u.user_id !== user.user_id);
          updatedUsers.push({ ...user, is_online: user.is_online });

          console.log("Updated user status list:", updatedUsers);
          this.userStatusSubject.next(updatedUsers);
        } catch (e) {
          console.error("Error parsing user status message:", e);
        }
      });
    } else {
      console.error("StompClient not connected when trying to subscribe to user status");
    }
  }

  public getUserStatus(userId: number): boolean {
    return this.userStatusMap.get(userId) ?? false;
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      console.log("WebSocket disconnected");
    }
  }
}