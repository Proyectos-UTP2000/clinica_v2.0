import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { SidebarComponent } from './sidebar.component';
import { AuthService } from '../../core/services/auth.service';

class AuthServiceStub {
  permissions = new Set<string>();
  hasPermission(permission: string): boolean {
    return this.permissions.has(permission);
  }
}

describe('SidebarComponent roles link', () => {
  let fixture: ComponentFixture<SidebarComponent>;
  let component: SidebarComponent;
  let auth: AuthServiceStub;

  beforeEach(async () => {
    auth = new AuthServiceStub();
    await TestBed.configureTestingModule({
      declarations: [SidebarComponent],
      imports: [RouterModule.forRoot([])],
      providers: [{ provide: AuthService, useValue: auth }]
    }).compileComponents();

    fixture = TestBed.createComponent(SidebarComponent);
    component = fixture.componentInstance;
  });

  it('muestra Roles bajo Administracion solo con roles.ver', () => {
    expect(component.visiblesPorGrupo('Administracion').some((item) => item.route === '/roles')).toBeFalse();

    auth.permissions.add('roles.ver');

    expect(component.visiblesPorGrupo('Administracion').some((item) => item.route === '/roles')).toBeTrue();
  });
});
