import {
  State,
  Action,
  Selector,
  StateContext
} from '@ngxs/store';
import { Injectable } from '@angular/core';
import { IBook } from '../../interfaces/books/book.interface';
import { stoks } from '../../services/local-data/inventaries';

export interface ICatalogueModel {
  books: IBook[];
  currentPosition: number;
  lastPosition: number;
}

export class RetrieveBooksData {
  static readonly type = '[CatalogueCollection] Retrieve Books Data';
  constructor() {}
}

@State<ICatalogueModel>({
  name: 'CatalogueCollection',
  defaults: {
    books: [],
    currentPosition: 0,
    lastPosition: 0,
  }
})
@Injectable()
export class CatalogeState {
  constructor() { }

  @Selector()
  static books(state: ICatalogueModel) {
    return state.books;
  }

  @Action(RetrieveBooksData)
  async retrieveBooksData(
    ctx: StateContext<ICatalogueModel>,
    action: RetrieveBooksData,
  ) {
    const lastPosition = Math.ceil(stoks.length / 5);
    ctx.patchState({
      books: [...stoks], 
      lastPosition: lastPosition,
    });
  }

}
