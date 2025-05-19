import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';
import { AuthService } from './auth.service';
import { Comment, CommentResponse } from '../models/comment.model';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = `${API_CONFIG.baseUrl}/comments`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  createComment(comment: Comment): Observable<CommentResponse> {
    return this.http.post<CommentResponse>(
      this.apiUrl, 
      comment, 
      { headers: this.authService.getAuthHeaders() }
    );
  }

  updateComment(commentId: number, comment: Comment): Observable<CommentResponse> {
    return this.http.put<CommentResponse>(
      `${this.apiUrl}/${commentId}`, 
      comment, 
      { headers: this.authService.getAuthHeaders() }
    );
  }

  deleteComment(commentId: number): Observable<CommentResponse> {
    return this.http.delete<CommentResponse>(
      `${this.apiUrl}/${commentId}`, 
      { headers: this.authService.getAuthHeaders() }
    );
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  isCommentAuthor(comment: CommentResponse, postAuthorUsername?: string): boolean {
    const currentUser = this.authService.getCurrentUser();
    const isCommentAuthor = currentUser && comment.user && currentUser.username === comment.user.username;
    const isPostAuthor = currentUser && postAuthorUsername && currentUser.username === postAuthorUsername;
    return isCommentAuthor || isPostAuthor;
  }
}