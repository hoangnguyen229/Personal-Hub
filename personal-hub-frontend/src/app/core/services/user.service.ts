import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';
import { AuthService } from './auth.service';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${API_CONFIG.baseUrl}/user`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getProfile(email: string): Observable<User> {
    return this.http.get<User>(
      `${this.apiUrl}/${email}`,
      { headers: this.authService.getAuthHeaders() }
    );
  }
  
  getCurrentUserProfile(): Observable<User> {
    return this.http.get<User>(
      `${this.apiUrl}`,
      { headers: this.authService.getAuthHeaders() }
    );
  }

  getAllUsers(): Observable<any> {
    return this.http.get(
      `${this.apiUrl}/getAll`, 
      { headers: this.authService.getAuthHeaders() }
    );
  }

  updateProfile(formData: FormData): Observable<any> {
    return this.http.put(
      this.apiUrl, 
      formData, 
      { headers: this.authService.getAuthHeaders() }
    );
  }

  getOnlineUsers(): Observable<any> {
    return this.http.get(
      `${this.apiUrl}/online`, 
      { headers: this.authService.getAuthHeaders() }
    );
  }

  getUsersStatus(userIds: number[]): Observable<User[]> {
    return this.http.post<User[]>(
      `${this.apiUrl}/status`, 
      userIds, 
      { headers: this.authService.getAuthHeaders() }
    );
  }
}