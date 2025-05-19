import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ImageService {
  private apiUrl = `${API_CONFIG.baseUrl}/image`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  uploadImage(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('images', file);
    
    return this.http.post(
      this.apiUrl, 
      formData, 
      { headers: this.authService.getAuthHeaders() }
    );
  }
}