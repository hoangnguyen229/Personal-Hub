import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { LoginComponent } from "./login/login.component";
import { RegisterComponent } from "./register/register.component";
import { AuthLayoutComponent } from "./auth-layout.component";
import { AuthCallbackComponent } from "./login/oauth/auth-callback.component";
import { ForgotPasswordComponent } from "./forgot-password/forgot-password.component";
import { VerifyOtpComponent } from "./verify-otp/verify-otp.component";
import { ResetPasswordComponent } from "./reset-password/reset-password.component";
import { NonAuthGuard } from "../core/guards/non-auth.guard";

const routes: Routes = [
    {
        path: '',
        component: AuthLayoutComponent,
        canActivate: [NonAuthGuard],
        children: [
            { path: 'login', component: LoginComponent},
            { path: 'register', component: RegisterComponent },
            { path: 'callback', component: AuthCallbackComponent},
            { path: 'forgot-password', component: ForgotPasswordComponent },
            { path: 'verify-otp', component: VerifyOtpComponent },
            { path: 'reset-password', component: ResetPasswordComponent },
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})

export class AuthRoutingModule { }