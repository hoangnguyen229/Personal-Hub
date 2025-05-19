import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CategoryService } from 'src/app/core/services/category.service';
import { CategoryModel } from 'src/app/core/models/category.model';
import { AuthService } from 'src/app/core/services/auth.service';
import { ImageService } from 'src/app/core/services/image.service';
import { PostService } from 'src/app/core/services/post.service';
import { ToolbarService, LinkService, ImageService as RTEImageService, HtmlEditorService, QuickToolbarService, CountService } from '@syncfusion/ej2-angular-richtexteditor';
import { UploadingEventArgs } from '@syncfusion/ej2-inputs';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RichTextEditorComponent } from '@syncfusion/ej2-angular-richtexteditor';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.css'],
  providers: [ToolbarService, LinkService, RTEImageService, HtmlEditorService, QuickToolbarService, CountService]
})
export class EditorComponent implements OnInit, OnDestroy {
  @ViewChild('rteInstance') rteInstance: RichTextEditorComponent;

  public tools: object = {
    type: 'Expand',
    items: [
      'Bold', 'Italic', 'Underline', '|',
      'Formats', 'Alignments', '|',
      'OrderedList', 'UnorderedList', '|',
      {
        tooltipText: 'Insert Custom Image',
        template: '<button class="e-tbar-btn e-btn" type="button" id="customImage"><span class="e-btn-icon e-icons e-image">🖼️</span></button>',
        click: this.onCustomImageClick.bind(this)
      },
      '|', 'SourceCode', 'Undo', 'Redo' 
    ]
  };

  public quickToolbarSettings: object = {
    image: ['Replace', 'Align', 'Caption', 'Remove', 'InsertLink', '-', 'Display', 'AltText', 'Dimension']
  };

  public insertImageSettings: object = {
    saveUrl: null,
    allowedTypes: ['.jpeg', '.jpg', '.png', '.gif', '.webp'],
  };

  public blogForm: FormGroup;
  public categories: CategoryModel[] = [];
  public isLoading: boolean = false;
  public errors: { [key: string]: string } = {};
  public wordCount: number = 0;
  private tempImages: string[] = [];

  constructor(
    private postService: PostService,
    private router: Router,
    private toastrService: ToastrService,
    private categoryService: CategoryService,
    private authService: AuthService,
    private imageService: ImageService,
    private formBuilder: FormBuilder
  ) { }

  ngOnInit(): void {
    this.initForm();
    this.loadCategories();
  }

  ngOnDestroy(): void {}

  initForm(): void {
    this.blogForm = this.formBuilder.group({
      blogTitle: ['', [Validators.required]],
      blogContent: ['', [Validators.required]],
      blogTags: [''],
      selectedCategoryId: [0, [Validators.min(1)]]
    });
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe(
      (response) => {
        this.categories = response;
        if (this.categories.length > 0) {
          this.blogForm.patchValue({
            selectedCategoryId: this.categories[0].categoryID
          });
        }
      },
      (error) => {
        this.toastrService.error('Unable to load categories');
        console.error(error);
      }
    );
  }

  onImageUpload(args: UploadingEventArgs): void {
    args.cancel = true; // Hủy hành vi upload mặc định
  }

  isValidImageFile(file: File): boolean {
    const acceptedImageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    return acceptedImageTypes.includes(file.type);
  }

  uploadCustomImage(file: File): void {
    this.toastrService.info('Uploading image...', '', { timeOut: 1000 });
    this.imageService.uploadImage(file).subscribe(
      response => {
        const imageUrl = response.image_url;
        if (!imageUrl) {
          this.toastrService.error('Did not receive image URL from server');
          return;
        }
        this.tempImages.push(imageUrl);

        // Chèn ảnh vào editor
        const imageHtml = `<img src="${imageUrl}" alt="Uploaded Image" class="e-rte-image e-imginline">`;
        this.rteInstance.executeCommand('insertHTML', imageHtml);

        // Cập nhật nội dung form
        this.blogForm.patchValue({
          blogContent: this.rteInstance.value
        });

        this.toastrService.success('Image uploaded successfully');
      },
      error => {
        this.toastrService.error('Unable to upload image');
        console.error('Error uploading image:', error);
      }
    );
  }

  onCustomImageClick(): void {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.jpeg,.jpg,.png,.gif,.webp';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file && this.isValidImageFile(file)) {
        this.uploadCustomImage(file);
      } else {
        this.toastrService.error('Only image files are accepted');
      }
    };
    input.click();
  }

  onEditorCreated(): void {
    console.log('Editor created');
  }

  onValueChange(args: any): void {
    const text = args.value.replace(/<[^>]*>/g, ' ').replace(/\s+/g, ' ').trim();
    this.wordCount = text ? text.split(' ').filter(Boolean).length : 0;
    this.blogForm.patchValue({
      blogContent: args.value
    });
  }

  publishPost(): void {
    if (this.blogForm.invalid) {
      this.markFormGroupTouched(this.blogForm);
      return;
    }

    if (!this.authService.isLoggedIn()) {
      this.toastrService.error('You must be logged in to publish a post');
      this.router.navigate(['/login']);
      return;
    }

    let content = this.blogForm.value.blogContent;
    if (content.includes('blob:')) {
      content = content.replace(/<img[^>]+src="blob:[^"]*"[^>]*>/g, '');
      this.blogForm.patchValue({ blogContent: content });
    }

    this.isLoading = true;
    const formData = new FormData();
    formData.append('title', this.blogForm.value.blogTitle);
    formData.append('content', this.blogForm.value.blogContent);
    formData.append('categoryID', this.blogForm.value.selectedCategoryId.toString());

    if (this.blogForm.value.blogTags.trim()) {
      const tagNames = this.blogForm.value.blogTags.split(',').map(tag => tag.trim());
      formData.append('tagNames', JSON.stringify(tagNames));
    }

    if (this.tempImages.length > 0) {
      formData.append('tempImages', JSON.stringify(this.tempImages));
    }

    this.postService.createPost(formData).subscribe(
      response => {
        this.isLoading = false;
        this.toastrService.success('Post has been published successfully!');
        setTimeout(() => {
          this.router.navigate(['/']);
        }, 1500);
      },
      error => {
        this.isLoading = false;
        if (error.status === 401 || error.status === 403) {
          this.toastrService.error('Authentication error: Please log in again');
          this.authService.logout();
          this.router.navigate(['/login']);
        } else {
          this.toastrService.error(error.error?.message || 'Unable to publish post');
          if (error.error?.errors) {
            this.errors = { ...this.errors, ...error.error.errors };
          }
        }
        console.error(error);
      }
    );
  }

  saveDraft(): void {
    this.toastrService.info('Draft saving feature is not yet implemented');
  }

  previewPost(): void {
    this.toastrService.info('Preview feature is not yet implemented');
  }

  markFormGroupTouched(formGroup: FormGroup) {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }
}