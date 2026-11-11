import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { IUser } from '../../interfaces/user.interface';
import { UserPage } from '../../interfaces/user-page.interface';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-list-users',
  standalone: false,
  templateUrl: './list-users.component.html',
  styleUrls: ['./list-users.component.css']
})
export class ListUsersComponent implements OnInit {
  users: IUser[] = [];
  currentPage = 0;
  totalPages = 0;
  totalUsers = 0;
  loading = false;
  errorMessage = '';

  constructor(private userService: UserService) { }

  ngOnInit(): void {
    this.loadUsers(0);
  }

  loadUsers(page: number): void {
    this.loading = true;
    this.errorMessage = '';
    this.userService.getUsers(page).subscribe({
      next: (result: UserPage) => {
        this.users = result.contenido;
        this.currentPage = result.pagina;
        this.totalPages = result.totalPaginas;
        this.totalUsers = result.totalElementos;
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        const detail: unknown = error.error?.detail;
        this.errorMessage = typeof detail === 'string' && detail.trim()
          ? detail
          : 'No se pudieron cargar los usuarios. Inténtalo de nuevo.';
        this.loading = false;
      }
    });
  }
}
