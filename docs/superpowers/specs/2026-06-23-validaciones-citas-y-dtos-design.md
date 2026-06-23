# Diseño: Validaciones de Citas y DTOs en Backend y Frontend

Este documento detalla el plan de diseño para implementar validaciones robustas tanto en el backend (Jakarta Bean Validation / Spring Boot) como en el frontend (Angular Reactive Forms), así como el mecanismo automatizado para la gestión de inasistencias a citas médicas.

---

## 1. Requerimiento: Validación de Fechas en "Nuevas Citas" (Presente/Futuro)

### Backend
En los DTOs que gestionan la programación o reprogramación de citas, validaremos que las fechas ingresadas por el usuario sean presentes o futuras mediante la anotación `@FutureOrPresent`.

- **[CitaCreateRequest.java](file:///home/rvelasco/Proyectos/clinica_v2.0/src/main/java/com/web/clinica/dto/request/CitaCreateRequest.java)**:
  ```java
  @NotNull(message = "La fecha y hora de inicio es obligatoria")
  @FutureOrPresent(message = "La fecha y hora de la cita debe ser presente o futura")
  private LocalDateTime fechaHoraInicio;
  ```
- **[CitaUpdateRequest.java](file:///home/rvelasco/Proyectos/clinica_v2.0/src/main/java/com/web/clinica/dto/request/CitaUpdateRequest.java)**:
  ```java
  @FutureOrPresent(message = "La nueva fecha y hora debe ser presente o futura")
  private LocalDateTime nuevaFechaHora;
  ```

### Frontend
Restringiremos la selección de fechas pasadas directamente en el calendario del frontend:
- **[crear-cita.component.ts](file:///home/rvelasco/Proyectos/clinica_v2.0/frontend/src/app/citas/pages/crear-cita/crear-cita.component.ts)**:
  Definir una propiedad de componente `today: string` en formato `yyyy-MM-dd` para limitar el calendario:
  ```typescript
  today = new Date().toISOString().split('T')[0];
  ```
- **[crear-cita.component.html](file:///home/rvelasco/Proyectos/clinica_v2.0/frontend/src/app/citas/pages/crear-cita/crear-cita.component.html)**:
  Añadir el atributo `[min]="today"` al input de fecha:
  ```html
  <input id="fecha" type="date" class="form-control" formControlName="fecha" [min]="today" ... />
  ```

---

## 2. Requerimiento: Lógica de Inasistencia ("no_asistida") para Citas Expiradas

### Arquitectura de la Tarea Programada (Scheduler)
Implementaremos un mecanismo en el backend para buscar periódicamente aquellas citas que han superado su horario sin ser atendidas, actualizando su estado a `"no_asistida"`.

1. **Habilitar Scheduling**:
   Añadir `@EnableScheduling` en [ClinicaApplication.java](file:///home/rvelasco/Proyectos/clinica_v2.0/src/main/java/com/web/clinica/ClinicaApplication.java).
2. **Crear CitaScheduler**:
   Crear un componente `CitaScheduler` en el paquete `com.web.clinica.scheduler`:
   ```java
   @Component
   @RequiredArgsConstructor
   @Slf4j
   public class CitaScheduler {
       private final CitaRepository citaRepository;

       // Se ejecuta cada 30 minutos (1800000 ms) o configurable por cron
       @Scheduled(fixedRate = 1800000)
       @Transactional
       public void procesarCitasVencidas() {
           LocalDateTime ahora = LocalDateTime.now();
           List<Cita> citasVencidas = citaRepository.buscarCitasVencidas(ahora, List.of("programada", "reprogramada"));
           
           for (Cita cita : citasVencidas) {
               cita.setEstado("no_asistida");
               citaRepository.save(cita);
               log.info("Cita ID {} del paciente ID {} marcada como no asistida", cita.getId(), cita.getPaciente().getId());
           }
       }
   }
   ```
3. **Consulta en CitaRepository**:
   Añadir el método en `CitaRepository` para buscar citas cuyo `fechaHoraFin` sea menor a la hora actual y su estado sea `"programada"` o `"reprogramada"`.

---

## 3. Requerimiento: Validaciones Adicionales en DTOs e Interfaz

Aplicaremos validaciones rigurosas sobre otros campos importantes para garantizar la calidad de la información:

### Pacientes y Médicos (Creación y Edición)
- **DNI**: Debe tener exactamente 8 caracteres numéricos.
  - *Backend:* `@Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 dígitos numéricos")`
  - *Frontend:* `Validators.pattern('^[0-9]{8}$')` e inputs con `maxlength="8"`.
- **Nombres y Apellidos**:
  - *Backend:* `@Size(min = 2, max = 100, message = "Debe tener entre 2 y 100 caracteres")`
  - *Frontend:* `Validators.minLength(2), Validators.maxLength(100)`.
- **Teléfono**:
  - *Backend:* `@Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos y puede comenzar con '+'")`
  - *Frontend:* `Validators.pattern('^\\+?[0-9]{7,15}$')`.
- **Fecha de Nacimiento**:
  - *Backend:* `@Past(message = "La fecha de nacimiento debe ser en el pasado")`
  - *Frontend:* Validación de fecha menor al día de hoy.
- **Sexo**:
  - *Backend:* `@Pattern(regexp = "^(M|F|Otro)$", message = "El sexo debe ser M, F o Otro")`

### Pagos (Creación)
- **Monto**:
  - *Backend:* `@DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")` en `PagoCreateRequest`.
  - *Frontend:* Validación de valor numérico mayor a cero.

### Disponibilidad y Excepciones
- **Excepciones de disponibilidad**:
  - *Backend:* `@FutureOrPresent` en la fecha de la excepción en `ExcepcionDisponibilidadCreateRequest`.
  - *Lógica de Servicio:* Asegurar que `horaFin` es después de `horaInicio` tanto en horario base como excepciones.

---

## 4. Plan de Pruebas
1. **Prueba Unitaria / Integración (Backend):**
   - Verificar que al intentar crear una cita en el pasado se devuelva un error de validación (HTTP 400).
   - Verificar que el scheduler marque correctamente como `no_asistida` una cita cuya hora final haya pasado.
2. **Pruebas de Interfaz (Frontend):**
   - Asegurarse de que el calendario de Nueva Cita tenga bloqueados los días anteriores a hoy.
   - Confirmar que los formularios de pacientes/médicos muestran mensajes de error apropiados si el DNI no tiene exactamente 8 dígitos.
