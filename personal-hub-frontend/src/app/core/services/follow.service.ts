import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';
import { AuthService } from './auth.service';
import { Follow, FollowResponse } from '../models/follow.model';

@Injectable({
    providedIn: 'root'
})
export class FollowService {
    private apiUrl = `${API_CONFIG.baseUrl}/follows`;

    constructor(
        private http: HttpClient,
        private authService: AuthService
    ) {}

    followUser(followingId: number): Observable<FollowResponse> {
        const followRequest: Follow = { followingID: followingId };
        return this.http.post<FollowResponse>(
            this.apiUrl,
            followRequest,
            { headers: this.authService.getAuthHeaders() }
        );
    }

    unfollowUser(followingId: number): Observable<FollowResponse> {
        return this.http.delete<FollowResponse>(
            `${this.apiUrl}/${followingId}`,
            { headers: this.authService.getAuthHeaders() }
        );
    }

    getAllFollowers(userId: number): Observable<FollowResponse[]> {
        return this.http.get<FollowResponse[]>(
            `${this.apiUrl}/followers/${userId}`,
            { headers: this.authService.getAuthHeaders() }
        );
    }

    getAllFollowing(userId: number): Observable<FollowResponse[]> {
        return this.http.get<FollowResponse[]>(
            `${this.apiUrl}/following/${userId}`,
            { headers: this.authService.getAuthHeaders() }
        );
    }

    // isFollowing(follows: FollowResponse[]): boolean {
    //     const currentUser = this.authService.getCurrentUser();
    //     console.log('Current user in FollowService:', currentUser); // Debug
    //     if (!currentUser || !follows || !currentUser.user_id) {
    //         console.error('Invalid user or follows data:', { currentUser, follows });
    //         return false;
    //     }
    //     return follows.some(follow => 
    //         follow.follower_user?.user_id === currentUser.user_id && !follow.deleted_at
    //     );
    // }
}