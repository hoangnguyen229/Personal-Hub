import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { DashboardComponent } from "./dashboard/dashboard.component";
import { MainRoutingModule } from "./main-routing.module";
import { NavbarComponent } from "../components/navbar/navbar.component";
import { FooterComponent } from "../components/footer/footer.component";
import { MainLayoutComponent } from "./main-layout.component";
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { BlogDetailComponent } from './blog-detail/blog-detail.component';
import { EditorComponent } from './editor/editor.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RichTextEditorAllModule } from "@syncfusion/ej2-angular-richtexteditor";
import { UpdateProfileComponent } from './update-profile/update-profile.component';
import { NotificationComponent } from './notification/notification.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
@NgModule({
    declarations: [
        DashboardComponent,
        NavbarComponent,
        FooterComponent,
        MainLayoutComponent,
        BlogDetailComponent,
        EditorComponent,
        UpdateProfileComponent,
        NotificationComponent,
        UserProfileComponent,
    ],
    imports: [
        MainRoutingModule,
        CommonModule,
        FontAwesomeModule,
        FormsModule,
        HttpClientModule,
        RichTextEditorAllModule,
        ReactiveFormsModule
    ],
})

export class MainModule { }