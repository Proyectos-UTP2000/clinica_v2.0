# Validaciones de Citas y DTOs - Plan de Implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement robust validations in backend DTOs and Angular frontend forms, and add an automated scheduled task to mark overdue appointments as `no_asistida`.

**Architecture:** We will use `jakarta.validation` annotations for Request DTOs, modify the Angular Reactive Forms configurations, and implement a Spring `@Scheduled` service that finds and updates expired appointments using a JPA Query.

**Tech Stack:** Java 17, Spring Boot, Spring Data JPA, Angular (Reactive Forms, SCSS).

## Global Constraints
- DNI must be exactly 8 digits (`^[0-9]{8}$`).
- Names and surnames length must be between 2 and 100 characters.
- "Nuevas Citas" must only accept present or future dates/times (`@FutureOrPresent`).
- Schedule task running every 30 minutes to update past appointments (where `fechaHoraFin` < current time) in state `programada` or `reprogramada` to `no_asistida`.

---

### Task 1: Validaciones en DTOs de Citas y Disponibilidad

**Files:**
- Modify: `src/main/java/com/web/clinica/dto/request/CitaCreateRequest.java`
- Modify: `src/main/java/com/web/clinica/dto/request/CitaUpdateRequest.java`
- Modify: `src/main/java/com/web/clinica/dto/request/ExcepcionDisponibilidadCreateRequest.java`
- Modify: `src/main/java/com/web/clinica/controller/CitaController.java`
- Create: `src/test/java/com/web/clinica/CitaValidationTests.java`

**Interfaces:**
- Consumes: None (relies on standard validation framework)
- Produces: `@Valid` validation constraints on appointment endpoints

- [ ] **Step 1: Write the failing tests**
  Create `src/test/java/com/web/clinica/CitaValidationTests.java` with test cases to check that validations are active:
  ```java
  package com.web.clinica;

  import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
  import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
  import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

  import com.fasterxml.jackson.databind.ObjectMapper;
  import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
  import com.web.clinica.controller.CitaController;
  import com.web.clinica.dto.request.CitaCreateRequest;
  import com.web.clinica.dto.request.CitaUpdateRequest;
  import com.web.clinica.service.abstractService.ICitaService;
  import java.time.LocalDateTime;
  import org.junit.jupiter.api.BeforeEach;
  import org.junit.jupiter.api.Test;
  import org.mockito.Mockito;
  import org.springframework.http.MediaType;
  import org.springframework.test.web.servlet.MockMvc;
  import org.springframework.test.web.servlet.setup.MockMvcBuilders;

  class CitaValidationTests {
      private MockMvc mockMvc;
      private ICitaService citaService;
      private ObjectMapper objectMapper;

      @BeforeEach
      void setup() {
          citaService = Mockito.mock(ICitaService.class);
          mockMvc = MockMvcBuilders.standaloneSetup(new CitaController(citaService)).build();
          objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
      }

      @Test
      void crearCitaConFechaPasadaFalla() throws Exception {
          CitaCreateRequest req = new CitaCreateRequest();
          req.setPacienteId(1L);
          req.setDoctorId(1L);
          req.setSedeId(1L);
          req.setConsultorioId(1L);
          req.setFechaHoraInicio(LocalDateTime.now().minusDays(1)); // fecha pasada
          req.setPagoAnticipado(false);

          mockMvc.perform(post("/api/citas")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(req)))
                  .andExpect(status().isBadRequest());
      }

      @Test
      void reprogramarCitaConFechaPasadaFalla() throws Exception {
          CitaUpdateRequest req = new CitaUpdateRequest();
          req.setNuevaFechaHora(LocalDateTime.now().minusDays(1)); // fecha pasada
          req.setDoctorId(1L);

          mockMvc.perform(put("/api/citas/1/reprogramar")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(req)))
                  .andExpect(status().isBadRequest());
      }
  }
  ```

- [ ] **Step 2: Run tests to verify they fail**
  Run: `./mvnw test -Dtest=CitaValidationTests`
  Expected: Failure because `@FutureOrPresent` and `@Valid` are not yet active on those endpoints and DTOs.

- [ ] **Step 3: Modify CitaCreateRequest to add @FutureOrPresent**
  Edit `src/main/java/com/web/clinica/dto/request/CitaCreateRequest.java` and add `@FutureOrPresent` annotation and import `jakarta.validation.constraints.FutureOrPresent`:
  ```java
      @NotNull
      @FutureOrPresent(message = "La fecha y hora de inicio de la cita debe ser presente o futura")
      private LocalDateTime fechaHoraInicio;
  ```

- [ ] **Step 4: Modify CitaUpdateRequest to add @FutureOrPresent**
  Edit `src/main/java/com/web/clinica/dto/request/CitaUpdateRequest.java` and add validation imports and annotations:
  ```java
  package com.web.clinica.dto.request;

  import jakarta.validation.constraints.FutureOrPresent;
  import jakarta.validation.constraints.NotNull;
  import java.time.LocalDateTime;
  import lombok.Data;

  @Data
  public class CitaUpdateRequest {

      private String estado;

      @NotNull(message = "Debe indicar la nueva fecha y hora")
      @FutureOrPresent(message = "La nueva fecha y hora debe ser presente o futura")
      private LocalDateTime nuevaFechaHora;

      private Long doctorId;
  }
  ```

- [ ] **Step 5: Modify ExcepcionDisponibilidadCreateRequest to add @FutureOrPresent**
  Edit `src/main/java/com/web/clinica/dto/request/ExcepcionDisponibilidadCreateRequest.java` to add `@FutureOrPresent` on the exception date:
  ```java
      @NotNull
      @jakarta.validation.constraints.FutureOrPresent(message = "La fecha de la excepción debe ser presente o futura")
      private LocalDate fecha;
  ```

- [ ] **Step 6: Update CitaController.java to enable @Valid on reprogramar**
  Edit `src/main/java/com/web/clinica/controller/CitaController.java:76` to add `@Valid` and remove the manual null check for `solicitud.getNuevaFechaHora()` which is now covered by `@NotNull` in DTO:
  ```java
      @PutMapping("/{id}/reprogramar")
      @RequierePermiso({"citas.editar_propias", "citas.editar_asignados"})
      public CitaResponse reprogramar(@PathVariable Long id, @Valid @RequestBody CitaUpdateRequest solicitud) {
          return citaService.reprogramar(id, solicitud.getNuevaFechaHora(), solicitud.getDoctorId());
      }
  ```

- [ ] **Step 7: Run tests to verify they pass**
  Run: `./mvnw test -Dtest=CitaValidationTests`
  Expected: PASS

- [ ] **Step 8: Commit Task 1**
  ```bash
  git add src/main/java/com/web/clinica/dto/request/CitaCreateRequest.java src/main/java/com/web/clinica/dto/request/CitaUpdateRequest.java src/main/java/com/web/clinica/dto/request/ExcepcionDisponibilidadCreateRequest.java src/main/java/com/web/clinica/controller/CitaController.java src/test/java/com/web/clinica/CitaValidationTests.java
  git commit -m "feat: add date validations for appointment creation and rescheduling"
  ```

---

### Task 2: Implementación de Tarea Programada para Citas "no_asistida"

**Files:**
- Modify: `src/main/java/com/web/clinica/ClinicaApplication.java`
- Modify: `src/main/java/com/web/clinica/repository/CitaRepository.java`
- Create: `src/main/java/com/web/clinica/scheduler/CitaScheduler.java`
- Create: `src/test/java/com/web/clinica/CitaSchedulerTests.java`

**Interfaces:**
- Consumes: `CitaRepository.buscarCitasVencidas`
- Produces: Periodic background task updating expired citations to "no_asistida"

- [ ] **Step 1: Write the failing tests**
  Create `src/test/java/com/web/clinica/CitaSchedulerTests.java`:
  ```java
  package com.web.clinica;

  import static org.assertj.core.api.Assertions.assertThat;
  import static org.mockito.ArgumentMatchers.any;
  import static org.mockito.ArgumentMatchers.eq;
  import static org.mockito.Mockito.mock;
  import static org.mockito.Mockito.times;
  import static org.mockito.Mockito.verify;
  import static org.mockito.Mockito.when;

  import com.web.clinica.model.Cita;
  import com.web.clinica.model.Paciente;
  import com.web.clinica.repository.CitaRepository;
  import com.web.clinica.scheduler.CitaScheduler;
  import java.time.LocalDateTime;
  import java.util.Arrays;
  import java.util.List;
  import org.junit.jupiter.api.Test;

  class CitaSchedulerTests {

      @Test
      void procesarCitasVencidasCambiaEstadoANoAsistida() {
          CitaRepository repository = mock(CitaRepository.class);
          CitaScheduler scheduler = new CitaScheduler(repository);

          Paciente paciente = new Paciente();
          paciente.setId(10L);

          Cita cita1 = new Cita();
          cita1.setId(1L);
          cita1.setEstado("programada");
          cita1.setPaciente(paciente);

          Cita cita2 = new Cita();
          cita2.setId(2L);
          cita2.setEstado("reprogramada");
          cita2.setPaciente(paciente);

          when(repository.buscarCitasVencidas(any(LocalDateTime.class), eq(Arrays.asList("programada", "reprogramada"))))
                  .thenReturn(Arrays.asList(cita1, cita2));

          scheduler.procesarCitasVencidas();

          assertThat(cita1.getEstado()).isEqualTo("no_asistida");
          assertThat(cita2.getEstado()).isEqualTo("no_asistida");
          verify(repository, times(1)).save(cita1);
          verify(repository, times(1)).save(cita2);
      }
  }
  ```

- [ ] **Step 2: Run tests to verify they fail**
  Run: `./mvnw test -Dtest=CitaSchedulerTests`
  Expected: Compile error (CitaScheduler class and repository methods don't exist yet).

- [ ] **Step 3: Modify ClinicaApplication to add @EnableScheduling**
  Edit `src/main/java/com/web/clinica/ClinicaApplication.java`:
  ```java
  package com.web.clinica;

  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.scheduling.annotation.EnableScheduling;

  @SpringBootApplication
  @EnableScheduling
  public class ClinicaApplication {

      public static void main(String[] args) {
          SpringApplication.run(ClinicaApplication.class, args);
      }
  }
  ```

- [ ] **Step 4: Modify CitaRepository to add query**
  Edit `src/main/java/com/web/clinica/repository/CitaRepository.java` to add `buscarCitasVencidas`:
  ```java
      @Query("SELECT c FROM Cita c WHERE c.fechaHoraFin < :ahora AND c.estado IN :estados")
      List<Cita> buscarCitasVencidas(@Param("ahora") LocalDateTime ahora, @Param("estados") List<String> estados);
  ```

- [ ] **Step 5: Create CitaScheduler class**
  Create `src/main/java/com/web/clinica/scheduler/CitaScheduler.java`:
  ```java
  package com.web.clinica.scheduler;

  import com.web.clinica.model.Cita;
  import com.web.clinica.repository.CitaRepository;
  import java.time.LocalDateTime;
  import java.util.Arrays;
  import java.util.List;
  import lombok.RequiredArgsConstructor;
  import lombok.extern.slf4j.Slf4j;
  import org.springframework.scheduling.annotation.Scheduled;
  import org.springframework.stereotype.Component;
  import org.springframework.transaction.annotation.Transactional;

  @Component
  @RequiredArgsConstructor
  @Slf4j
  public class CitaScheduler {

      private final CitaRepository citaRepository;

      // Corre cada 30 minutos (1800000 milisegundos)
      @Scheduled(fixedRate = 1800000)
      @Transactional
      public void procesarCitasVencidas() {
          LocalDateTime ahora = LocalDateTime.now();
          List<String> estadosActivos = Arrays.asList("programada", "reprogramada");
          List<Cita> vencidas = citaRepository.buscarCitasVencidas(ahora, estadosActivos);

          if (!vencidas.isEmpty()) {
              log.info("Iniciando procesamiento de {} citas vencidas para marcar inasistencia", vencidas.size());
              for (Cita cita : vencidas) {
                  cita.setEstado("no_asistida");
                  citaRepository.save(cita);
                  log.info("Cita ID {} (Paciente ID {}) marcada como no_asistida debido a vencimiento", cita.getId(), cita.getPaciente().getId());
              }
          }
      }
  }
  ```

- [ ] **Step 6: Run tests to verify they pass**
  Run: `./mvnw test -Dtest=CitaSchedulerTests`
  Expected: PASS

- [ ] **Step 7: Commit Task 2**
  ```bash
  git add src/main/java/com/web/clinica/ClinicaApplication.java src/main/java/com/web/clinica/repository/CitaRepository.java src/main/java/com/web/clinica/scheduler/CitaScheduler.java src/test/java/com/web/clinica/CitaSchedulerTests.java
  git commit -m "feat: add EnableScheduling and CitaScheduler background task for no_asistida status"
  ```

---

### Task 3: Validaciones Adicionales en DTOs (Pacientes, Médicos, Pagos)

**Files:**
- Modify: `src/main/java/com/web/clinica/dto/request/PacienteCreateRequest.java`
- Modify: `src/main/java/com/web/clinica/dto/request/PacienteUpdateRequest.java`
- Modify: `src/main/java/com/web/clinica/dto/request/MedicoCreateRequest.java`
- Modify: `src/main/java/com/web/clinica/dto/request/PagoCreateRequest.java`
- Create: `src/test/java/com/web/clinica/DtoAdditionalValidationTests.java`

**Interfaces:**
- Consumes: None
- Produces: Strict annotations for incoming DTO attributes

- [ ] **Step 1: Write the failing tests**
  Create `src/test/java/com/web/clinica/DtoAdditionalValidationTests.java` to test validation on DNI, names/surnames, phone, birth dates, and payment amounts:
  ```java
  package com.web.clinica;

  import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
  import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

  import com.fasterxml.jackson.databind.ObjectMapper;
  import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
  import com.web.clinica.controller.PacienteController;
  import com.web.clinica.controller.PagoController;
  import com.web.clinica.dto.request.PacienteCreateRequest;
  import com.web.clinica.dto.request.PagoCreateRequest;
  import com.web.clinica.service.abstractService.IPacienteService;
  import com.web.clinica.service.abstractService.IPagoService;
  import java.math.BigDecimal;
  import java.time.LocalDate;
  import org.junit.jupiter.api.BeforeEach;
  import org.junit.jupiter.api.Test;
  import org.mockito.Mockito;
  import org.springframework.http.MediaType;
  import org.springframework.test.web.servlet.MockMvc;
  import org.springframework.test.web.servlet.setup.MockMvcBuilders;

  class DtoAdditionalValidationTests {
      private MockMvc pacienteMockMvc;
      private MockMvc pagoMockMvc;
      private ObjectMapper objectMapper;

      @BeforeEach
      void setup() {
          IPacienteService pacienteService = Mockito.mock(IPacienteService.class);
          IPagoService pagoService = Mockito.mock(IPagoService.class);
          pacienteMockMvc = MockMvcBuilders.standaloneSetup(new PacienteController(pacienteService)).build();
          pagoMockMvc = MockMvcBuilders.standaloneSetup(new PagoController(pagoService)).build();
          objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
      }

      @Test
      void crearPacienteConDniLargoFalla() throws Exception {
          PacienteCreateRequest req = new PacienteCreateRequest();
          req.setDni("123456789"); // 9 digitos
          req.setNombres("Juan");
          req.setApellidos("Perez");
          req.setFechaNacimiento(LocalDate.now().minusYears(20));
          req.setTelefono("987654321");

          pacienteMockMvc.perform(post("/api/pacientes")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(req)))
                  .andExpect(status().isBadRequest());
      }

      @Test
      void registrarPagoConMontoCeroFalla() throws Exception {
          PagoCreateRequest req = new PagoCreateRequest();
          req.setCitaId(1L);
          req.setMonto(new BigDecimal("0.00")); // monto cero
          req.setMetodo("tarjeta");

          pagoMockMvc.perform(post("/api/pagos")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(req)))
                  .andExpect(status().isBadRequest());
      }
  }
  ```

- [ ] **Step 2: Run tests to verify they fail**
  Run: `./mvnw test -Dtest=DtoAdditionalValidationTests`
  Expected: Failure (monto 0.00 is accepted, DNI of 9 digits is accepted).

- [ ] **Step 3: Modify PacienteCreateRequest validations**
  Edit `src/main/java/com/web/clinica/dto/request/PacienteCreateRequest.java` to set strict limits on DNI, names, phone, birth date, and sex:
  ```java
  package com.web.clinica.dto.request;

  import jakarta.validation.constraints.Email;
  import jakarta.validation.constraints.NotBlank;
  import jakarta.validation.constraints.NotNull;
  import jakarta.validation.constraints.Past;
  import jakarta.validation.constraints.Pattern;
  import jakarta.validation.constraints.Size;
  import java.time.LocalDate;
  import lombok.Data;

  @Data
  public class PacienteCreateRequest {

      @NotBlank
      @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 dígitos numéricos")
      private String dni;

      @NotBlank
      @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
      private String nombres;

      @NotBlank
      @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
      private String apellidos;

      @Pattern(regexp = "^(M|F|Otro)$", message = "El sexo debe ser M, F o Otro")
      private String sexo;

      @NotNull
      @Past(message = "La fecha de nacimiento debe ser en el pasado")
      private LocalDate fechaNacimiento;

      @NotBlank
      @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos y puede comenzar con '+'")
      private String telefono;

      @Email
      private String email;

      private String password;
  }
  ```

- [ ] **Step 4: Modify PacienteUpdateRequest validations**
  Edit `src/main/java/com/web/clinica/dto/request/PacienteUpdateRequest.java` to align with Create validations:
  ```java
  package com.web.clinica.dto.request;

  import jakarta.validation.constraints.Email;
  import jakarta.validation.constraints.NotBlank;
  import jakarta.validation.constraints.NotNull;
  import jakarta.validation.constraints.Past;
  import jakarta.validation.constraints.Pattern;
  import jakarta.validation.constraints.Size;
  import java.time.LocalDate;
  import lombok.Data;

  @Data
  public class PacienteUpdateRequest {

      @NotBlank
      @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
      private String nombres;

      @NotBlank
      @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
      private String apellidos;

      @Pattern(regexp = "^(M|F|Otro)$", message = "El sexo debe ser M, F o Otro")
      private String sexo;

      @NotNull
      @Past(message = "La fecha de nacimiento debe ser en el pasado")
      private LocalDate fechaNacimiento;

      @NotBlank
      @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos y puede comenzar con '+'")
      private String telefono;

      @Email
      private String email;

      private String password;
  }
  ```

- [ ] **Step 5: Modify MedicoCreateRequest validations**
  Edit `src/main/java/com/web/clinica/dto/request/MedicoCreateRequest.java` to align validations:
  ```java
  package com.web.clinica.dto.request;

  import jakarta.validation.constraints.Email;
  import jakarta.validation.constraints.NotBlank;
  import jakarta.validation.constraints.NotNull;
  import jakarta.validation.constraints.Past;
  import jakarta.validation.constraints.Pattern;
  import jakarta.validation.constraints.Positive;
  import jakarta.validation.constraints.Size;
  import java.time.LocalDate;
  import java.util.List;
  import lombok.Data;

  @Data
  public class MedicoCreateRequest {

      @NotBlank
      @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 dígitos numéricos")
      private String dni;

      @NotBlank
      @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
      private String nombres;

      @NotBlank
      @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
      private String apellidos;

      @Email
      @NotBlank
      private String email;

      @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos y puede comenzar con '+'")
      private String telefono;

      @Past(message = "La fecha de nacimiento debe ser en el pasado")
      private LocalDate fechaNacimiento;

      @NotNull
      @Positive(message = "El ID de la especialidad debe ser un número positivo")
      private Long especialidadId;

      private Long subespecialidadId;
      private List<Long> sedesIds;
      private List<Long> consultorioIds;
  }
  ```

- [ ] **Step 6: Modify PagoCreateRequest validations**
  Edit `src/main/java/com/web/clinica/dto/request/PagoCreateRequest.java` to restrict payment amounts to positive:
  ```java
  package com.web.clinica.dto.request;

  import jakarta.validation.constraints.DecimalMin;
  import jakarta.validation.constraints.NotBlank;
  import jakarta.validation.constraints.NotNull;
  import java.math.BigDecimal;
  import lombok.Data;

  @Data
  public class PagoCreateRequest {

      @NotNull
      private Long citaId;

      @NotNull
      @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
      private BigDecimal monto;

      @NotBlank
      private String metodo;
  }
  ```

- [ ] **Step 7: Run tests to verify they pass**
  Run: `./mvnw test -Dtest=DtoAdditionalValidationTests`
  Expected: PASS

- [ ] **Step 8: Commit Task 3**
  ```bash
  git add src/main/java/com/web/clinica/dto/request/PacienteCreateRequest.java src/main/java/com/web/clinica/dto/request/PacienteUpdateRequest.java src/main/java/com/web/clinica/dto/request/MedicoCreateRequest.java src/main/java/com/web/clinica/dto/request/PagoCreateRequest.java src/test/java/com/web/clinica/DtoAdditionalValidationTests.java
  git commit -m "feat: add strict validations to Paciente, Medico and Pago DTOs"
  ```

---

### Task 4: Validaciones en el Frontend (Angular)

**Files:**
- Modify: `frontend/src/app/citas/pages/crear-cita/crear-cita.component.ts`
- Modify: `frontend/src/app/citas/pages/crear-cita/crear-cita.component.html`
- Modify: `frontend/src/app/pacientes/pages/crear-paciente/crear-paciente.component.ts`
- Modify: `frontend/src/app/pacientes/pages/editar-paciente/editar-paciente.component.ts`

**Interfaces:**
- Consumes: Reactive Forms validator states
- Produces: Real-time UI restrictions on invalid entries

- [ ] **Step 1: Modify crear-cita.component.ts for min date**
  Edit `frontend/src/app/citas/pages/crear-cita/crear-cita.component.ts`:
  Add `today: string = '';` as a property. In `ngOnInit()`, set it to today's date:
  ```typescript
  // En las propiedades de CrearCitaComponent
  today: string = new Date().toISOString().split('T')[0];
  ```

- [ ] **Step 2: Modify crear-cita.component.html to set min attribute**
  Edit `frontend/src/app/citas/pages/crear-cita/crear-cita.component.html` at the `fecha` input (line 64):
  ```html
  <input id="fecha" type="date" class="form-control" formControlName="fecha" [min]="today" [class.is-invalid]="campoInvalido('fecha')" />
  ```

- [ ] **Step 3: Update Paciente Forms validations in frontend**
  Edit `frontend/src/app/pacientes/pages/crear-paciente/crear-paciente.component.ts` and change validators for names and surnames to match backend size:
  ```typescript
    pacienteForm = this.fb.group({
      dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      nombres: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      apellidos: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      sexo: [''],
      fechaNacimiento: ['', [Validators.required]],
      telefono: ['', [Validators.required, Validators.pattern(/^\d{7,15}$/)]],
      email: ['', [Validators.email]]
    });
  ```

- [ ] **Step 4: Update Paciente Edit validations in frontend**
  Edit `frontend/src/app/pacientes/pages/editar-paciente/editar-paciente.component.ts`:
  ```typescript
    pacienteForm = this.fb.group({
      nombres: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      apellidos: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      sexo: [''],
      fechaNacimiento: ['', [Validators.required]],
      telefono: ['', [Validators.required, Validators.pattern(/^\d{7,15}$/)]],
      email: ['', [Validators.email]]
    });
  ```

- [ ] **Step 5: Run tests and verify the frontend builds successfully**
  Run: `npm run build` or `npx ng build` in the `frontend` folder.
  Expected: Successful compilation without TypeScript errors.

- [ ] **Step 6: Commit Task 4**
  ```bash
  git add frontend/src/app/citas/pages/crear-cita/crear-cita.component.ts frontend/src/app/citas/pages/crear-cita/crear-cita.component.html frontend/src/app/pacientes/pages/crear-paciente/crear-paciente.component.ts frontend/src/app/pacientes/pages/editar-paciente/editar-paciente.component.ts
  git commit -m "feat: implement frontend form validations matching the backend rules"
  ```
