import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProfileComponent } from './profile/profile.component';
import { ListUsersComponent } from './list-users/list-users.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { RouterModule } from '@angular/router';
import { ComponentsModule } from '../components/components.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    ComponentsModule
  ],
  declarations: [ProfileComponent, ListUsersComponent, LoginComponent, RegisterComponent],
  exports: [LoginComponent],
})
export class AuthModule { }