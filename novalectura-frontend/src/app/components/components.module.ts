import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ModalComponent } from './modal/modal.component';
import { TopMenuComponent } from './top-menu/top-menu.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule
  ],
  declarations: [ModalComponent, TopMenuComponent],
  exports: [TopMenuComponent]
})
export class ComponentsModule { }