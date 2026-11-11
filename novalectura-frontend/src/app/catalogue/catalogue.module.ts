import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComponentsModule } from '../components/components.module';
import { CatalogueComponent } from './catalogue/catalogue.component';
import { BookDetailComponent } from './book-detail/book-detail.component';
import { ItemBookComponent } from './item-book/item-book.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ComponentsModule
  ],
  declarations: [CatalogueComponent, BookDetailComponent, ItemBookComponent],
  exports: [CatalogueComponent]
})
export class CatalogueModule { }