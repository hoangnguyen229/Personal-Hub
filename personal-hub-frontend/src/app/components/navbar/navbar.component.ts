import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from 'src/app/core/services/auth.service';
import { Subject, Subscription } from 'rxjs';
import { NotificationService } from 'src/app/core/services/notification.service';
import { EsSearchService } from 'src/app/core/services/es-search.service';
import { debounceTime, switchMap, distinctUntilChanged } from 'rxjs/operators';
import { PostService } from 'src/app/core/services/post.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  isMenuCollapsed = true;
  isDropdownOpen = false;
  currentUser: any = null;
  unreadNotificationCount = 0;
  searchQuery = '';
  tagSuggestions: string[] = [];
  titleSuggestions: string[] = [];
  showSuggestions = false;
  
  private userSubscription: Subscription | null = null;
  private notificationSubscription: Subscription | null = null;
  private searchSubject = new Subject<string>();
  
  constructor(
    private router: Router, 
    private authService: AuthService,
    private toastrService: ToastrService,
    private notificationService: NotificationService,
    private esSearchService: EsSearchService,
    private postService: PostService
  ) { }

  ngOnInit(): void {
    this.userSubscription = this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      
      if (user) {
        // Khởi tạo dữ liệu thông báo
        this.loadNotifications();
        
        // Đăng ký lắng nghe thông báo mới
        this.notificationSubscription = this.notificationService.notifications$.subscribe(notifications => {
          this.unreadNotificationCount = notifications.filter(n => !n.is_read).length;
        });
      } else {
        // Ngắt kết nối WebSocket nếu người dùng đăng xuất
        this.unreadNotificationCount = 0;
      }

      this.searchSubject.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(query => {
          if(query.startsWith('#') && query.length > 1) {
            const prefix = query.substring(1);
            return this.esSearchService.autocompleteTags(prefix);
          } else if (query.length > 0){
            return this.esSearchService.autocompleteTitles(query);
          }
          return [];
        })
      ).subscribe(suggestions => {
        if(this.searchQuery.startsWith('#')){
          this.tagSuggestions = suggestions;
          this.titleSuggestions = [];
        } else {
          this.titleSuggestions = suggestions;
          this.tagSuggestions = [];
        }
        this.showSuggestions = suggestions.length > 0 && this.searchQuery.length > 0;
      })
    });
  }
  
  ngOnDestroy(): void {
    if (this.userSubscription) {
      this.userSubscription.unsubscribe();
    }
    
    if (this.notificationSubscription) {
      this.notificationSubscription.unsubscribe();
    }
  }

  onSearchInputChange(query: string): void{
    this.searchQuery = query;
    this.searchSubject.next(query);
  }

  onSearch(): void {
  if (this.searchQuery) {
    if (this.searchQuery.startsWith('#')) {
      const tag = this.searchQuery.substring(1);
      this.selectTag(tag);
    } else {
      debugger;
      this.router.navigate(['/search', this.searchQuery]);
      this.showSuggestions = false;
      this.searchQuery = '';
    }
  }
}

onKeydown(event: KeyboardEvent): void {
  if (event.key === 'Enter') {
    this.showSuggestions = false;
    this.onSearch();
  }
}

  selectTag(tag: string): void {
    this.searchQuery = `#${tag}`;
    this.tagSuggestions = [];
    this.titleSuggestions = [];
    this.showSuggestions = false;
    this.router.navigate(['/posts/tag', tag]);
  }

  selectTitle(title: string): void {
    this.searchQuery = title;
    this.tagSuggestions = [];
    this.titleSuggestions = [];
    this.showSuggestions = false;
    this.postService.getPostByTitle(title).subscribe({
      next: (post) => {
        debugger;
        this.router.navigate(['/blog', post.slug]);
      },
      error: (err) => {
        this.toastrService.error('Search failed. Please try again.');
      }
    });
  }

  hideSuggestions(): void {
    setTimeout(() => {
      this.showSuggestions = false;
    }, 200);
  }
  
  loadNotifications(): void {
    this.notificationService.getNotifications().subscribe({
      next: (data) => {
        this.notificationService.updateNotifications(data);
        this.unreadNotificationCount = data.filter(n => !n.is_read).length;
      },
      error: (err) => {
        // console.error('Error wh', err);
      }
    });
  }
  
  toggleMenu() {
    this.isMenuCollapsed = !this.isMenuCollapsed;
  }

  toggleDropdown(event: Event) {
    event.stopPropagation();
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  closeDropdown() {
    this.isDropdownOpen = false;
  }

  redirectEditor() {
    if (!this.authService.isLoggedIn()) {
      this.toastrService.error('You must be logged in to create a post');
      this.router.navigate(['/auth/login']);
      return;
    } else {
      this.router.navigate(['/editor']);
    }
  }

  navigateToProfile() {
    this.router.navigate(['/profile', this.currentUser.email]);
    this.closeDropdown();
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  logout() {
    this.authService.logout();
    this.toastrService.success('Logged out successfully');
    this.router.navigate(['/']);
    this.closeDropdown();
  }

  getProfileImageUrl(): string {
    if (!this.currentUser || !this.currentUser.profile_picture) {
      return 'assets/images/default-avatar.jpg';
    }
    
    // Add cache-busting query parameter
    return `${this.currentUser.profile_picture}?t=${new Date().getTime()}`;
  }
}