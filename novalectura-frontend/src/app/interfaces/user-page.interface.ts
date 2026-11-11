import { IUser } from './user.interface';

export interface UserPage {
  contenido: IUser[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}
