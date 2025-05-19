import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';
import { AuthService } from './auth.service';
import { Like, LikeResponse } from '../models/like.model';

@Injectable({
    providedIn: 'root'
  })
  export class LikeService {
    private apiUrl = `${API_CONFIG.baseUrl}/likes`;
  
    constructor(
      private http: HttpClient,
      private authService: AuthService
    ) {}
  
    likePost(postId: number): Observable<LikeResponse> {
      debugger;
      const likeRequest: Like = { postId };
      return this.http.post<LikeResponse>(
        this.apiUrl, 
        likeRequest, 
        { headers: this.authService.getAuthHeaders() }
      );
    }
  
    unlikePost(postId: number): Observable<LikeResponse> {
      debugger;
      return this.http.delete<LikeResponse>(
        `${this.apiUrl}/${postId}`, 
        { headers: this.authService.getAuthHeaders() }
      );
    }
  
    getLikesByPostId(postId: number): Observable<LikeResponse[]> {
      debugger;
      return this.http.get<LikeResponse[]>(
        `${this.apiUrl}/post/${postId}`
      );
    }
  
    getLikeCountByPostId(postId: number): Observable<number> {
      return this.http.get<number>(
        `${this.apiUrl}/post/${postId}/count`
      );
    }
  
    isLikedByCurrentUser(likes: LikeResponse[]): boolean {
      const currentUser = this.authService.getCurrentUser();
      console.log('Current user in LikeService:', currentUser); // Debug
      if (!currentUser || !likes || !currentUser.user_id) {
        console.error('Invalid user or likes data:', { currentUser, likes });
        return false;
      }
      return likes.some(like => like.user_id === currentUser.user_id && !like.deleted_at);
    }
  }