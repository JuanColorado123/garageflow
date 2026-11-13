import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { provideRouter } from '@angular/router';
import { UsuarioService } from './core/usuario/usuario.service';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideRouter([]), UsuarioService],
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('00 REQ-5.1 y 07 REQ-1.1 el selector tiene los 4 usuarios y asesor.ana elegido', () => {
    const select = fixture.nativeElement.querySelector('select#usuario');
    const options = select.querySelectorAll('option');

    expect(options.length).toBe(4);
    expect(options[0].value).toBe('asesor.ana');
    expect(options[1].value).toBe('asesor.luis');
    expect(options[2].value).toBe('auditor.marta');
    expect(options[3].value).toBe('admin.sofia');

    expect(select.value).toBe('asesor.ana');
    expect(options[0].selected).toBe(true);
  });
});
