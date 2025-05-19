import { NgModule } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { CommonModule } from "@angular/common";
import { AuthRoutingModule } from "./auth-routing.module";
import { LoginComponent } from "./login/login.component";
import { RegisterComponent } from "./register/register.component";
import { TextFieldComponent } from "./shared/textfield/textfield.component";
import { ButtonComponent } from "./shared/button/button.component";
import { AuthLayoutComponent } from "./auth-layout.component";
import { ToastrModule } from "ngx-toastr";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { VerifyOtpComponent } from './verify-otp/verify-otp.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';

@NgModule({
    declarations: [
        LoginComponent,
        RegisterComponent,
        TextFieldComponent,
        ButtonComponent,
        AuthLayoutComponent,
        ForgotPasswordComponent,
        VerifyOtpComponent,
        ResetPasswordComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        AuthRoutingModule,
        ToastrModule.forRoot(),
        BrowserAnimationsModule
    ],
})

export class AuthModule { }