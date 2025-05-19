import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    // First check if the user is authenticated
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/auth/login'], {
        queryParams: { returnUrl: state.url }
      });
      return false;
    }
    
    // Get required roles from route data
    const requiredRoles = route.data['roles'] as Array<string>;
    
    // If no specific roles required, allow access
    if (!requiredRoles || requiredRoles.length === 0) {
      return true;
    }
    
    // Get current user and check roles
    const currentUser = this.authService.getCurrentUser();
    
    if (!currentUser || !currentUser.roles) {
      this.router.navigate(['/access-denied']);
      return false;
    }
    
    // Check if user has any of the required roles
    const hasRequiredRole = requiredRoles.some(role => 
      currentUser.roles.includes(role)
    );
    
    if (!hasRequiredRole) {
      this.router.navigate(['/access-denied']);
      return false;
    }
    
    return true;
  }
}