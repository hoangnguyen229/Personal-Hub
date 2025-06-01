import { Component, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { ToastrService } from 'ngx-toastr';
import { DateUtilService } from '../../core/services/date-util.service';
import { Notification } from '../../core/models/notification.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-notification',
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.css']
})
export class NotificationComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  currentUser: any = null;
  activeTab: 'all' | 'unread' = 'all';
  filteredNotifications: Notification[] = [];
  private subscriptions: Subscription[] = [];

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private toastrService: ToastrService,
    public dateUtilService: DateUtilService
  ) {}

  ngOnInit(): void {
    const userSub = this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
        
      this.loadNotifications();
        
      const notifySub = this.notificationService.notifications$.subscribe(notifications => {          
        this.notifications = notifications;
        this.updateFilteredNotifications();
      });
        
      this.subscriptions.push(notifySub);
    });
    
    this.subscriptions.push(userSub);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  loadNotifications(): void {
    this.notificationService.getNotifications().subscribe({
      next: (data) => {
        this.notifications = data;
        this.notificationService.updateNotifications(data);
        this.updateFilteredNotifications();
      },
      error: (err) => {
        this.toastrService.error(err.message);
      }
    });
  }

  getNotificationById(notificationId: number): void {
    this.notificationService.getNotificationById(notificationId).subscribe({
      next: (notification) => {
        this.notificationService.addNewNotification(notification);
      },
      error: (err) => {
        this.toastrService.error(err.message);
      }
    });
  }

  changeTab(tab: 'all' | 'unread'): void {
    this.activeTab = tab;
    this.updateFilteredNotifications();
  }

  updateFilteredNotifications(): void {
    if (this.activeTab === 'unread') {
      this.filteredNotifications = this.notifications.filter(n => !n.is_read);
    } else {
      this.filteredNotifications = [...this.notifications];
    }
  }

  hasUnreadNotifications(): boolean {
    return this.notifications.some(n => !n.is_read);
  }

  hasNotifications(): boolean {
    return this.notifications.length > 0;
  }

  markAsRead(notification: Notification): void {
    if (notification.is_read) {
      return;
    }
    
    this.notificationService.markAsRead(notification.notification_id).subscribe({
      next: (updatedNotification) => {
        notification.is_read = true;
        this.notificationService.updateNotificationReadStatus(notification.notification_id);
        this.updateFilteredNotifications();
      },
      error: (err) => {
        this.toastrService.error('Cannot mark as read');
      }
    });
  }

  markAllAsRead(): void {
    const unreadNotifications = this.notifications.filter(n => !n.is_read);
    
    Promise.all(
      unreadNotifications.map(notification => 
        this.notificationService.markAsRead(notification.notification_id).toPromise()
      )
    ).then(() => {
      this.notifications.forEach(notification => notification.is_read = true);
      this.updateFilteredNotifications();
    }).catch(err => {
      this.toastrService.error('Cannot mark all as read');
    });
  }

  deleteNotification(id: number): void {
    this.notificationService.deleteNotification(id).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.notification_id !== id);
        this.toastrService.success('Notification deleted successfully');
        this.updateFilteredNotifications();
      },
      error: (err) => {
        this.toastrService.error('Cannot delete notification');
      }
    });
  }

  clearAllNotifications(): void {
    this.notificationService.deleteAllNotifications().subscribe({
      next: () => {
        this.notifications = [];
        this.toastrService.success('All notifications cleared successfully');
        this.updateFilteredNotifications();
      },
      error: (err) => {
        this.toastrService.error('Cannot clear all notifications');
      }
    })
  }

  getUnreadNotificationCount(): number {
    return this.notifications.filter(n => !n.is_read).length;
  }

  getUserImageUrl(notification: Notification): string {
    const defaultImageUrl = 'https://example.com/images/default-user.jpg';
    return notification.userImageUrl || defaultImageUrl;
  }
}