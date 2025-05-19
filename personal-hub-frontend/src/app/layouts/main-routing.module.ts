import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { MainLayoutComponent } from "./main-layout.component";
import { DashboardComponent } from "./dashboard/dashboard.component";
import { BlogDetailComponent } from "./blog-detail/blog-detail.component";
import { EditorComponent } from "./editor/editor.component";
import { AuthGuard } from "../core/guards/auth.guard";
import { UpdateProfileComponent } from "./update-profile/update-profile.component";
import { NotificationComponent } from "./notification/notification.component";
import { UserProfileComponent } from "./user-profile/user-profile.component";

const routes: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        children: [
            { path: '', component: DashboardComponent },
            { path: 'search/:q', component: DashboardComponent },
            { path: 'posts/tag/:tag', component: DashboardComponent },
            { path: 'blog/:slug', component: BlogDetailComponent},
            { path: 'editor', component: EditorComponent, canActivate: [AuthGuard]},
            { path: 'profile/edit', component: UpdateProfileComponent, canActivate: [AuthGuard]},
            { path: 'notifications', component: NotificationComponent, canActivate: [AuthGuard]},
            { path: 'profile/:email', component: UserProfileComponent, canActivate: [AuthGuard]},
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})

export class MainRoutingModule { }