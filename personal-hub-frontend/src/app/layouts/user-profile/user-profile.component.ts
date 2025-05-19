import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { PostService } from 'src/app/core/services/post.service';
import { FollowService } from 'src/app/core/services/follow.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { UserService } from 'src/app/core/services/user.service';
import { PostResponse } from 'src/app/core/models/post.model';
import { FollowResponse } from 'src/app/core/models/follow.model';
import { User } from 'src/app/core/models/user.model';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {
  user: User | null = null;
  userPosts: PostResponse[] = [];
  userFollowers: FollowResponse[] = [];
  userFollowing: FollowResponse[] = [];
  activeTab: string = 'posts';
  isLoading: boolean = false;
  isCurrentUser: boolean = false; // Kiểm tra xem có phải profile của current user
  isFollowing: boolean = false; // Kiểm tra trạng thái follow

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private postService: PostService,
    private followService: FollowService,
    private authService: AuthService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const email = params.get('email');
      if (email) {
        this.loadUserProfile(email);
      } else {
        console.error('No email provided in route');
        this.isLoading = false;
        this.router.navigate(['/']);
      }
    });
  }

  loadUserProfile(email: string): void {
    this.isLoading = true;

    // Check if the email belongs to the current user
    const currentUser = this.authService.getCurrentUser();
    this.isCurrentUser = currentUser?.email === email;

    if (this.isCurrentUser) {
      // Use getCurrentUserProfile to call /api/user (getUserById)
      this.userService.getCurrentUserProfile().subscribe({
        next: (user: User) => {
          this.user = user;
          this.loadUserData(user.user_id);
        },
        error: (err) => {
          console.error('Error fetching current user profile:', err);
          this.isLoading = false;
          this.router.navigate(['/']);
        }
      });
    } else {
      // Fetch user profile by email for other users
      this.userService.getProfile(email).subscribe({
        next: (user: User) => {
          this.user = user;
          this.loadUserData(user.user_id);
          // Kiểm tra trạng thái follow
          if (currentUser) {
            this.checkFollowingStatus(user.user_id);
          }
        },
        error: (err) => {
          console.error('Error fetching user profile:', err);
          this.isLoading = false;
          this.router.navigate(['/']);
        }
      });
    }
  }

  loadUserData(userId: number): void {
    // Fetch user's posts
    this.postService.getPostByUser(userId).subscribe({
      next: (posts) => {
        this.userPosts = posts;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching posts:', err);
        this.isLoading = false;
      }
    });

    // Fetch followers
    this.followService.getAllFollowers(userId).subscribe({
      next: (followers) => {
        this.userFollowers = followers.filter(f => !f.deleted_at);
      },
      error: (err) => {
        console.error('Error fetching followers:', err);
      }
    });

    // Fetch following
    this.followService.getAllFollowing(userId).subscribe({
      next: (following) => {
        this.userFollowing = following.filter(f => !f.deleted_at);
      },
      error: (err) => {
        console.error('Error fetching following:', err);
      }
    });
  }

  // Kiểm tra trạng thái follow
  checkFollowingStatus(userId: number): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.followService.getAllFollowers(userId).subscribe({
        next: (followers) => {
          this.isFollowing = followers.some(follow => 
            follow.follower_user?.user_id === currentUser.user_id && !follow.deleted_at
          );
        },
        error: (err) => {
          console.error('Error checking follow status:', err);
        }
      });
    }
  }

  // Xử lý follow/unfollow
  toggleFollow(): void {
    if (!this.user) return;
    this.isLoading = true;

    if (this.isFollowing) {
      this.followService.unfollowUser(this.user.user_id).subscribe({
        next: () => {
          this.isFollowing = false;
          this.userFollowers = this.userFollowers.filter(
            follow => follow.follower_user?.user_id !== this.authService.getCurrentUser()?.user_id
          );
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error unfollowing user:', err);
          this.isLoading = false;
        }
      });
    } else {
      this.followService.followUser(this.user.user_id).subscribe({
        next: (follow) => {
          this.isFollowing = true;
          this.userFollowers.push(follow);
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error following user:', err);
          this.isLoading = false;
        }
      });
    }
  }

  navigateToProfile(): void {
    this.router.navigate(['/profile/edit']);
  }

  changeTab(tab: string): void {
    this.activeTab = tab;
  }

  navigateToUserProfile(email: string | undefined): void {
    if (email) {
      this.router.navigate(['/profile', email]);
    } else {
      console.error('User email not available');
    }
  }
}