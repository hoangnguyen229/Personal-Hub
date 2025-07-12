import { Injectable } from "@angular/core";
import { API_CONFIG } from "../api.config";
import { Observable, BehaviorSubject } from "rxjs";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Login } from "../models/login.model";
import { Register } from "../models/register.model";
import { CookieService } from "ngx-cookie-service";

@Injectable({
    providedIn : 'root'
})
export class AuthService {
    private apiUrl = `${API_CONFIG.baseUrl}/auth`;
    
    private currentUserSubject = new BehaviorSubject<any>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    constructor(
        private http: HttpClient,
        private cookieService: CookieService
    ) {
        const user = this.getCurrentUser();
        this.currentUserSubject.next(user);
    }

    login(userLogin: Login): Observable<any> {
        return this.http.post(`${this.apiUrl}/login`, userLogin);
    }

    register(userRegister: Register): Observable<any> {
        return this.http.post(`${this.apiUrl}/register`, userRegister);
    }

    googleLogin(email: string, username: string): Observable<any> {
        return this.http.get(`${this.apiUrl}/google/callback`, {
            params: { email, username }
        });
    }

    initiateGoogleLogin(): void {
        const googleOAuthUrl = 'http://localhost:8094/oauth2/authorization/google';
        window.location.href = googleOAuthUrl;
    }

    githubLogin(email: string, username: string): Observable<any> {
        return this.http.get(`${this.apiUrl}/github/callback`, {
            params: { email, username }
        });
    }

    initiateGithubLogin(): void {
        const githubOAuthUrl = 'http://localhost:8094/oauth2/authorization/github';
        window.location.href = githubOAuthUrl;
    }

    forgotPassword(email: string): Observable<any> {
        return this.http.post(`${this.apiUrl}/forgot-password`, { email });
    }

    verifyOTP(email: string, otp: string): Observable<any> {
        return this.http.post(`${this.apiUrl}/verify-otp`, { email, otp });
    }

    resetPassword(token: string, newPassword: string, confirmPassword: string): Observable<any> {
        return this.http.post(`${this.apiUrl}/reset-password`, { 
            token, 
            newPassword, 
            confirmPassword 
        });
    }

    getAuthToken(): string | null {
        return this.cookieService.get('LOCAL_access_token') || 
               this.cookieService.get('GOOGLE_access_token') || 
               this.cookieService.get('GITHUB_access_token') || 
               null;
    }

    getCurrentUser(): any | null {
        const localUserStr = this.cookieService.get('LOCAL_user');
        const googleUserStr = this.cookieService.get('GOOGLE_user');
        const githubUserStr = this.cookieService.get('GITHUB_user');
        
        let user = null;
        if (localUserStr) user = JSON.parse(localUserStr);
        else if (googleUserStr) user = JSON.parse(googleUserStr);
        else if (githubUserStr) user = JSON.parse(githubUserStr);
    
        if (user) {
            if (user.user_id) {
                return {
                    user_id: user.user_id,
                    username: user.username,
                    email: user.email,
                    bio: user.bio,
                    profile_picture: user.profile_picture,
                    auth_type: user.auth_type
                };
        }  else {
                console.error('User object from cookie does not contain user_id:', user);
                return null;
            }
        }
        
        return null;
    }

    getAuthHeaders(): HttpHeaders{
        const token = this.getAuthToken();
        return new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });
    }

    isLoggedIn(): boolean {
        return !!this.getAuthToken();
    }

    handleSuccessfulLogin(response: any, authType: string): void {
        const cookieOptions = {
            expires: 1,
            path: '/',
            httpOnly: true,
            secure: false,
            sameSite: 'Lax' as 'Lax' 
        };

        const authTypeTokenCookie = `${authType}_access_token`;
        this.cookieService.set(authTypeTokenCookie, response.access_token, cookieOptions);

        const authTypeUserCookie = `${authType}_user`;
        this.cookieService.set(authTypeUserCookie, JSON.stringify(response.user), cookieOptions);

        this.currentUserSubject.next(response.user);
    }

    updateCurrentUser(user: any): void {
        const authType = user.auth_type;
        const authTypeCookie = `${authType}_user`;
        
        this.cookieService.set(authTypeCookie, JSON.stringify(user), {
            path: '/',
            secure: true,
            sameSite: 'Strict' as 'Strict'
        });
        
        this.currentUserSubject.next(user);
    }

    logout(): void {
        this.cookieService.delete('LOCAL_access_token', '/');
        this.cookieService.delete('LOCAL_user', '/');
        this.cookieService.delete('GOOGLE_access_token', '/');
        this.cookieService.delete('GOOGLE_user', '/');
        this.cookieService.delete('GITHUB_access_token', '/');
        this.cookieService.delete('GITHUB_user', '/');
        
        this.currentUserSubject.next(null);
    }
}