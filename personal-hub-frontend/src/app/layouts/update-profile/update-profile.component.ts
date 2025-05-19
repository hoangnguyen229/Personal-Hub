import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from 'src/app/core/services/auth.service';
import { UserService } from 'src/app/core/services/user.service';
import { ImageService } from 'src/app/core/services/image.service';

@Component({
  selector: 'app-update-profile',
  templateUrl: './update-profile.component.html',
  styleUrls: ['./update-profile.component.css']
})
export class UpdateProfileComponent implements OnInit {
  profileForm: FormGroup;
  currentUser: any = null;
  isLoading = false;
  selectedFile: File | null = null;
  imagePreview: string | null = null;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private imageService: ImageService,
    private toastr: ToastrService,
    private router: Router
  ) {
    this.profileForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      bio: ['', [Validators.maxLength(200)]]
    });
  }

  ngOnInit(): void {
    this.isLoading = true;
    this.currentUser = this.authService.getCurrentUser();
    
    if (this.currentUser) {
      this.profileForm.patchValue({
        username: this.currentUser.username,
        bio: this.currentUser.bio
      });
      this.imagePreview = this.currentUser.profile_picture;
    } else {
      this.toastr.error('You must be logged in to update your profile');
      this.router.navigate(['/auth/login']);
    }
    this.isLoading = false;
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    
    if (file) {
      const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg'];
      if (!allowedTypes.includes(file.type)) {
        this.toastr.error('Only JPEG, JPG, and PNG files are allowed');
        return;
      }
      
      if (file.size > 2 * 1024 * 1024) {
        this.toastr.error('Image size should not exceed 2MB');
        return;
      }
      
      this.selectedFile = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.toastr.error('Please correct the form errors');
      return;
    }

    this.isLoading = true;
    const formData = new FormData();
    formData.append('username', this.profileForm.value.username);
    formData.append('bio', this.profileForm.value.bio || '');
    
    if (this.selectedFile) {
      formData.append('profilePic', this.selectedFile);
    }

    this.userService.updateProfile(formData).subscribe({
      next: (response) => {
        const updatedUser = {
          ...this.currentUser,
          username: response.username,
          bio: response.bio,
          profile_picture: response.profile_picture
        };
        
        this.authService.updateCurrentUser(updatedUser);
        this.toastr.success('Profile updated successfully');
        this.router.navigate(['/']);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error updating profile:', error);
        this.toastr.error(error.error?.message || 'Failed to update profile');
        this.isLoading = false;
      }
    });
  }
}