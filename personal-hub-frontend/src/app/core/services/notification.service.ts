import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { API_CONFIG } from '../api.config';
import { AuthService } from './auth.service';
import { Notification } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = `${API_CONFIG.baseUrl}/notifications`;
  private notificationsSubject = new BehaviorSubject<any>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getNotifications(): Observable<any> {
    return this.http.get<any>(
      this.apiUrl,
      { headers: this.authService.getAuthHeaders() }
    );
  }

  getNotificationById(notificationId: number): Observable<Notification> {
    return this.http.get<Notification>(
      `${this.apiUrl}/${notificationId}`,
      { headers: this.authService.getAuthHeaders() }
    );
  }

  markAsRead(notificationId: number): Observable<Notification> {
    return this.http.put<Notification>(
      `${this.apiUrl}/${notificationId}`,
      {},
      { headers: this.authService.getAuthHeaders() }
    );
  }

  updateNotifications(notifications: any): void {
    this.notificationsSubject.next(notifications);
  }
  
  
  addNewNotification(notification: Notification): void {
    if (!notification || !notification.content || notification.content.trim() === '') {
        return;
    }
    
    const currentNotifications = this.notificationsSubject.getValue();
    
    const existingIndex = currentNotifications.findIndex(
      n => n.notification_id === notification.notification_id
    );
    
    if (existingIndex >= 0) {
      // Nếu thông báo đã tồn tại, cập nhật nó
      currentNotifications[existingIndex] = notification;
      this.notificationsSubject.next([...currentNotifications]);
    } else {
      // Nếu là thông báo mới, thêm vào đầu danh sách để hiển thị trước
      this.notificationsSubject.next([notification, ...currentNotifications]);
    }
  }

  updateNotificationReadStatus(notificationId: number): void {
    const currentNotifications = this.notificationsSubject.getValue();
    const updatedNotifications = currentNotifications.map(notification => {
      if (notification.notification_id === notificationId) {
        return { ...notification, is_read: true };
      }
      return notification;
    });
    this.notificationsSubject.next(updatedNotifications);
  }

  deleteNotification(notificationId: number): Observable<Notification> {
      return this.http.delete<Notification>(
        `${this.apiUrl}/${notificationId}`, 
        { headers: this.authService.getAuthHeaders() }
      );
    }

  deleteAllNotifications(): Observable<any> {
    return this.http.delete<any>(
      `${this.apiUrl}/all`,
      { headers: this.authService.getAuthHeaders() }
    );
  }
}