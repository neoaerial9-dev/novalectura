import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ComponentsModule } from '../components/components.module';
import { ShoppingsComponent } from './shoppings/shoppings.component';
import { HistoryComponent } from './history/history.component';

@NgModule({
  imports: [
    CommonModule,
    ComponentsModule
  ],
  declarations: [ShoppingsComponent, HistoryComponent]
})
export class ShoppingCarModule { }