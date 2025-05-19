import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { faBookmark, faShareAlt, faEllipsisH, faThumbsUp, faComment, faPaperPlane, faEdit, faTrash } from '@fortawesome/free-solid-svg-icons';
import { PostService } from 'src/app/core/services/post.service';
import { DateUtilService } from 'src/app/core/services/date-util.service';
import { CommentService } from 'src/app/core/services/comment.service';
import { Comment, CommentResponse } from 'src/app/core/models/comment.model';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PostResponse, TransformedPost } from 'src/app/core/models/post.model';
import { LikeService } from 'src/app/core/services/like.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { FollowService } from 'src/app/core/services/follow.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-blog-detail',
  templateUrl: './blog-detail.component.html',
  styleUrls: ['./blog-detail.component.css']
})
export class BlogDetailComponent implements OnInit {
  faBookmark = faBookmark;
  faShareAlt = faShareAlt;
  faEllipsisH = faEllipsisH;
  faThumbsUp = faThumbsUp;
  faComment = faComment;
  faPaperPlane = faPaperPlane;
  faEdit = faEdit;
  faTrash = faTrash;
  
  blogPost: TransformedPost = {} as TransformedPost;
  postSlug: string = '';
  postId: number = 0;
  comments: CommentResponse[] = [];
  loading = true;
  error: string | null = null;
  commentForm: FormGroup;
  commentSubmitting = false;
  commentError: string | null = null;
  isLoggedIn = false;
  isLiked = false;
  likeProcessing = false;
  user: any | null = null;
  isFollowing = false;
  followProcessing = false;
  isCurrentUserAuthor = false;
  
  // Add sanitizedContent property to hold the processed content
  sanitizedContent: SafeHtml;

  editCommentForm: FormGroup;
  showDeleteConfirmation = false;
  commentToDelete: CommentResponse | null = null;

  constructor(
    private route: ActivatedRoute,
    private postService: PostService,
    private sanitizer: DomSanitizer,
    private dateUtil: DateUtilService,
    private commentService: CommentService,
    private fb: FormBuilder,
    private likeService: LikeService,
    private authService: AuthService,
    private followService: FollowService,
    private router: Router
  ) {
    this.commentForm = this.fb.group({
      content: ['', [Validators.required, Validators.minLength(2)]]
    });
    
    this.editCommentForm = this.fb.group({
      content: ['', [Validators.required, Validators.minLength(2)]]
    });
  }

  ngOnInit(): void {
    this.isLoggedIn = this.commentService.isLoggedIn();
    
    this.route.paramMap.subscribe(params => {
      const slug = params.get('slug');
      if (slug) {
        this.postSlug = slug;
        this.loadPostBySlug(slug);
      } else {
        this.error = 'Post not found';
        this.loading = false;
      }
    });
  }

  loadPostBySlug(slug: string): void {
    this.loading = true;
    this.error = null;

    this.postService.getPostBySlug(slug).subscribe({
      next: (post) => {
        this.postId = post.id || post.postId;
        this.transformPostData(post);
        this.comments = post.comments || [];
        this.loading = false;
        this.user = this.authService.getCurrentUser();

        debugger;
        if (this.user && this.blogPost.authorId) {
          this.isCurrentUserAuthor = this.user.user_id === this.blogPost.authorId;
        }

        if (this.isLoggedIn && this.blogPost.authorId) {
          this.checkFollowStatus();
        }
      },
      error: (err) => {
        console.error('Error loading post:', err);
        this.error = 'Failed to load the post. Please try again later.';
        this.loading = false;
      }
    });
  }

  transformPostData(post: PostResponse): void {
    const formattedDate = this.dateUtil.formatDate(post.created_at || post.createdAt);

    const tags = post.tags ? post.tags.map(tag => tag.name) : [];

    let imageUrl = 'assets/images/default-post.jpg';
    if (post.images && post.images.length > 0) {
      imageUrl = post.images[0].image_url || post.images[0].imageUrl;
    }

    let processedContent = post.content || 'No content available';
    processedContent = this.processHtmlContent(processedContent);
    this.sanitizedContent = this.sanitizer.bypassSecurityTrustHtml(processedContent);

    const likeCount = post.likes ? post.likes.filter(like => !like.deleted_at).length : 0;

    this.isLiked = this.isLoggedIn ? this.likeService.isLikedByCurrentUser(post.likes) : false;

    this.blogPost = {
      id: post.post_id || post.id,
      title: post.title,
      author: post.user?.username,
      email: post.user?.email,
      publishedIn: post.category?.name,
      publishDate: formattedDate,
      content: processedContent,
      image: imageUrl,
      likes: likeCount,
      comments: post.comments?.length || 0,
      tags: tags,
      authorId: post.user?.user_id,
      authorAvatar: post.user?.profilePic || post.user?.profile_picture,
      authorBio: post.user?.bio
    };
  }

  checkFollowStatus(): void {
  this.user = this.authService.getCurrentUser();
  if (!this.user || !this.blogPost.authorId) {
    console.warn('No current user or author ID available');
    return;
  }
  this.followService.getAllFollowing(this.user.user_id).subscribe({
    next: (following) => {
      this.isFollowing = following.some(f => 
        f.following_user?.user_id === this.blogPost.authorId && !f.deleted_at
      );
    },
    error: (err) => {
      console.error('Error checking follow status:', err);
    }
  });
}

  toggleFollow(): void {
    if (!this.isLoggedIn || !this.blogPost.authorId || this.followProcessing) {
      return;
    }

    this.followProcessing = true;

    if (this.isFollowing) {
      this.followService.unfollowUser(this.blogPost.authorId).subscribe({
        next: () => {
          this.isFollowing = false;
          this.followProcessing = false;
        },
        error: (err) => {
          console.error('Error unfollowing user:', err);
          this.error = 'Failed to unfollow the user. Please try again.';
          this.followProcessing = false;
        }
      });
    } else {
      this.followService.followUser(this.blogPost.authorId).subscribe({
        next: () => {
          this.isFollowing = true;
          this.followProcessing = false;
        },
        error: (err) => {
          console.error('Error following user:', err);
          this.error = 'Failed to follow the user. Please try again.';
          this.followProcessing = false;
        }
      });
    }
  }

  toggleLike(): void {
    debugger;
    if (!this.isLoggedIn) {
      return;
    }

    if (this.likeProcessing) {
      return;
    }

    this.likeProcessing = true;

    if (this.isLiked) {
      this.likeService.unlikePost(this.blogPost.id).subscribe({
        next: () => {
          this.isLiked = false;
          this.blogPost.likes--;
          this.likeProcessing = false;
        },
        error: (err) => {
          this.error = 'Failed to unlike the post. Please try again.';
          this.likeProcessing = false;
        }
      });
    } else {
      this.likeService.likePost(this.blogPost.id).subscribe({
        next: () => {
          this.isLiked = true;
          this.blogPost.likes++;
          this.likeProcessing = false;
        },
        error: (err) => {
          this.error = 'Failed to like the post. Please try again.';
          this.likeProcessing = false;
        }
      });
    }
  }

  processHtmlContent(content: string): string {
    if (!content || typeof content !== 'string') {
      return 'No content available';
    }

    const parser = new DOMParser();
    const doc = parser.parseFromString(content, 'text/html');
    
    const imgElements = doc.querySelectorAll('img');
    imgElements.forEach(img => {
      img.classList.add('content-image');
      
      const src = img.getAttribute('src');
      if (src) {
        if (!src.startsWith('http') && !src.startsWith('/')) {
          img.setAttribute('src', `https://${src}`);
        }
      }
      
      if (!img.getAttribute('alt')) {
        img.setAttribute('alt', 'Blog image');
      }
    });

    const captionImgs = doc.querySelectorAll('.e-img-caption img, .e-rte-image');
    captionImgs.forEach(img => {
      img.classList.add('content-image');
      
      const src = img.getAttribute('src');
      if (src) {
        if (!src.startsWith('http') && !src.startsWith('/')) {
          img.setAttribute('src', `https://${src}`);
        }
      }
      
      if (!img.getAttribute('alt')) {
        img.setAttribute('alt', 'Blog image');
      }
    });

    const captionElements = doc.querySelectorAll('.e-img-caption');
    captionElements.forEach(caption => {
      caption.classList.add('image-container');
    });

    const cloudinaryRegex = /(https?:\/\/res\.cloudinary\.com\/[^\s<>"']+\.(jpg|jpeg|png|gif|webp))/gi;
    
    const textNodes = this.getTextNodes(doc.body);
    textNodes.forEach(node => {
      if (node.textContent && cloudinaryRegex.test(node.textContent)) {
        const newHtml = node.textContent.replace(cloudinaryRegex, (match) => {
          return `<img src="${match}" class="content-image" alt="Blog image" />`;
        });
        
        const tempDiv = doc.createElement('div');
        tempDiv.innerHTML = newHtml;
        
        const parent = node.parentNode;
        while (tempDiv.firstChild) {
          parent.insertBefore(tempDiv.firstChild, node);
        }
        parent.removeChild(node);
      }
    });

    return doc.body.innerHTML;
  }

  getTextNodes(element: Node): Node[] {
    const textNodes: Node[] = [];
    
    const walk = (node: Node) => {
      if (node.nodeType === Node.TEXT_NODE && node.textContent && node.textContent.trim()) {
        textNodes.push(node);
      } else {
        node.childNodes.forEach(child => walk(child));
      }
    };
    
    walk(element);
    return textNodes;
  }

  submitComment(): void {
    debugger;
    if (this.commentForm.invalid) {
      return;
    }
    
    const content = this.commentForm.value.content;
    const comment: Comment = {
      content: content,
      postId: this.blogPost.id
    };
    
    this.commentSubmitting = true;
    this.commentError = null;
    
    this.commentService.createComment(comment).subscribe({
      next: (response) => {
        this.comments.unshift(response);
        this.blogPost.comments = this.comments.length;
        this.commentForm.reset();
        this.commentSubmitting = false;
      },
      error: (err) => {
        console.error('Error submitting comment:', err);
        this.commentError = 'Failed to submit your comment. Please try again.';
        this.commentSubmitting = false;
      }
    });
  }

  formatCommentDate(dateString: string): string {
    return this.dateUtil.getRelativeTimeFromNow(dateString);
  }

  toggleCommentActions(comment: any): void {
    this.comments.forEach(c => {
      if (c !== comment) {
        c.showActions = false;
      }
    });
    comment.showActions = !comment.showActions;
  }

  canEditComment(comment: CommentResponse): boolean {
    return this.commentService.isLoggedIn() && 
           this.commentService.isCommentAuthor(comment, this.blogPost?.author);
  }

  startEditComment(comment: CommentResponse): void {
    comment.showActions = false;
    comment.isEditing = true;
    
    this.editCommentForm.setValue({
      content: comment.content
    });
  }

  cancelEditComment(): void {
    const editingComment = this.comments.find(c => c.isEditing);
    if (editingComment) {
      editingComment.isEditing = false;
    }
    this.editCommentForm.reset();
  }

  submitEditComment(comment: CommentResponse): void {
    if (this.editCommentForm.invalid) {
      return;
    }
    
    const content = this.editCommentForm.value.content;
    
    const updatedComment: Comment = {
      content: content,
      postId: this.blogPost.id
    };
    
    this.commentSubmitting = true;
    
    this.commentService.updateComment(comment.comment_id, updatedComment).subscribe({
      next: (response) => {
        const index = this.comments.findIndex(c => c.comment_id === comment.comment_id);
        if (index !== -1) {
          this.comments[index] = { 
            ...response, 
            isEditing: false 
          };
        }
        this.commentSubmitting = false;
      },
      error: (err) => {
        console.error('Error updating comment:', err);
        this.commentError = 'Failed to update your comment. Please try again.';
        this.commentSubmitting = false;
      }
    });
  }

  confirmDeleteComment(comment: CommentResponse): void {
    this.commentToDelete = comment;
    this.showDeleteConfirmation = true;
    comment.showActions = false;
  }

  cancelDeleteComment(): void {
    this.commentToDelete = null;
    this.showDeleteConfirmation = false;
  }

  deleteComment(): void {
    if (!this.commentToDelete) {
      return;
    }
    
    this.commentService.deleteComment(this.commentToDelete.comment_id).subscribe({
      next: (response) => {
        const index = this.comments.findIndex(c => c.comment_id === this.commentToDelete?.comment_id);
        if (index !== -1) {
          this.comments[index] = {
            ...response
          };
        }
        
        this.showDeleteConfirmation = false;
        this.commentToDelete = null;
      },
      error: (err) => {
        console.error('Error deleting comment:', err);
        this.commentError = 'Failed to delete your comment. Please try again.';
        this.showDeleteConfirmation = false;
        this.commentToDelete = null;
      }
    });
  }

  scrollToCommentForm(): void {
    const commentFormSection = document.getElementById('comment-form-section');
    if (commentFormSection) {
      commentFormSection.scrollIntoView({ behavior: 'smooth'});
    }
  }

  navigateToUserProfile() {
    this.router.navigate(['/profile', this.blogPost.email]);
  }
}