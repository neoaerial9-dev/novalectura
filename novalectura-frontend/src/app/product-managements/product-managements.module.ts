import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ComponentsModule } from '../components/components.module';
import { ProductManagementComponent } from './product-management/product-management.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    ComponentsModule
  ],
  declarations: [ProductManagementComponent],
  exports: [ProductManagementComponent]
})
export class ProductManagementsModule { }