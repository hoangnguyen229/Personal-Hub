import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Post } from 'src/app/core/models/post.model';
import { faLinkedin, faGithub, faFacebook } from '@fortawesome/free-brands-svg-icons';
import { faComment, faThumbsUp } from '@fortawesome/free-solid-svg-icons';
import { PostService } from 'src/app/core/services/post.service';
import { DateUtilService } from 'src/app/core/services/date-util.service';
import { EsSearchService } from 'src/app/core/services/es-search.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  faLinkedin = faLinkedin;
  faGithub = faGithub;
  faFacebook = faFacebook;
  faComment = faComment;
  faThumbsUp = faThumbsUp;
  
  blogPosts: Post[] = [];
  loading = false;
  error: string | null = null;
  searchQuery: string | null = null;
  selectedTag: string | null = null;

  // Pagination params
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  
  constructor(
    private postService: PostService,
    private sanitizer: DomSanitizer,
    private dateUtil: DateUtilService,
    private esSearchService: EsSearchService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    // Lắng nghe query parameter 'search'
    this.route.paramMap.subscribe(params => {
      this.searchQuery = params.get('q') || null;
      this.selectedTag = params.get('tag') || null;
      this.currentPage = 0; // Reset page khi có query mới
      this.loadPosts(this.currentPage);
    });
  }

  // dashboard.component.ts
loadPosts(page: number): void {
  this.loading = true;
  this.error = null;
  this.currentPage = page;

  if (this.selectedTag) {
    // Gọi API tìm kiếm theo tag
    this.esSearchService.searchPostsByTag(this.selectedTag, page, this.pageSize)
      .subscribe({
        next: (response) => {
          if (response && response.content) {
            this.blogPosts = this.transformPostsData(response.content);
            this.totalPages = response.total_pages;
            this.totalElements = response.total_elements;
          } else {
            this.blogPosts = [];
          }
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Không tìm thấy bài viết với tag này.';
          this.loading = false;
        }
      });
  } else if (this.searchQuery) {
    // Gọi API tìm kiếm theo tiêu đề
    this.esSearchService.searchPostsByTitle(this.searchQuery, page, this.pageSize)
      .subscribe({
        next: (response) => {
          if (response && response.content) {
            this.blogPosts = this.transformPostsData(response.content);
            this.totalPages = response.total_pages;
            this.totalElements = response.total_elements;
          } else {
            this.blogPosts = [];
          }
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Không tìm thấy bài viết phù hợp.';
          this.loading = false;
        }
      });
  } else {
    // Gọi API lấy tất cả bài post
    this.postService.getAllPosts(page, this.pageSize)
      .subscribe({
        next: (response) => {
          if (response && response.content) {
            this.blogPosts = this.transformPostsData(response.content);
            this.totalPages = response.total_pages;
            this.totalElements = response.total_elements;
          } else {
            this.blogPosts = this.transformPostsData(response);
          }
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Không thể tải bài viết. Vui lòng thử lại sau.';
          this.loading = false;
        }
      });
  }
}

  loadPreviousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadPosts(this.currentPage);
    }
  }

  loadNextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadPosts(this.currentPage);
    }
  }

  transformPostsData(posts: any[]): Post[] {
    if (!posts || !Array.isArray(posts)) {
      return [];
    }

    return posts.map(post => {
      const excerpt = this.generateExcerpt(post.content);
      
      const publishDate = this.dateUtil.formatDate(post.createdAt || post.created_at);
      
      // Get the image URL with improved handling
      const imageUrl = this.extractImageUrl(post);
      
      // Create HTML for image with improved handling
      const imageHtml = this.createSafeImageHtml(imageUrl, post.title);
      
      // Create slug from title if not provided in the API response
      const slug = post.slug;

      const likeCount = post.likes ? post.likes.filter(like => !like.deleted_at).length : 0;
      
      return {
        id: post.postID || post.id,
        title: post.title,
        excerpt: excerpt,
        imageUrl: imageUrl, // Keep original URL for backward compatibility
        imageHtml: imageHtml, // Add sanitized HTML for the image
        publishDate: publishDate,
        commentCount: post.comments?.length || 0,
        slug: slug,
        likes: likeCount,
        author: post.user ? {
          name: post.user.username,
          avatarUrl: post.user.profilePic || post.user.profile_picture || 'assets/images/NguyenVanHoang.jpg'
        } : undefined,
        topic: post.category ? {
          name: post.category.name,
          iconUrl: 'assets/images/NguyenVanHoang.jpg' // Default icon for category
        } : undefined
      };
    });
  }

  // Improved method to extract image URL from post data
  extractImageUrl(post: any): string {
    // Check for images array first
    if (post.images && post.images.length > 0) {
      // Check for both camelCase and snake_case properties
      if (post.images[0].imageUrl) {
        return post.images[0].imageUrl;
      }
      if (post.images[0].image_url) {
        return post.images[0].image_url;
      }
    }
    
    // If no images array or empty array, try to extract from content
    if (post.content) {
      const imgMatch = post.content.match(/<img[^>]+src=["']([^"']+)["'][^>]*>/i);
      if (imgMatch && imgMatch[1]) {
        return imgMatch[1];
      }
      
      // Try to find Cloudinary URLs directly in content
      const cloudinaryMatch = post.content.match(/(https?:\/\/res\.cloudinary\.com\/[^\s<>"']+\.(jpg|jpeg|png|gif|webp))/i);
      if (cloudinaryMatch && cloudinaryMatch[1]) {
        return cloudinaryMatch[1];
      }
    }
    
    // Return default image if nothing found
    return 'assets/images/default-post.jpg';
  }

  // Create safe HTML for the image with proper error handling
  createSafeImageHtml(imageUrl: string, title: string): SafeHtml {
    // Make sure URL is valid
    let safeUrl = imageUrl;
    
    // Check if URL is relative and doesn't start with '/'
    if (safeUrl && !safeUrl.startsWith('http') && !safeUrl.startsWith('/') && !safeUrl.startsWith('assets/')) {
      safeUrl = `https://${safeUrl}`;
    }
    
    // Create HTML with onerror handler to fallback to default image
    const imageHtml = `
      <img 
        src="${safeUrl}" 
        alt="${title || 'Blog post'}" 
        class="post-thumbnail"
        onerror="this.onerror=null; this.src='assets/images/default-post.jpg';"
      >
    `;
    
    return this.sanitizer.bypassSecurityTrustHtml(imageHtml);
  }

  // Generate excerpt from content
  generateExcerpt(content: string): string {
    if (!content) return 'No content available...';
    
    // Remove HTML tags and decode HTML entities
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = content;
    const plainText = tempDiv.textContent || tempDiv.innerText || '';
    
    // Truncate to around 150 characters, but at a word boundary
    if (plainText.length <= 150) return plainText;
    
    const truncated = plainText.substring(0, 150).trim();
    // Find the last space to avoid cutting words
    const lastSpaceIndex = truncated.lastIndexOf(' ');
    
    if (lastSpaceIndex > 100) {
      return truncated.substring(0, lastSpaceIndex) + '...';
    }
    
    return truncated + '...';
  }
}