import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = `${API_CONFIG.baseUrl}/categories`;

  constructor(private http: HttpClient) {}

  getAllCategories(): Observable<any> {
    return this.http.get(this.apiUrl);
  }
}