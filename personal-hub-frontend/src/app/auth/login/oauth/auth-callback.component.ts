import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth.service';
import { ToastrService } from 'ngx-toastr';
import { CookieService } from 'ngx-cookie-service';

@Component({
  selector: 'app-auth-callback',
  templateUrl: './auth-callback.component.html',
  styleUrls: ['./auth-callback.component.scss']
})
export class AuthCallbackComponent implements OnInit {
  statusMessage: string = 'Please wait a moment...';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private toastr: ToastrService,
    private cookieService: CookieService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const email = params['email'];
      const username = params['username'];
      const status = params['status'];
      const error = params['error'];
      const provider = params['provider'];
      if (status === 'failed') {
        this.statusMessage = `${provider} login failed!`;
        this.toastr.error(error || `${provider} login failed!`);
        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 1500);
        return;
      }

      if (email && username) {
        this.statusMessage = 'Verifying information...';
        
        let authObservable;
        if (provider === 'GITHUB') {
          authObservable = this.authService.githubLogin(email, username);
        } else {
          authObservable = this.authService.googleLogin(email, username);
        }
        
        authObservable.subscribe(
          response => {
            this.statusMessage = `${provider} login successful!`;
            this.authService.handleSuccessfulLogin(response, provider);
            this.toastr.success(response.message);

            setTimeout(() => {
              this.router.navigate(['/']);
            }, 1500);
          },
          error => {
            this.statusMessage = 'Login failed!';
            this.toastr.error(error.message || 'Unable to authenticate with server');
            setTimeout(() => {
              this.router.navigate(['/auth/login']);
            }, 1500);
          }
        );
      } else {
        this.statusMessage = 'Missing login information!';
        this.toastr.error('Missing login information!');
        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 1500);
      }
    });
  }
}