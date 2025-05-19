// src/app/auth/verify-otp/verify-otp.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth.service';
import { ToastrService } from 'ngx-toastr';
import { VerifyOTP } from 'src/app/core/models/verify-otp.model';

@Component({
  selector: 'app-verify-otp',
  templateUrl: './verify-otp.component.html',
  styleUrls: ['./verify-otp.component.css']
})
export class VerifyOtpComponent implements OnInit {
  verifyOTPData: VerifyOTP = { email: '', otp: '' };
  errors: { [key: string]: string } = {};
  isSubmitted = false;
  isLoading = false;
  otpDigits: string[] = ['', '', '', '', '', ''];
  remainingTime: number = 300; // 5 minutes in seconds
  timerInterval: any;

  constructor(
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    const email = sessionStorage.getItem('reset_email');
    if (!email) {
      this.toastr.error('Email not found. Please restart the password reset process.');
      this.router.navigate(['/auth/forgot-password']);
      return;
    }
    
    this.verifyOTPData.email = email;
    this.startTimer();
  }

  ngOnDestroy(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  startTimer(): void {
    this.timerInterval = setInterval(() => {
      this.remainingTime--;
      if (this.remainingTime <= 0) {
        clearInterval(this.timerInterval);
        this.toastr.warning('OTP has expired. Please request a new one.');
      }
    }, 1000);
  }

  formatTime(): string {
    const minutes: number = Math.floor(this.remainingTime / 60);
    const seconds: number = this.remainingTime % 60;
    return `${minutes}:${seconds < 10 ? '0' : ''}${seconds}`;
  }

  onDigitInput(event: any, index: number): void {
    const input = event.target as HTMLInputElement;
    let value = input.value;
    
    // Chỉ lấy ký tự đầu tiên nếu nhiều hơn 1 ký tự được nhập
    if (value.length > 1) {
      value = value.charAt(value.length - 1);
      input.value = value;
    }
    
    // Chỉ chấp nhận ký tự số
    if (/^\d$/.test(value)) {
      // Cập nhật giá trị OTP
      this.otpDigits[index] = value;
      this.verifyOTPData.otp = this.otpDigits.join('');
      
      // Auto-focus cho ô tiếp theo
      if (index < 5) {
        const nextInput = document.getElementById(`otp-${index + 1}`) as HTMLInputElement;
        if (nextInput) {
          nextInput.focus();
        }
      }
    } else {
      // Xóa giá trị nếu không phải số
      input.value = '';
      this.otpDigits[index] = '';
    }
  }

  onPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const clipboardData = event.clipboardData;
    if (!clipboardData) return;
    
    const pastedText = clipboardData.getData('text');
    if (!pastedText) return;
    
    // Only process if the pasted content looks like an OTP (6 digits)
    if (/^\d{6}$/.test(pastedText)) {
      for (let i = 0; i < 6 && i < pastedText.length; i++) {
        this.otpDigits[i] = pastedText[i];
        const inputElement = document.getElementById(`otp-${i}`) as HTMLInputElement;
        if (inputElement) {
          inputElement.value = pastedText[i];
        }
      }
      this.verifyOTPData.otp = pastedText;
    }
  }

  onSubmit(): void {
    this.isSubmitted = true;
    this.isLoading = true;
    
    if (!this.validateForm()) {
      this.isLoading = false;
      return;
    }

    this.authService.verifyOTP(this.verifyOTPData.email, this.verifyOTPData.otp).subscribe(
      response => {
        this.toastr.success(response.message);
        this.isLoading = false;
        
        // Store the token for the reset password process
        sessionStorage.setItem('reset_token', response.access_token);
        
        setTimeout(() => {
          this.router.navigate(['/auth/reset-password']);
        }, 1500);
      },
      error => {
        this.isLoading = false;
        this.toastr.error(error.error.message || 'Invalid OTP');
        
        if (error.error.errors) {
          this.errors = { ...this.errors, ...error.error.errors };
        }
      }
    );
  }

  validateForm(): boolean {
    this.errors = {};
    
    if (!this.verifyOTPData.otp || this.verifyOTPData.otp.length !== 6) {
      this.errors['otp'] = 'Please enter a valid 6-digit OTP';
      return false;
    }
    
    if (!this.verifyOTPData.email) {
      this.errors['email'] = 'Email is required';
      return false;
    }
    
    return true;
  }

  resendOTP(): void {
    if (this.remainingTime > 0) {
      return; // Prevent resending if timer is still active
    }
    
    this.isLoading = true;
    this.authService.forgotPassword(this.verifyOTPData.email).subscribe(
      response => {
        this.toastr.success(response.message);
        this.isLoading = false;
        
        // Reset timer
        this.remainingTime = 300;
        this.startTimer();
        
        // Clear OTP fields
        this.otpDigits = ['', '', '', '', '', ''];
        this.verifyOTPData.otp = '';
        
        // Clear input fields
        for (let i = 0; i < 6; i++) {
          const inputElement = document.getElementById(`otp-${i}`) as HTMLInputElement;
          if (inputElement) {
            inputElement.value = '';
          }
        }
      },
      error => {
        this.isLoading = false;
        this.toastr.error(error.error.message || 'Failed to resend OTP');
      }
    );
  }
}