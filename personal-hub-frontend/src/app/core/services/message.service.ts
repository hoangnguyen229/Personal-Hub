import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';
import { AuthService } from './auth.service';
import { Messages, MessagesResponse } from '../models/messages.model';
import { User } from '../models/user.model';

@Injectable({
    providedIn: 'root'
  })
  export class MessageService {
    private apiUrl = `${API_CONFIG.baseUrl}/messages`;
  
    constructor(
      private http: HttpClient,
      private authService: AuthService
    ) {}

    getMessages(recieverId: number) : Observable<MessagesResponse[]>{
      return this.http.get<MessagesResponse[]>(
        `${this.apiUrl}/${recieverId}`,
        { headers: this.authService.getAuthHeaders() }
      );
    }

    sendMessage(message: Messages) : Observable<MessagesResponse>{
      return this.http.post<MessagesResponse>(
        this.apiUrl,
        message,
        { headers: this.authService.getAuthHeaders() }
      );
    }

    getConversations(): Observable<User[]> {
      return this.http.get<User[]>(
        `${this.apiUrl}/conversations`,
        { headers: this.authService.getAuthHeaders() }
      );
    }

    getPendingMessages(senderId: number): Observable<MessagesResponse[]> {
      return this.http.get<MessagesResponse[]>(
        `${this.apiUrl}/pending/${senderId}`,
        { headers: this.authService.getAuthHeaders() }
      );
    }

    getPendingConversations(): Observable<User[]> {
      return this.http.get<User[]>(
        `${this.apiUrl}/pending-conversations`,
        { headers: this.authService.getAuthHeaders() }
      );
    }

    acceptPendingConversation(senderId: number): Observable<MessagesResponse> {
      return this.http.post<MessagesResponse>(
        `${this.apiUrl}/accept-conversation/${senderId}`,
        {},
        { headers: this.authService.getAuthHeaders() }
      );
    }

    rejectPendingConversation(senderId: number): Observable<MessagesResponse> {
      return this.http.post<MessagesResponse>(
        `${this.apiUrl}/reject-conversation/${senderId}`,
        {},
        { headers: this.authService.getAuthHeaders() }
      );
    }
  }