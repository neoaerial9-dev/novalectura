import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { ActualizacionesService } from './actualizaciones-service';
import { AuthService } from './auth-service';
import { UsuarioService } from './usuario-service';

describe('Avisos de cambios en usuarios', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
  });

  it('actualizar el perfil avisa que cambió la lista de usuarios', () => {
    const http = TestBed.inject(HttpTestingController);
    const aviso = vi.fn();
    TestBed.inject(ActualizacionesService).de('usuarios').subscribe(aviso);

    TestBed.inject(UsuarioService).actualizarPerfil({ nombres: 'Ana', apellidos: 'Paz', telefono: null, direccionEnvio: null }).subscribe();
    http.expectOne(`${environment.apiUrl}/usuarios/me`).flush({});

    expect(aviso).toHaveBeenCalledTimes(1);
  });

  it('registrarse avisa que apareció un usuario nuevo', () => {
    const http = TestBed.inject(HttpTestingController);
    const aviso = vi.fn();
    TestBed.inject(ActualizacionesService).de('usuarios').subscribe(aviso);

    TestBed.inject(AuthService).registrar({ nombres: 'Ana', apellidos: 'Paz', correo: 'a@b.com', contrasena: 'secreto123' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/registro`).flush({});

    expect(aviso).toHaveBeenCalledTimes(1);
  });
});
