-- Modelo base de datos - Sistema interno de clinica
-- Script autocontenido para PostgreSQL. Se ejecuta automaticamente desde
-- /docker-entrypoint-initdb.d cuando el contenedor se crea por primera vez.

BEGIN;

CREATE TABLE rol (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);
COMMENT ON TABLE rol IS 'Roles de acceso del sistema interno.';
COMMENT ON COLUMN rol.nombre IS 'Nombre visible del rol. La unicidad se valida sin distinguir mayusculas.';

CREATE UNIQUE INDEX uk_rol_nombre_lower ON rol (LOWER(nombre));

CREATE TABLE permiso (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);
COMMENT ON TABLE permiso IS 'Permisos atomicos usados por RBAC.';
COMMENT ON COLUMN permiso.codigo IS 'Codigo estable usado por backend y frontend, por ejemplo citas.crear.';

CREATE TABLE rol_permiso (
    rol_id BIGINT NOT NULL REFERENCES rol(id) ON DELETE CASCADE,
    permiso_id BIGINT NOT NULL REFERENCES permiso(id) ON DELETE CASCADE,
    PRIMARY KEY (rol_id, permiso_id)
);
COMMENT ON TABLE rol_permiso IS 'Relacion muchos a muchos entre roles y permisos.';

CREATE TABLE sede (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);
COMMENT ON TABLE sede IS 'Sedes fisicas de la clinica.';

CREATE TABLE especialidad (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    especialidad_padre_id BIGINT REFERENCES especialidad(id) ON DELETE RESTRICT
);
COMMENT ON TABLE especialidad IS 'Especialidades y subespecialidades medicas.';
COMMENT ON COLUMN especialidad.especialidad_padre_id IS 'Referencia opcional para modelar subespecialidades.';

CREATE TABLE sede_especialidad (
    sede_id BIGINT NOT NULL REFERENCES sede(id) ON DELETE CASCADE,
    especialidad_id BIGINT NOT NULL REFERENCES especialidad(id) ON DELETE CASCADE,
    PRIMARY KEY (sede_id, especialidad_id)
);
COMMENT ON TABLE sede_especialidad IS 'Especialidades disponibles por sede.';

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    fecha_nacimiento DATE,
    password_hash VARCHAR(255) NOT NULL,
    cambio_password_obligatorio BOOLEAN NOT NULL DEFAULT TRUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);
COMMENT ON TABLE usuario IS 'Empleado interno con acceso al sistema.';
COMMENT ON COLUMN usuario.password_hash IS 'Hash BCrypt de la contrasena.';

CREATE TABLE usuario_rol (
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    rol_id BIGINT NOT NULL REFERENCES rol(id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, rol_id)
);
COMMENT ON TABLE usuario_rol IS 'Roles asignados a usuarios internos.';

CREATE TABLE doctor (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuario(id) ON DELETE RESTRICT,
    especialidad_id BIGINT NOT NULL REFERENCES especialidad(id) ON DELETE RESTRICT,
    subespecialidad_id BIGINT REFERENCES especialidad(id) ON DELETE RESTRICT
);
COMMENT ON TABLE doctor IS 'Perfil medico asociado uno a uno a un usuario.';

CREATE TABLE doctor_sede (
    doctor_id BIGINT NOT NULL REFERENCES doctor(id) ON DELETE CASCADE,
    sede_id BIGINT NOT NULL REFERENCES sede(id) ON DELETE CASCADE,
    PRIMARY KEY (doctor_id, sede_id)
);
COMMENT ON TABLE doctor_sede IS 'Sedes donde atiende cada doctor.';

CREATE TABLE secretaria (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuario(id) ON DELETE RESTRICT
);
COMMENT ON TABLE secretaria IS 'Perfil de secretaria asociado a un usuario.';

CREATE TABLE secretaria_doctor (
    secretaria_id BIGINT NOT NULL REFERENCES secretaria(id) ON DELETE CASCADE,
    doctor_id BIGINT NOT NULL REFERENCES doctor(id) ON DELETE CASCADE,
    PRIMARY KEY (secretaria_id, doctor_id)
);
COMMENT ON TABLE secretaria_doctor IS 'Medicos asignados a una secretaria para edicion de citas.';

CREATE TABLE enfermera (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuario(id) ON DELETE RESTRICT
);
COMMENT ON TABLE enfermera IS 'Perfil de enfermera asociado a un usuario.';

CREATE TABLE paciente (
    id BIGSERIAL PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    sexo VARCHAR(20),
    fecha_nacimiento DATE,
    telefono VARCHAR(20) NOT NULL,
    email VARCHAR(150) UNIQUE,
    password_hash VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);
COMMENT ON TABLE paciente IS 'Paciente de la clinica. El email es opcional para quienes no tienen cuenta web.';

CREATE TABLE justificacion (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctor(id) ON DELETE RESTRICT,
    texto TEXT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('cambio_horario', 'movimiento_cita'))
);
COMMENT ON TABLE justificacion IS 'Justificaciones obligatorias para cambios sensibles de agenda o citas.';

CREATE TABLE cita (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT NOT NULL REFERENCES paciente(id) ON DELETE RESTRICT,
    doctor_id BIGINT NOT NULL REFERENCES doctor(id) ON DELETE RESTRICT,
    sede_id BIGINT NOT NULL REFERENCES sede(id) ON DELETE RESTRICT,
    fecha_hora_inicio TIMESTAMP NOT NULL,
    fecha_hora_fin TIMESTAMP NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('programada', 'confirmada', 'cancelada', 'reprogramada', 'atendida', 'no_asistida')),
    estado_pago VARCHAR(20) NOT NULL DEFAULT 'pendiente' CHECK (estado_pago IN ('pendiente', 'pagado')),
    reprogramaciones_restantes INT NOT NULL DEFAULT 2 CHECK (reprogramaciones_restantes BETWEEN 0 AND 2),
    origen VARCHAR(20) NOT NULL CHECK (origen IN ('web', 'interno')),
    creado_por_usuario_id BIGINT REFERENCES usuario(id) ON DELETE SET NULL,
    justificacion_id BIGINT REFERENCES justificacion(id) ON DELETE SET NULL,
    CONSTRAINT ck_cita_rango_horario CHECK (fecha_hora_fin > fecha_hora_inicio),
    CONSTRAINT ck_cita_creador_origen CHECK (
        (origen = 'interno' AND creado_por_usuario_id IS NOT NULL)
        OR (origen = 'web')
    )
);
COMMENT ON TABLE cita IS 'Citas medicas programadas, reprogramadas, canceladas o atendidas.';

CREATE TABLE pago (
    id BIGSERIAL PRIMARY KEY,
    cita_id BIGINT NOT NULL UNIQUE REFERENCES cita(id) ON DELETE RESTRICT,
    monto DECIMAL(10, 2) NOT NULL CHECK (monto >= 0),
    metodo VARCHAR(20) NOT NULL CHECK (metodo IN ('efectivo', 'tarjeta', 'transferencia', 'web')),
    fecha_pago TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registrado_por_usuario_id BIGINT REFERENCES usuario(id) ON DELETE SET NULL
);
COMMENT ON TABLE pago IS 'Pago asociado a una cita. Solo puede existir un pago por cita.';

CREATE TABLE disponibilidad_base (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctor(id) ON DELETE CASCADE,
    sede_id BIGINT NOT NULL REFERENCES sede(id) ON DELETE CASCADE,
    dia_semana INT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    CONSTRAINT ck_disponibilidad_base_rango CHECK (hora_fin > hora_inicio),
    CONSTRAINT uk_disponibilidad_base_doctor_sede_dia UNIQUE (doctor_id, sede_id, dia_semana)
);
COMMENT ON TABLE disponibilidad_base IS 'Horario semanal recurrente de atencion por medico y sede.';
COMMENT ON COLUMN disponibilidad_base.dia_semana IS '1=Lunes, 7=Domingo.';

CREATE TABLE excepcion_disponibilidad (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctor(id) ON DELETE CASCADE,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    motivo TEXT,
    justificacion_id BIGINT REFERENCES justificacion(id) ON DELETE SET NULL,
    CONSTRAINT ck_excepcion_disponibilidad_rango CHECK (hora_fin > hora_inicio),
    CONSTRAINT uk_excepcion_disponibilidad_doctor_fecha_inicio UNIQUE (doctor_id, fecha, hora_inicio)
);
COMMENT ON TABLE excepcion_disponibilidad IS 'Bloques excepcionales de disponibilidad o indisponibilidad por medico.';

CREATE TABLE consulta (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT NOT NULL REFERENCES paciente(id) ON DELETE RESTRICT,
    doctor_id BIGINT NOT NULL REFERENCES doctor(id) ON DELETE RESTRICT,
    cita_id BIGINT REFERENCES cita(id) ON DELETE SET NULL,
    sede_id BIGINT NOT NULL REFERENCES sede(id) ON DELETE RESTRICT,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('consulta', 'control', 'urgencia', 'procedimiento')),
    motivo_consulta TEXT,
    diagnostico TEXT,
    observaciones TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'activa' CHECK (estado IN ('activa', 'finalizada', 'anulada'))
);
COMMENT ON TABLE consulta IS 'Entrada principal del historial clinico.';

CREATE TABLE receta (
    id BIGSERIAL PRIMARY KEY,
    consulta_id BIGINT NOT NULL REFERENCES consulta(id) ON DELETE CASCADE,
    medicamento VARCHAR(255) NOT NULL,
    dosis VARCHAR(100),
    frecuencia VARCHAR(100),
    duracion VARCHAR(100),
    indicaciones TEXT
);
COMMENT ON TABLE receta IS 'Medicamentos indicados dentro de una consulta.';

CREATE TABLE indicacion_medica (
    id BIGSERIAL PRIMARY KEY,
    consulta_id BIGINT NOT NULL REFERENCES consulta(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('reposo', 'derivacion', 'estudios')),
    descripcion TEXT
);
COMMENT ON TABLE indicacion_medica IS 'Indicaciones medicas no farmacologicas.';

CREATE TABLE estudio_complementario (
    id BIGSERIAL PRIMARY KEY,
    consulta_id BIGINT NOT NULL REFERENCES consulta(id) ON DELETE CASCADE,
    tipo_estudio VARCHAR(100),
    detalle TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'realizado')),
    archivo_resultado VARCHAR(255)
);
COMMENT ON TABLE estudio_complementario IS 'Estudios solicitados o registrados en una consulta.';

CREATE TABLE adjunto (
    id BIGSERIAL PRIMARY KEY,
    consulta_id BIGINT NOT NULL REFERENCES consulta(id) ON DELETE CASCADE,
    nombre_archivo VARCHAR(255),
    ruta VARCHAR(255),
    tipo_mime VARCHAR(100),
    fecha_subida TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE adjunto IS 'Archivos adjuntos asociados al historial clinico.';

CREATE TABLE nota_evolucion (
    id BIGSERIAL PRIMARY KEY,
    consulta_id BIGINT NOT NULL REFERENCES consulta(id) ON DELETE CASCADE,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nota TEXT NOT NULL,
    autor_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT
);
COMMENT ON TABLE nota_evolucion IS 'Notas de evolucion agregadas al historial clinico.';

CREATE TABLE codigo_verificacion (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    codigo VARCHAR(10) NOT NULL,
    tipo VARCHAR(30) NOT NULL CHECK (tipo IN ('recuperacion', 'verificacion_registro')),
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_expiracion TIMESTAMP NOT NULL
);
COMMENT ON TABLE codigo_verificacion IS 'Codigos temporales para recuperacion o verificacion por email.';

-- Indices sobre claves foraneas y columnas frecuentes de busqueda/filtro.
CREATE INDEX idx_rol_permiso_permiso_id ON rol_permiso(permiso_id);
CREATE INDEX idx_sede_activo ON sede(activo);
CREATE INDEX idx_especialidad_padre_id ON especialidad(especialidad_padre_id);
CREATE INDEX idx_sede_especialidad_especialidad_id ON sede_especialidad(especialidad_id);
CREATE INDEX idx_usuario_dni ON usuario(dni);
CREATE INDEX idx_usuario_email ON usuario(email);
CREATE INDEX idx_usuario_activo ON usuario(activo);
CREATE INDEX idx_usuario_rol_rol_id ON usuario_rol(rol_id);
CREATE INDEX idx_doctor_usuario_id ON doctor(usuario_id);
CREATE INDEX idx_doctor_especialidad_id ON doctor(especialidad_id);
CREATE INDEX idx_doctor_subespecialidad_id ON doctor(subespecialidad_id);
CREATE INDEX idx_doctor_sede_sede_id ON doctor_sede(sede_id);
CREATE INDEX idx_secretaria_usuario_id ON secretaria(usuario_id);
CREATE INDEX idx_secretaria_doctor_doctor_id ON secretaria_doctor(doctor_id);
CREATE INDEX idx_enfermera_usuario_id ON enfermera(usuario_id);
CREATE INDEX idx_paciente_dni ON paciente(dni);
CREATE INDEX idx_paciente_email ON paciente(email);
CREATE INDEX idx_paciente_activo ON paciente(activo);
CREATE INDEX idx_justificacion_doctor_id ON justificacion(doctor_id);
CREATE INDEX idx_justificacion_tipo ON justificacion(tipo);
CREATE INDEX idx_cita_paciente_id ON cita(paciente_id);
CREATE INDEX idx_cita_doctor_id ON cita(doctor_id);
CREATE INDEX idx_cita_sede_id ON cita(sede_id);
CREATE INDEX idx_cita_fecha_hora_inicio ON cita(fecha_hora_inicio);
CREATE INDEX idx_cita_estado ON cita(estado);
CREATE INDEX idx_cita_estado_pago ON cita(estado_pago);
CREATE INDEX idx_cita_creado_por_usuario_id ON cita(creado_por_usuario_id);
CREATE INDEX idx_cita_justificacion_id ON cita(justificacion_id);
CREATE INDEX idx_pago_registrado_por_usuario_id ON pago(registrado_por_usuario_id);
CREATE INDEX idx_pago_fecha_pago ON pago(fecha_pago);
CREATE INDEX idx_disponibilidad_base_doctor_id ON disponibilidad_base(doctor_id);
CREATE INDEX idx_disponibilidad_base_sede_id ON disponibilidad_base(sede_id);
CREATE INDEX idx_excepcion_disponibilidad_doctor_fecha ON excepcion_disponibilidad(doctor_id, fecha);
CREATE INDEX idx_excepcion_disponibilidad_justificacion_id ON excepcion_disponibilidad(justificacion_id);
CREATE INDEX idx_consulta_paciente_id ON consulta(paciente_id);
CREATE INDEX idx_consulta_doctor_id ON consulta(doctor_id);
CREATE INDEX idx_consulta_cita_id ON consulta(cita_id);
CREATE INDEX idx_consulta_sede_id ON consulta(sede_id);
CREATE INDEX idx_consulta_fecha_hora ON consulta(fecha_hora);
CREATE INDEX idx_receta_consulta_id ON receta(consulta_id);
CREATE INDEX idx_indicacion_medica_consulta_id ON indicacion_medica(consulta_id);
CREATE INDEX idx_estudio_complementario_consulta_id ON estudio_complementario(consulta_id);
CREATE INDEX idx_adjunto_consulta_id ON adjunto(consulta_id);
CREATE INDEX idx_nota_evolucion_consulta_id ON nota_evolucion(consulta_id);
CREATE INDEX idx_nota_evolucion_autor_id ON nota_evolucion(autor_id);
CREATE INDEX idx_codigo_verificacion_email ON codigo_verificacion(email);
CREATE INDEX idx_codigo_verificacion_codigo ON codigo_verificacion(codigo);
CREATE INDEX idx_codigo_verificacion_expiracion ON codigo_verificacion(fecha_expiracion);

-- Datos semilla esenciales.
INSERT INTO permiso (codigo, descripcion) VALUES
    ('roles.ver', 'Ver roles'),
    ('roles.crear', 'Crear roles'),
    ('roles.editar', 'Editar roles'),
    ('roles.eliminar', 'Eliminar roles'),
    ('usuarios.ver', 'Ver usuarios'),
    ('usuarios.crear', 'Crear usuarios'),
    ('usuarios.editar', 'Editar usuarios'),
    ('usuarios.desactivar', 'Desactivar usuarios'),
    ('sedes.ver', 'Ver sedes'),
    ('sedes.crear', 'Crear sedes'),
    ('sedes.editar', 'Editar sedes'),
    ('sedes.eliminar', 'Eliminar sedes'),
    ('especialidades.ver', 'Ver especialidades'),
    ('especialidades.crear', 'Crear especialidades'),
    ('especialidades.editar', 'Editar especialidades'),
    ('especialidades.eliminar', 'Eliminar especialidades'),
    ('citas.ver_todas', 'Ver todas las citas'),
    ('citas.ver_propias', 'Ver citas propias'),
    ('citas.ver_asignados', 'Ver citas de medicos asignados'),
    ('citas.crear', 'Crear citas'),
    ('citas.editar_propias', 'Editar citas propias'),
    ('citas.editar_asignados', 'Editar citas de medicos asignados'),
    ('citas.cancelar', 'Cancelar citas'),
    ('pagos.ver', 'Ver pagos'),
    ('pagos.crear', 'Crear pagos'),
    ('historial.ver_todos', 'Ver todos los historiales clinicos'),
    ('historial.ver_propios', 'Ver historiales clinicos propios'),
    ('historial.ver_basico', 'Ver historial clinico basico'),
    ('historial.crear', 'Crear historial clinico'),
    ('historial.editar', 'Editar historial clinico'),
    ('disponibilidad.ver_todas', 'Ver todas las disponibilidades'),
    ('disponibilidad.ver_propia', 'Ver disponibilidad propia'),
    ('disponibilidad.editar_propia', 'Editar disponibilidad propia'),
    ('justificaciones.ver_todas', 'Ver todas las justificaciones'),
    ('justificaciones.ver_propias', 'Ver justificaciones propias'),
    ('reportes.ver', 'Ver reportes'),
    ('dashboard.ver', 'Ver dashboard');

INSERT INTO rol (nombre, descripcion, activo)
VALUES ('Administrador', 'Rol administrador con acceso completo al sistema', TRUE);

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
CROSS JOIN permiso p
WHERE r.nombre = 'Administrador';

INSERT INTO usuario (
    dni,
    nombres,
    apellidos,
    email,
    password_hash,
    cambio_password_obligatorio,
    activo
) VALUES (
    '00000000',
    'Admin',
    'Sistema',
    'admin@clinica.com',
    '$2a$10$fGm9sT24fvKIf.jWDYJvzu9KrgsyYoRAO1lUWFL5wMTYTR6s9XAES',
    TRUE,
    TRUE
);

INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuario u
JOIN rol r ON r.nombre = 'Administrador'
WHERE u.dni = '00000000';

COMMIT;
