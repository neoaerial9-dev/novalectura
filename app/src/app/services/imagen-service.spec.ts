import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { ImagenService } from './imagen-service';

describe('ImagenService', () => {
  it('sube el archivo como multipart (campo "archivo") y devuelve la ruta', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const http = TestBed.inject(HttpTestingController);
    const archivo = new File(['x'], 'portada.png', { type: 'image/png' });
    let ruta = '';

    TestBed.inject(ImagenService).subir(archivo).subscribe((r) => (ruta = r));
    const req = http.expectOne(`${environment.apiUrl}/imagenes`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBeInstanceOf(FormData);
    expect((req.request.body as FormData).get('archivo')).toBeInstanceOf(File);
    req.flush({ ruta: '/api/imagenes/abc.png' });

    expect(ruta).toBe('/api/imagenes/abc.png');
  });
});
