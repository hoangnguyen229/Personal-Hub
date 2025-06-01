import { Component, OnDestroy, OnInit, Renderer2 } from '@angular/core';
import { Subscription } from 'rxjs';
import { WebSocketService } from '../core/services/websocket.service';
import { AuthService } from '../core/services/auth.service';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-main-layout',
  template: `
    <app-navbar class="fixed-navbar"></app-navbar>
    <div class="main-content">
      <router-outlet></router-outlet>
    </div>
    <app-footer *ngIf="!isMessagesPage"></app-footer>
  `,
  styles: [`
    .fixed-navbar {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      height: 60px;
      z-index: 1000;
    }

    .main-content {
      margin-top: 60px;
      padding: 20px;
      min-height: calc(100vh - 60px);
    }

    app-footer {
      height: 60px;
    }

    body.no-scroll {
      overflow: hidden;
      height: 100vh;
    }
  `]
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  private authSubscription: Subscription | null = null;
  private routerSubscription: Subscription | null = null;
  isMessagesPage = false;

  constructor(
    private webSocketService: WebSocketService,
    private authService: AuthService,
    private router: Router,
    private renderer: Renderer2
  ) {}

  ngOnInit(): void {
    // 1. Xử lý WebSocket theo trạng thái đăng nhập
    this.authSubscription = this.authService.currentUser$.subscribe(user => {
      if (user && this.authService.isLoggedIn) {
        this.webSocketService.connect();
      } else {
        this.webSocketService.disconnect();
      }
    });

    // 2. Xử lý URL hiện tại NGAY LẬP TỨC khi load trang (fix reload /messages)
    this.updatePageState(this.router.url);

    // 3. Lắng nghe thay đổi router
    this.routerSubscription = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.updatePageState(event.urlAfterRedirects);
      });
  }

  private updatePageState(url: string) {
    this.isMessagesPage = url.startsWith('/messages');
    if (this.isMessagesPage) {
      this.renderer.addClass(document.body, 'no-scroll');
    } else {
      this.renderer.removeClass(document.body, 'no-scroll');
    }
  }

  ngOnDestroy(): void {
    this.authSubscription?.unsubscribe();
    this.routerSubscription?.unsubscribe();
    this.webSocketService.disconnect();
    this.renderer.removeClass(document.body, 'no-scroll');
  }
}
