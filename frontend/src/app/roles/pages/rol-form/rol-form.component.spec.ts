import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { RolFormComponent } from './rol-form.component';
import { RolService } from '../../services/rol.service';

class RolServiceStub {
  listarPermisos = jasmine.createSpy('listarPermisos').and.returnValue(of([
    { id: 1, codigo: 'roles.ver', descripcion: 'Ver roles' },
    { id: 2, codigo: 'roles.crear', descripcion: 'Crear roles' }
  ]));
  crear = jasmine.createSpy('crear').and.returnValue(of({ id: 10 }));
  actualizar = jasmine.createSpy('actualizar').and.returnValue(of({ id: 10 }));
  obtener = jasmine.createSpy('obtener').and.returnValue(of({
    id: 10,
    nombre: 'Administrador',
    descripcion: 'Gestion total',
    activo: true,
    permisos: [{ id: 1, codigo: 'roles.ver', descripcion: 'Ver roles' }]
  }));
}

describe('RolFormComponent', () => {
  let fixture: ComponentFixture<RolFormComponent>;
  let component: RolFormComponent;
  let service: RolServiceStub;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    service = new RolServiceStub();
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [RolFormComponent],
      imports: [FormsModule, ReactiveFormsModule],
      providers: [
        { provide: RolService, useValue: service },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: new Map() } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RolFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('carga permisos disponibles y envia permisos seleccionados al crear', () => {
    component.rolForm.patchValue({ nombre: 'Administrador', descripcion: 'Gestion total' });
    component.togglePermiso(1, true);
    component.togglePermiso(2, true);

    expect(component.gruposPermisos.some((grupo) => grupo.codigo === 'roles')).toBeTrue();
    expect(component.totalSeleccionados).toBe(2);

    component.guardar();

    expect(service.crear).toHaveBeenCalledWith({
      nombre: 'Administrador',
      descripcion: 'Gestion total',
      permisosIds: [1, 2]
    });
    expect(router.navigate).toHaveBeenCalledWith(['/roles']);
  });
});
