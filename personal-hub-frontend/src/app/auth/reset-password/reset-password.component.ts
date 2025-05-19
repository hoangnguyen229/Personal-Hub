// src/app/auth/reset-password/reset-password.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth.service';
import { ToastrService } from 'ngx-toastr';
import { ResetPassword } from 'src/app/core/models/reset-password.model';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  resetPasswordData: ResetPassword = {
    token: '',
    newPassword: '',
    confirmPassword: ''
  };
  errors: { [key: string]: string } = {};
  isSubmitted = false;
  isLoading = false;
  showPassword = false;
  showConfirmPassword = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    const token = sessionStorage.getItem('reset_token');
    if (!token) {
      this.toastr.error('Reset token not found. Please restart the password reset process.');
      this.router.navigate(['/forgot-password']);
      return;
    }
    
    this.resetPasswordData.token = token;
  }

  onSubmit(): void {
    this.isSubmitted = true;
    this.isLoading = true;
    
    if (!this.validateForm()) {
      this.isLoading = false;
      return;
    }

    this.authService.resetPassword(
      this.resetPasswordData.token,
      this.resetPasswordData.newPassword,
      this.resetPasswordData.confirmPassword
    ).subscribe(
      response => {
        this.toastr.success(response.message);
        this.isLoading = false;
        
        // Clean up session storage
        sessionStorage.removeItem('reset_email');
        sessionStorage.removeItem('reset_token');
        
        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 1500);
      },
      error => {
        this.isLoading = false;
        this.toastr.error(error.error.message || 'Failed to reset password');
        
        if (error.error.errors) {
          this.errors = { ...this.errors, ...error.error.errors };
        }
      }
    );
  }

  validateForm(): boolean {
    this.errors = {};
    return this.validatePassword() && this.validateConfirmPassword();
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  private validatePassword(): boolean {    
    if (!this.resetPasswordData.newPassword) {
      this.errors['newPassword'] = 'Password is required';
      return false;
    }
  
    return true;
  }

  private validateConfirmPassword(): boolean {
    if (!this.resetPasswordData.confirmPassword) {
      this.errors['confirmPassword'] = 'Confirm Password is required';
      return false;
    }
    
    if (this.resetPasswordData.newPassword !== this.resetPasswordData.confirmPassword) {
      this.errors['confirmPassword'] = 'Passwords do not match';
      return false;
    }
    
    return true;
  }
}