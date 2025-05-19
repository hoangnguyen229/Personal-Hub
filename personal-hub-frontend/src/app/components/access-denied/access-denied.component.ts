import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-access-denied',
  template: `
    <div class="access-denied-container">
      <div class="content">
        <h1>Access Denied</h1>
        <p>Sorry, you don't have permission to access this page.</p>
        <div class="actions">
          <button class="btn btn-primary" (click)="goHome()">Go to Home</button>
          <button class="btn btn-outline-secondary" (click)="goBack()">Go Back</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .access-denied-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
      padding: 2rem;
    }
    
    .content {
      text-align: center;
      max-width: 600px;
      padding: 2rem;
      background-color: #fff;
      border-radius: 8px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    }
    
    h1 {
      color: #dc3545;
      margin-bottom: 1rem;
    }
    
    .actions {
      margin-top: 2rem;
    }
    
    .btn {
      margin: 0 0.5rem;
    }
  `]
})
export class AccessDeniedComponent {
  
  constructor(private router: Router) {}
  
  goHome(): void {
    this.router.navigate(['/']);
  }
  
  goBack(): void {
    window.history.back();
  }
}