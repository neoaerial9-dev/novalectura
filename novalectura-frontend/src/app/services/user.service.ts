import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';
import { UserPage } from '../interfaces/user-page.interface';
import { IUser, UserProfileRequest } from '../interfaces/user.interface';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly usersUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) { }

  getUsers(page: number): Observable<UserPage> {
    const params = new HttpParams().set('pagina', page).set('tamano', 12);
    return this.http.get<UserPage>(this.usersUrl, { params });
  }

  getMyProfile(): Observable<IUser> {
    return this.http.get<IUser>(`${this.usersUrl}/me`);
  }

  updateMyProfile(request: UserProfileRequest): Observable<IUser> {
    return this.http.put<IUser>(`${this.usersUrl}/me`, request);
  }
}
