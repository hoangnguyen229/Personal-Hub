import { Component } from "@angular/core";
import { Router } from "@angular/router";
import { Login } from "src/app/core/models/login.model";
import { AuthService } from "src/app/core/services/auth.service";
import { ToastrService } from "ngx-toastr";
import { CookieService } from "ngx-cookie-service";

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent {
    userLogin: Login = { email: '', password: '' };
    errors: { [key: string]: string } = {};
    isSubmitted = false;

    constructor(
        private authService: AuthService,
        private router: Router,
        private toastr: ToastrService,
        private cookieService: CookieService
    ) {}

    onLogin() {
        this.isSubmitted = true;
        
        if (!this.validateForm()) {
            return;
        }

        this.authService.login(this.userLogin).subscribe(
            response => {
                this.handleSuccessfulLogin(response);
            },
            error => {
                this.handleLoginError(error);
            }
        );
    }

    validateForm(): boolean {
        this.errors = {};
        
        return this.validateEmail() && this.validatePassword();
    }

    private validateEmail(): boolean {
        const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        
        if (!this.userLogin.email.trim()) {
            this.errors['email'] = 'Email is required';
            return false;
        } 
        
        if (!emailPattern.test(this.userLogin.email)) {
            this.errors['email'] = 'Please enter a valid email';
            return false;
        }
        
        return true;
    }

    private validatePassword(): boolean {
        if (!this.userLogin.password) {
            this.errors['password'] = 'Password is required';
            return false;
        }
        
        return true;
    }

    private handleSuccessfulLogin(response: any): void {
        this.authService.handleSuccessfulLogin(response, 'LOCAL');
        this.toastr.success(response.message);
        
        setTimeout(() => {
            this.router.navigate(['/']);
        }, 1500);
    }

    private handleLoginError(error: any): void {
        debugger;
        this.toastr.error('Login failed');
        
        if (error.error.errors) {
            this.errors = { ...this.errors, ...error.error.errors };
        }
    }

    onGoogleLogin() {
        this.authService.initiateGoogleLogin();
    }
    
    onGithubLogin() {
        this.authService.initiateGithubLogin();
    }
}