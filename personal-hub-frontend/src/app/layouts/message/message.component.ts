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
    users: User[] = []; // Danh sách người dùng có hội thoại ACTIVE
    pendingUsers: User[] = []; // Danh sách người dùng có hội thoại PENDING
    currentChat: User | null = null; // Người dùng đang được chọn để chat
    messages: MessagesResponse[] = []; // Danh sách tin nhắn (ACTIVE hoặc PENDING)
    newMessage: string = '';
    activeTab: 'active' | 'pending' = 'active';
    private messageSub: Subscription | undefined;

    @ViewChild('messagesContainer') messagesContainer!: ElementRef;

    constructor(
        private messageService: MessageService,
        private webSocketService: WebSocketService,
        private authService: AuthService,
        private userService: UserService,
        private route: ActivatedRoute
    ) {}

    ngOnInit(): void {
      // Load danh sách hội thoại ACTIVE
      this.messageService.getConversations().subscribe({
          next: (conversations) => {
              this.users = conversations;
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
              console.error('Lỗi khi lấy danh sách hội thoại:', err);
          }
      });

      // Load danh sách người dùng có hội thoại PENDING
      this.loadPendingConversations();

      // Lắng nghe tin nhắn WebSocket
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
                  newMessage.receiver.user_id === currentUserId // Chỉ thêm nếu người dùng hiện tại là người nhận
              ) {
                  // Thêm vào danh sách tin nhắn PENDING
                  if (this.activeTab === 'pending' && this.currentChat?.user_id === newMessage.sender.user_id) {
                      this.messages.push(newMessage);
                  }
                  // Cập nhật danh sách pendingUsers nếu cần
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
    }

    ngAfterViewChecked(): void {
        this.scrollToBottom();
    }

    loadPendingConversations(): void {
    this.messageService.getPendingConversations().subscribe({
        next: (users) => {
            const currentUserId = this.authService.getCurrentUser()?.user_id;
            this.pendingUsers = users.filter(user => {
                return new Promise<boolean>((resolve) => {
                    this.messageService.getPendingMessages(user.user_id).subscribe({
                        next: (messages) => {
                            const hasPendingMessages = messages.some(
                                msg => msg.receiver.user_id === currentUserId && msg.sender.user_id === user.user_id
                            );
                            resolve(hasPendingMessages);
                        },
                        error: () => resolve(false)
                    });
                });
            });
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
      this.currentChat = user;
      this.activeTab = tab;
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
                  // Chỉ hiển thị tin nhắn PENDING mà người dùng hiện tại là người nhận
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
          // Thêm tin nhắn vào danh sách hiển thị ngay lập tức
          this.messages.push(response);
          // Lưu tin nhắn vào pendingMessagesSent nếu nó ở trạng thái pending
          this.newMessage = '';
          // Thêm người nhận vào danh sách users nếu chưa có
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