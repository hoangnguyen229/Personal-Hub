import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { MessageService } from '../../core/services/message.service';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/models/user.model';
import { Messages, MessagesResponse } from '../../core/models/messages.model';
import { WebSocketService } from '../../core/services/websocket.service';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-message',
  templateUrl: './message.component.html',
  styleUrls: ['./message.component.css']
})
export class MessageComponent implements OnInit, OnDestroy, AfterViewChecked {
  users: User[] = [];
  pendingUsers: User[] = [];
  currentChat: User | null = null;
  messages: MessagesResponse[] = [];
  newMessage: string = '';
  activeTab: 'active' | 'pending' = 'active';
  private messageSub: Subscription | undefined;
  private userStatusSub: Subscription | undefined;

  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  constructor(
    private messageService: MessageService,
    private webSocketService: WebSocketService,
    private authService: AuthService,
    private userService: UserService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.messageService.getConversations().subscribe({
      next: (conversations) => {
        this.users = conversations;

        // Lấy trạng thái online của các user trong danh sách hội thoại
        const userIds = this.users.map(u => u.user_id);
        if (userIds.length > 0) {
          this.userService.getUsersStatus(userIds).subscribe({
            next: (onlineUsers) => {
              this.users = this.users.map(u => {
                const onlineUser = onlineUsers.find(ou => ou.user_id === u.user_id);
                return onlineUser ? { ...u, is_online: onlineUser.is_online } : { ...u, is_online: false };
              });
              this.pendingUsers = this.pendingUsers.map(u => {
                const onlineUser = onlineUsers.find(ou => ou.user_id === u.user_id);
                return onlineUser ? { ...u, is_online: onlineUser.is_online } : { ...u, is_online: false };
              });

              // Chọn chat mặc định
              this.route.queryParams.subscribe(params => {
                const email = params['email'];
                if (email) {
                  this.loadUserForChat(email);
                } else if (this.users.length > 0) {
                  this.selectChat(this.users[0], 'active');
                }
              });
            },
            error: (err) => {
              console.error('Lỗi khi lấy trạng thái người dùng:', err);
              this.users = this.users.map(u => ({ ...u, is_online: false }));
              this.pendingUsers = this.pendingUsers.map(u => ({ ...u, is_online: false }));
            }
          });
        } else {
          this.route.queryParams.subscribe(params => {
            const email = params['email'];
            if (email) {
              this.loadUserForChat(email);
            }
          });
        }
      },
      error: (err) => {
        console.error('Lỗi khi lấy danh sách hội thoại:', err);
      }
    });

    this.loadPendingConversations();

    this.userStatusSub = this.webSocketService.userStatus$.subscribe(users => {
      this.users = this.users.map(u => {
        const updatedUser = users.find(nu => nu.user_id === u.user_id);
        return updatedUser ? { ...u, is_online: updatedUser.is_online } : u;
      });
      this.pendingUsers = this.pendingUsers.map(u => {
        const updatedUser = users.find(nu => nu.user_id === u.user_id);
        return updatedUser ? { ...u, is_online: updatedUser.is_online } : u;
      });

      if (this.currentChat) {
        const updatedCurrentChat = users.find(u => u.user_id === this.currentChat!.user_id);
        if (updatedCurrentChat) {
          this.currentChat = { ...this.currentChat, is_online: updatedCurrentChat.is_online };
        }
      }
    });

    this.messageSub = this.webSocketService.messages$.subscribe((newMessage: MessagesResponse | null) => {
      if (newMessage) {
        const currentUserId = this.authService.getCurrentUser()?.user_id;
        if (
          this.currentChat &&
          this.activeTab === 'active' &&
          (newMessage.receiver.user_id === this.currentChat.user_id ||
            newMessage.sender.user_id === this.currentChat.user_id)
        ) {
          this.messages.push(newMessage);
        } else if (
          newMessage.approval_status === 'PENDING' &&
          newMessage.receiver.user_id === currentUserId
        ) {
          if (this.activeTab === 'pending' && this.currentChat?.user_id === newMessage.sender.user_id) {
            this.messages.push(newMessage);
          }
          const sender = newMessage.sender;
          if (!this.pendingUsers.some(u => u.user_id === sender.user_id)) {
            this.pendingUsers.push(sender);
          }
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.webSocketService.disconnect();
    this.messageSub?.unsubscribe();
    this.userStatusSub?.unsubscribe();
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  loadPendingConversations(): void {
    this.messageService.getPendingConversations().subscribe({
      next: (users) => {
        this.pendingUsers = users;
        // Cập nhật trạng thái online cho pendingUsers
        const userIds = this.pendingUsers.map(u => u.user_id);
        if (userIds.length > 0) {
          this.userService.getUsersStatus(userIds).subscribe({
            next: (onlineUsers) => {
              this.pendingUsers = this.pendingUsers.map(u => {
                const onlineUser = onlineUsers.find(ou => ou.user_id === u.user_id);
                return onlineUser ? { ...u, is_online: onlineUser.is_online } : { ...u, is_online: false };
              });
            },
            error: (err) => {
              console.error('Lỗi khi lấy trạng thái người dùng pending:', err);
              this.pendingUsers = this.pendingUsers.map(u => ({ ...u, is_online: false }));
            }
          });
        }
      },
      error: (err) => {
        console.error('Lỗi khi lấy danh sách hội thoại PENDING:', err);
      }
    });
  }

  switchTab(tab: 'active' | 'pending'): void {
    this.activeTab = tab;
    this.currentChat = null;
    this.messages = [];
    if (tab === 'pending' && this.pendingUsers.length > 0) {
      this.selectChat(this.pendingUsers[0], 'pending');
    } else if (tab === 'active' && this.users.length > 0) {
      this.selectChat(this.users[0], 'active');
    }
  }

  loadUserForChat(email: string): void {
    const existingUser = this.users.find(u => u.email === email) || this.pendingUsers.find(u => u.email === email);
    if (existingUser) {
      this.selectChat(existingUser, this.users.includes(existingUser) ? 'active' : 'pending');
    } else {
      this.userService.getProfile(email).subscribe({
        next: (user: User) => {
          if (!this.users.some(u => u.user_id === user.user_id)) {
            this.users.push(user);
          }
          this.selectChat(user, 'active');
        },
        error: (err) => {
          console.error('Lỗi khi lấy thông tin người dùng để nhắn tin:', err);
        }
      });
    }
  }

  selectChat(user: User, tab: 'active' | 'pending'): void {
    console.log(`Selecting chat with user_id=${user.user_id}, current is_online=${user.is_online}`);
    this.currentChat = { ...user }; // Sử dụng trạng thái is_online hiện có từ user
    this.activeTab = tab;

    // Kiểm tra trạng thái từ WebSocketService chỉ khi cần
    const currentStatus = this.webSocketService.getUserStatus(user.user_id);
    if (currentStatus !== undefined && currentStatus !== user.is_online) {
      console.log(`Updating currentChat status from WebSocket: user_id=${user.user_id}, is_online=${currentStatus}`);
      this.currentChat = { ...this.currentChat, is_online: currentStatus };
    }

    // Chỉ gọi API nếu trạng thái vẫn chưa rõ
    if (this.currentChat.is_online === undefined || this.currentChat.is_online === false) {
      this.userService.getUsersStatus([user.user_id]).subscribe({
        next: (onlineUsers) => {
          const onlineUser = onlineUsers.find(ou => ou.user_id === user.user_id);
          const isOnline = onlineUser ? onlineUser.is_online : false;
          console.log(`API status for user_id=${user.user_id}: is_online=${isOnline}`);
          this.currentChat = { ...this.currentChat, is_online: isOnline };
        },
        error: (err) => {
          console.error('Lỗi khi lấy trạng thái người dùng:', err);
          this.currentChat = { ...this.currentChat, is_online: false };
        }
      });
    }

    if (tab === 'active') {
      this.messageService.getMessages(user.user_id).subscribe({
        next: (messages) => {
          this.messages = messages;
        },
        error: (err) => {
          console.error('Lỗi khi lấy tin nhắn:', err);
        }
      });
    } else {
      this.messageService.getPendingMessages(user.user_id).subscribe({
        next: (messages) => {
          const currentUserId = this.authService.getCurrentUser()?.user_id;
          this.messages = messages.filter(msg => msg.receiver.user_id === currentUserId);
        },
        error: (err) => {
          console.error('Lỗi khi lấy tin nhắn PENDING:', err);
        }
      });
    }
  }

  sendMessage(): void {
    if (this.newMessage.trim() && this.currentChat) {
      const message: Messages = {
        receiverId: this.currentChat.user_id,
        content: this.newMessage
      };

      this.messageService.sendMessage(message).subscribe({
        next: (response) => {
          this.messages.push(response);
          this.newMessage = '';
          if (!this.users.some(u => u.user_id === this.currentChat!.user_id)) {
            this.users.push(this.currentChat!);
          }
        },
        error: (err) => {
          console.error('Lỗi khi gửi tin nhắn:', err);
        }
      });
    }
  }

  acceptPendingConversation(senderId: number): void {
    this.messageService.acceptPendingConversation(senderId).subscribe({
      next: () => {
        if (this.currentChat) {
          const sender = this.currentChat;
          this.pendingUsers = this.pendingUsers.filter(u => u.user_id !== senderId);
          if (!this.users.some(u => u.user_id === sender.user_id)) {
            this.users.push(sender);
          }
          this.selectChat(sender, 'active');
        }
      },
      error: (err) => {
        console.error('Lỗi khi chấp nhận cuộc hội thoại:', err);
      }
    });
  }

  rejectPendingConversation(senderId: number): void {
    this.messageService.rejectPendingConversation(senderId).subscribe({
      next: () => {
        if (this.currentChat) {
          this.pendingUsers = this.pendingUsers.filter(u => u.user_id !== senderId);
          this.messages = [];
          if (this.pendingUsers.length > 0) {
            this.selectChat(this.pendingUsers[0], 'pending');
          } else {
            this.currentChat = null;
          }
        }
      },
      error: (err) => {
        console.error('Lỗi khi từ chối cuộc hội thoại:', err);
      }
    });
  }

  private scrollToBottom(): void {
    if (this.messagesContainer) {
      const container = this.messagesContainer.nativeElement;
      container.scrollTop = container.scrollHeight;
    }
  }
}