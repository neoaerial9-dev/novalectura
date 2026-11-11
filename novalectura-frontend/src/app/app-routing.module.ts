import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CatalogueComponent } from './catalogue/catalogue/catalogue.component';
import { LoginComponent } from './auth/login/login.component';
import { ListUsersComponent } from './auth/list-users/list-users.component';
import { ProfileComponent } from './auth/profile/profile.component';
import { RegisterComponent } from './auth/register/register.component';
import { ProductManagementComponent } from './product-managements/product-management/product-management.component';
import { adminGuard } from './guards/admin.guard';
import { authGuard } from './guards/auth.guard';
import { ShoppingsComponent } from './shopping-car/shoppings/shoppings.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegisterComponent },
  { path: 'catalogo', component: CatalogueComponent },
  { path: 'carrito', component: ShoppingsComponent, canActivate: [authGuard] },
  { path: 'cuenta', component: ProfileComponent, canActivate: [authGuard] },
  { path: 'admin/libros', component: ProductManagementComponent, canActivate: [adminGuard] },
  { path: 'admin/usuarios', component: ListUsersComponent, canActivate: [adminGuard] },
  { path: '**', redirectTo: 'login' }
];

@NgModule({
  exports: [RouterModule],
  imports: [
    RouterModule.forRoot(routes)
  ],
  declarations: []
})
export class AppRoutingModule { }