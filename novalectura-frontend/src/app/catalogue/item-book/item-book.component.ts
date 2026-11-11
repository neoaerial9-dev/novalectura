import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ManagedBook } from '../../interfaces/books/book-api.interface';

@Component({
  selector: 'app-item-book',
  standalone: false,
  templateUrl: './item-book.component.html',
  styleUrls: ['./item-book.component.css']
})
export class ItemBookComponent {
  @Input() book!: ManagedBook;
  @Output() openDetails = new EventEmitter<ManagedBook>();
}
