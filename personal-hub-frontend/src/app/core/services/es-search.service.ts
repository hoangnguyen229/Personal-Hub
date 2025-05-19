import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';

@Injectable({
  providedIn: 'root'
})
export class EsSearchService {
  private apiUrl = `${API_CONFIG.baseUrl}/post`;

  constructor(private http: HttpClient) {}

  autocompleteTags(prefix: string): Observable<string[]> {
    let params = new HttpParams().set('prefix', prefix);
    return this.http.get<string[]>(`${this.apiUrl}/tag/autocomplete`, { params });
  }

  autocompleteTitles(prefix: string): Observable<string[]> {
    let params = new HttpParams().set('prefix', prefix);
    return this.http.get<string[]>(`${this.apiUrl}/suggest`, { params });
  }

    searchPostsByTag(tagName: string, page: number = 0, size: number = 20): Observable<any> {
    let params = new HttpParams()
        .set('page', page.toString())
        .set('size', size.toString());
    return this.http.get(`${this.apiUrl}/tag/${tagName}`, { params });
    }

  searchPostsByTitle(query: string, page: number = 0, size: number = 20): Observable<any> {
    debugger;
    let params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get(`${this.apiUrl}/search`, { params });
  }

}