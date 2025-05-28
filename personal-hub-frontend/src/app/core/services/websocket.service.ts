import { Injectable } from '@angular/core';
import { Client, Message } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { AuthService } from './auth.service';
import { Notification } from '../models/notification.model';
import { Messages } from '../models/messages.model'
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
  public notifications$ = this.notificationsSubject.asObservable();
  public messages$ = this.messagesSubject.asObservable();

  constructor(
    private authService: AuthService,
    private toatrService: ToastrService,
    private notificationService: NotificationService
  ) {}

  connect(): void {
    const token = this.authService.getAuthToken();
    if (!token) {
      console.error('No authentication token found');
      return;
    }
    
    console.log("Starting WebSocket connection...");
    const socket = new SockJS('http://localhost:8094/ws');

    this.stompClient = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => console.log("STOMP Debug:", str),
      onConnect: () => {
        console.log('Successfully connected to WebSocket server');
        if (this.stompClient?.connected) {
          console.log("Connection status verified as connected");
          this.subscribeToNotifications();
          this.subscribeToMessages();
        } else {
          console.error("onConnect called but connection status is false");
        }
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      },
      onWebSocketClose: (evt) => {
        console.log("WebSocket connection closed:", evt);
      }
    });
  
    this.stompClient.activate();
  }

  private subscribeToNotifications(): void {
    console.log("Subscribe function called");
    console.log("StompClient connection status:", this.stompClient?.connected);
    
    if (this.stompClient && this.stompClient.connected) {
      const userEmail = this.authService.getCurrentUser()?.email;
      
      if (userEmail) {
        console.log("Subscribing to:", `/user/${userEmail}/queue/notifications`);
        this.stompClient.subscribe(`/user/queue/notifications`, (message) => {
          try {
            const notification: Notification = JSON.parse(message.body);
            // this.toatrService.info("Recieved new notification");
            this.notificationService.addNewNotification(notification);
          } catch (e) {
            console.error("Error parsing message:", e);
          }
        });
        console.log("Subscription completed");
      } else {
        console.error("No user email found for subscription");
      }
    } else {
      console.error("StompClient not connected when trying to subscribe");
    }
  }

  private subscribeToMessages(): void {
    if(this.stompClient && this.stompClient.connected){
      const userEmail = this.authService.getCurrentUser()?.email;
      if(userEmail){
        console.log("Subscribing MESSAGE channel to:", `/user/${userEmail}/queue/messages`);
        this.stompClient.subscribe(`/user/queue/messages`, (message) => {
          try{
            debugger;
            const newMessage : Messages = JSON.parse(message.body);
            this.messagesSubject.next(newMessage);
          } catch(e){
            console.error("Error parsing message:", e);
          }
        })
      } else {
        console.error("No user email found for subscription");
      }
    }
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }
}