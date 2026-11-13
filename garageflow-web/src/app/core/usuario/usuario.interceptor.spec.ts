import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { UsuarioService } from './usuario.service';
import { usuarioInterceptor } from './usuario.interceptor';

describe('usuarioInterceptor', () => {
  let httpMock: HttpTestingController;
  let usuarioService: UsuarioService;
  let httpClient: HttpClient;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([usuarioInterceptor])),
        provideHttpClientTesting(),
        UsuarioService,
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
    usuarioService = TestBed.inject(UsuarioService);
    httpClient = TestBed.inject(HttpClient);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('00 REQ-5.2 agrega X-Usuario con el usuario elegido', () => {
    usuarioService.elegir('asesor.luis');

    httpClient.get('/test').subscribe();

    const req = httpMock.expectOne('/test');
    expect(req.request.headers.get('X-Usuario')).toBe('asesor.luis');
    req.flush({});
  });

  it('07 REQ-1.2 envía X-Rol con el rol elegido', () => {
    // Default user is asesor.ana with role ASESOR
    httpClient.get('/test').subscribe();
    let req = httpMock.expectOne('/test');
    expect(req.request.headers.get('X-Rol')).toBe('ASESOR');
    req.flush({});

    // After choosing auditor.marta
    usuarioService.elegir('auditor.marta');
    httpClient.get('/test2').subscribe();
    req = httpMock.expectOne('/test2');
    expect(req.request.headers.get('X-Rol')).toBe('AUDITOR');
    req.flush({});
  });
});
