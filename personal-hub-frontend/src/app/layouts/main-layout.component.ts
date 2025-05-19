import { Component } from '@angular/core';

@Component({
  selector: 'app-main-layout',
  template: `
    <app-navbar></app-navbar>
    <div class="main-content">
      <router-outlet></router-outlet>
    </div>
    <app-footer></app-footer>
  `,
  styles: [`
    .main-content {
      min-height: calc(100vh - 120px); /* Điều chỉnh theo chiều cao của navbar và footer */
      padding: 20px;
    }
  `]
})
export class MainLayoutComponent { }