import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authService.getAuthToken();
    
    // Only add token to requests that are not authentication related
    if (token && !request.url.includes('/auth/login') && 
        !request.url.includes('/auth/register') &&
        !request.url.includes('/auth/forgot-password')) {
        request = request.clone({
            setHeaders: {
            Authorization: `Bearer ${token}`
            }
        });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Handle 401 Unauthorized errors by redirecting to login
        if (error.status === 401) {
          this.authService.logout();
          this.router.navigate(['/login']);
        }
        // Handle 403 Forbidden errors (insufficient permissions)
        else if (error.status === 403) {
          this.router.navigate(['/access-denied']);
        }
        
        return throwError(() => error);
      })
    );
  }
}