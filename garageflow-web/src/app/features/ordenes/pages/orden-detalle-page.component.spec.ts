import { ComponentFixture, TestBed } from '@angular/core/testing';
import { OrdenDetallePageComponent } from './orden-detalle-page.component';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { OrdenesApi } from '../data-access/ordenes.api';
import { AuditoriaApi } from '../../auditoria/data-access/auditoria.api';
import { AgendaApi } from '../../agenda/data-access/agenda.api';
import { Orden } from '../data-access/ordenes.models';

describe('OrdenDetallePageComponent', () => {
  let component: OrdenDetallePageComponent;
  let fixture: ComponentFixture<OrdenDetallePageComponent>;
  let ordenesApiStub: jasmine.SpyObj<OrdenesApi>;
  let auditoriaApiStub: jasmine.SpyObj<AuditoriaApi>;
  let agendaApiStub: jasmine.SpyObj<AgendaApi>;

  const ordenMock: Orden = {
    id: 'orden-1',
    clienteNombre: 'Juan',
    clienteTelefono: '555-1234',
    vehiculoPlaca: 'ABC123',
    vehiculoDescripcion: 'Toyota',
    aseguradora: null,
    trabajo: 'Reparación general',
    origen: 'ASESOR',
    estado: 'EN_REPARACION',
    siguienteEstado: 'REPARADA',
    accionesPermitidas: ['AVANZAR'],
    creadaEn: '2026-11-12T10:00:00',
    actualizadaEn: '2026-11-12T11:00:00',
  };

  const ordenConReabrir: Orden = {
    ...ordenMock,
    estado: 'CERRADA',
    siguienteEstado: null,
    accionesPermitidas: ['REABRIR'],
  };

  beforeEach(async () => {
    ordenesApiStub = jasmine.createSpyObj('OrdenesApi', [
      'obtener',
      'avanzar',
      'reabrir',
      'listar',
      'registrar',
    ]);
    auditoriaApiStub = jasmine.createSpyObj('AuditoriaApi', ['historial']);
    agendaApiStub = jasmine.createSpyObj('AgendaApi', [
      'citasDeOrden',
      'asesores',
      'disponibilidad',
      'agendar',
      'cancelar',
    ]);

    await TestBed.configureTestingModule({
      imports: [OrdenDetallePageComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: new Map([['id', 'orden-1']]) },
          },
        },
        { provide: OrdenesApi, useValue: ordenesApiStub },
        { provide: AuditoriaApi, useValue: auditoriaApiStub },
        { provide: AgendaApi, useValue: agendaApiStub },
      ],
    }).compileComponents();

    ordenesApiStub.obtener.and.returnValue(of(ordenMock));
    auditoriaApiStub.historial.and.returnValue(of([]));
    agendaApiStub.citasDeOrden.and.returnValue(of([]));
    agendaApiStub.asesores.and.returnValue(of([]));

    fixture = TestBed.createComponent(OrdenDetallePageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('06 REQ-2.2 muestra solo las acciones permitidas por la API', () => {
    expect(component.orden()).toEqual(ordenMock);
    expect(component.orden()!.accionesPermitidas).toContain('AVANZAR');
    expect(component.orden()!.accionesPermitidas).not.toContain('REABRIR');

    // Update to a state that allows REABRIR
    ordenesApiStub.obtener.and.returnValue(of(ordenConReabrir));
    component['cargarOrden']('orden-1');
    fixture.detectChanges();

    expect(component.orden()!.accionesPermitidas).toContain('REABRIR');
    expect(component.orden()!.accionesPermitidas).not.toContain('AVANZAR');
  });
});
