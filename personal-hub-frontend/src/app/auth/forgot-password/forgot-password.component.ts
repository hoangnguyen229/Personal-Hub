// src/app/auth/forgot-password/forgot-password.component.ts
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth.service';
import { ToastrService } from 'ngx-toastr';
import { ForgotPassword } from 'src/app/core/models/forgot-password.model';

@Component({
    selector: 'app-forgot-password',
    templateUrl: './forgot-password.component.html',
    styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
    forgotPasswordData: ForgotPassword = { email: '' };
    errors: { [key: string]: string } = {};
    isSubmitted = false;
    isLoading = false;

    constructor(
        private authService: AuthService,
        private router: Router,
        private toastr: ToastrService
    ) {}

    onSubmit() {
        this.isSubmitted = true;
        this.isLoading = true;
        
        if (!this.validateForm()) {
            this.isLoading = false;
            return;
        }

        this.authService.forgotPassword(this.forgotPasswordData.email).subscribe(
            response => {
                this.toastr.success(response.message);
                this.isLoading = false;
                
                sessionStorage.setItem('reset_email', this.forgotPasswordData.email);
                debugger
                setTimeout(() => {
                  this.router.navigate(['/auth/verify-otp']);
                }, 1500);
            },
            error => {
                this.isLoading = false;
                this.toastr.error(error.error.message || 'Failed to process request');
                
                if (error.error.errors) {
                    this.errors = { ...this.errors, ...error.error.errors };
                }
            }
        );
    }

    validateForm(): boolean {
        this.errors = {};
        return this.validateEmail();
    }

    private validateEmail(): boolean {
        const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        
        if (!this.forgotPasswordData.email.trim()) {
            this.errors['email'] = 'Email is required';
            return false;
        } 
        
        if (!emailPattern.test(this.forgotPasswordData.email)) {
            this.errors['email'] = 'Please enter a valid email';
            return false;
        }
        
        return true;
    }
}