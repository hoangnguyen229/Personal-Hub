import { Component } from "@angular/core";
import { Router } from "@angular/router";
import { Register } from "src/app/core/models/register.model";
import { AuthService } from "src/app/core/services/auth.service";
import { ToastrService } from "ngx-toastr";

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css']
})

export class RegisterComponent {
    userRegister: Register = { 
        username: '', 
        email: '', 
        password: '',
        confirmPassword: '' 
    };
    errors: { [key: string]: string } = {};
    isSubmitted = false;

    constructor(private authService: AuthService, private router: Router, private toastr: ToastrService) {}

    onRegister() {
        this.isSubmitted = true;
        
        if (!this.validateForm()) {
            return;
        }

        this.authService.register(this.userRegister).subscribe(
            response => {
                this.toastr.success(response.message);
                setTimeout(() => {
                    this.router.navigate(['/auth/login']);
                }, 1500);
            },
            error => {
                this.handleError(error);
            }
        );
    }

    validateForm(): boolean {
        this.errors = {};
        
        return this.validateUsername() &&
               this.validateEmail() &&
               this.validatePassword() &&
               this.validateConfirmPassword();
    }

    private validateUsername(): boolean {
        if (!this.userRegister.username.trim()) {
            this.errors['username'] = 'Username is required';
            return false;
        } 
        
        if (this.userRegister.username.length < 3) {
            this.errors['username'] = 'Username must be at least 3 characters';
            return false;
        }
        
        return true;
    }

    private validateEmail(): boolean {
        const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        
        if (!this.userRegister.email.trim()) {
            this.errors['email'] = 'Email is required';
            return false;
        } 
        
        if (!emailPattern.test(this.userRegister.email)) {
            this.errors['email'] = 'Please enter a valid email';
            return false;
        }
        
        return true;
    }

    private validatePassword(): boolean {
        if (!this.userRegister.password) {
            this.errors['password'] = 'Password is required';
            return false;
        } 
        
        if (this.userRegister.password.length < 8) {
            this.errors['password'] = 'Password must be at least 8 characters';
            return false;
        }
        
        return true;
    }

    private validateConfirmPassword(): boolean {
        if (!this.userRegister.confirmPassword) {
            this.errors['confirmPassword'] = 'Please confirm your password';
            return false;
        } 
        
        if (this.userRegister.password !== this.userRegister.confirmPassword) {
            this.errors['confirmPassword'] = 'Passwords do not match';
            return false;
        }
        
        return true;
    }

    private handleError(error: any): void {
        this.toastr.error(error.error.message || 'Registration failed');
        
        if (error.error.errors) {
            this.errors = { ...this.errors, ...error.error.errors };
        }
    }
}