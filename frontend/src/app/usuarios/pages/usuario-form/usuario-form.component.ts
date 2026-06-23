import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { RolResponse } from '../../../shared/models/rol.model';
import { MedicoResponse } from '../../../shared/models/medico.model';
import { UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-usuario-form',
  templateUrl: './usuario-form.component.html',
  styleUrl: './usuario-form.component.css',
  standalone: false
})
export class UsuarioFormComponent implements OnInit {
  usuarioId: number | null = null;
  roles: RolResponse[] = [];
  medicos: MedicoResponse[] = [];
  rolesSeleccionados = new Set<number>();
  medicosSeleccionados = new Set<number>();

  cargando = false;
  cargandoCatalogos = false;
  consultandoDni = false;
  guardando = false;
  mensajeError = '';
  mensajeInfo = '';

  usuarioForm = this.fb.group({
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    nombres: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(80)]],
    email: ['', [Validators.required, Validators.email]],
    telefono: ['', [Validators.pattern(/^\d{7,15}$/)]],
    fechaNacimiento: ['']
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly fb: FormBuilder,
    private readonly usuarioService: UsuarioService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.usuarioId = idParam ? Number(idParam) : null;
    this.cargarCatalogos();
  }

  get esSecretaria(): boolean {
    return Array.from(this.rolesSeleccionados).some((roleId) => {
      const rol = this.roles.find((r) => r.id === roleId);
      return rol?.nombre.toLowerCase() === 'secretaria';
    });
  }

  cargarCatalogos(): void {
    this.cargandoCatalogos = true;
    this.usuarioService.listarRoles().subscribe({
      next: (roles) => {
        this.roles = roles;
        this.usuarioService.listarMedicos().subscribe({
          next: (medicos) => {
            this.medicos = medicos;
            this.cargandoCatalogos = false;
            if (this.usuarioId) {
              this.cargarUsuario(this.usuarioId);
            }
          },
          error: () => {
            this.mensajeError = 'No se pudo cargar la lista de médicos.';
            this.cargandoCatalogos = false;
          }
        });
      },
      error: () => {
        this.mensajeError = 'No se pudo cargar la lista de roles.';
        this.cargandoCatalogos = false;
      }
    });
  }

  cargarUsuario(id: number): void {
    this.cargando = true;
    this.usuarioForm.get('dni')?.disable(); // DNI cannot be edited
    this.usuarioService.obtener(id)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (usuario) => {
          this.usuarioForm.patchValue({
            dni: usuario.dni,
            nombres: usuario.nombres,
            apellidos: usuario.apellidos,
            email: usuario.email,
            telefono: usuario.telefono,
            fechaNacimiento: usuario.fechaNacimiento
          });
          this.rolesSeleccionados = new Set(usuario.roles.map((r) => r.id));
          this.medicosSeleccionados = new Set(usuario.doctorIds ?? []);
        },
        error: () => (this.mensajeError = 'No se pudo cargar la información del empleado.')
      });
  }

  consultarDni(): void {
    this.mensajeError = '';
    this.mensajeInfo = '';
    const dniControl = this.usuarioForm.get('dni');
    dniControl?.markAsTouched();

    if (dniControl?.invalid) {
      return;
    }

    const dni = dniControl?.value ?? '';
    this.consultandoDni = true;
    this.usuarioService.consultarDni(dni)
      .pipe(finalize(() => (this.consultandoDni = false)))
      .subscribe({
        next: (info) => {
          this.usuarioForm.patchValue({ nombres: info.nombres, apellidos: info.apellidos });
          this.mensajeInfo = 'Datos de identidad autocompletados correctamente.';
        },
        error: () => {
          this.mensajeError = 'No se pudieron consultar los datos del DNI.';
        }
      });
  }

  toggleRol(rolId: number, checked: boolean): void {
    if (checked) {
      this.rolesSeleccionados.add(rolId);
    } else {
      this.rolesSeleccionados.delete(rolId);
    }
  }

  rolSeleccionado(rolId: number): boolean {
    return this.rolesSeleccionados.has(rolId);
  }

  toggleMedico(medicoId: number, checked: boolean): void {
    if (checked) {
      this.medicosSeleccionados.add(medicoId);
    } else {
      this.medicosSeleccionados.delete(medicoId);
    }
  }

  medicoSeleccionado(medicoId: number): boolean {
    return this.medicosSeleccionados.has(medicoId);
  }

  campoInvalido(campo: string): boolean {
    const control = this.usuarioForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }

  guardar(): void {
    this.mensajeError = '';
    this.mensajeInfo = '';
    this.usuarioForm.markAllAsTouched();

    if (this.usuarioForm.invalid) {
      return;
    }

    if (this.rolesSeleccionados.size === 0) {
      this.mensajeError = 'Debe seleccionar al menos un rol para el empleado.';
      return;
    }

    this.guardando = true;
    const raw = this.usuarioForm.getRawValue();

    const rolesIds = Array.from(this.rolesSeleccionados);
    const doctorIds = this.esSecretaria ? Array.from(this.medicosSeleccionados) : [];

    if (this.usuarioId) {
      const request = {
        nombres: raw.nombres ?? '',
        apellidos: raw.apellidos ?? '',
        email: raw.email ?? '',
        telefono: raw.telefono || undefined,
        fechaNacimiento: raw.fechaNacimiento || undefined,
        rolesIds,
        doctorIds
      };

      this.usuarioService.actualizar(this.usuarioId, request)
        .pipe(finalize(() => (this.guardando = false)))
        .subscribe({
          next: () => this.router.navigate(['/usuarios']),
          error: (err) => {
            this.mensajeError = err.error?.mensaje || 'No se pudo actualizar el empleado.';
          }
        });
    } else {
      const request = {
        dni: raw.dni ?? '',
        nombres: raw.nombres ?? '',
        apellidos: raw.apellidos ?? '',
        email: raw.email ?? '',
        telefono: raw.telefono || undefined,
        fechaNacimiento: raw.fechaNacimiento || undefined,
        rolesIds,
        doctorIds
      };

      this.usuarioService.crear(request)
        .pipe(finalize(() => (this.guardando = false)))
        .subscribe({
          next: () => this.router.navigate(['/usuarios']),
          error: (err) => {
            this.mensajeError = err.error?.mensaje || 'No se pudo crear el empleado. Verifique si el DNI o correo ya existen.';
          }
        });
    }
  }
}
