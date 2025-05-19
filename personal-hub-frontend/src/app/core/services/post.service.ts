import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private apiUrl = `${API_CONFIG.baseUrl}/post`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  createPost(formData: FormData): Observable<any> {
    return this.http.post(
      this.apiUrl, 
      formData, 
      { headers: this.authService.getAuthHeaders() }
    );
  }

  getAllPosts(page: number = 0, size: number = 20, sortBy: string = 'createdAt', sortDir: string = 'desc'): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    
    return this.http.get(this.apiUrl, { params });
  }

  getPostById(postId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${postId}`);
  }

  getPostByTitle(title: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/title/${encodeURIComponent(title)}`);
  }

  getPostBySlug(slug: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/slug/${slug}`);
  }

  getPostsByCategory(categorySlug: string, page: number = 0, size: number = 20): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get(`${this.apiUrl}/category/${categorySlug}`, { params });
  }

  getPostsByTag(tagName: string, page: number = 0, size: number = 20): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get(`${this.apiUrl}/tag/${tagName}`, { params });
  }

  getPostByUser(userId: number): Observable<any[]> {
        debugger;
        return this.http.get<any[]>(
          `${this.apiUrl}/user/${userId}`
        );
      }
}