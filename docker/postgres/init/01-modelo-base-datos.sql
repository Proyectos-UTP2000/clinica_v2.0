--
-- PostgreSQL database dump
--


-- Dumped from database version 16.14
-- Dumped by pg_dump version 18.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: clinica_db; Type: DATABASE; Schema: -; Owner: clinica
--





SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: adjunto; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.adjunto (
    id bigint NOT NULL,
    consulta_id bigint NOT NULL,
    nombre_archivo character varying(255),
    ruta character varying(255),
    tipo_mime character varying(100),
    fecha_subida timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.adjunto OWNER TO clinica;

--
-- Name: TABLE adjunto; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.adjunto IS 'Archivos adjuntos asociados al historial clinico.';


--
-- Name: adjunto_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.adjunto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.adjunto_id_seq OWNER TO clinica;

--
-- Name: adjunto_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.adjunto_id_seq OWNED BY public.adjunto.id;


--
-- Name: audit_log; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.audit_log (
    id bigint NOT NULL,
    usuario_dni character varying(20),
    usuario_nombre character varying(100),
    accion character varying(100) NOT NULL,
    detalles text,
    ip_address character varying(45),
    estado character varying(20) DEFAULT 'EXITOSO'::character varying NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.audit_log OWNER TO clinica;

--
-- Name: audit_log_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.audit_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.audit_log_id_seq OWNER TO clinica;

--
-- Name: audit_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.audit_log_id_seq OWNED BY public.audit_log.id;


--
-- Name: caja_diaria; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.caja_diaria (
    id bigint NOT NULL,
    fecha date NOT NULL,
    monto_apertura numeric(10,2) NOT NULL,
    monto_cierre numeric(10,2),
    ingresos numeric(10,2) DEFAULT 0 NOT NULL,
    egresos numeric(10,2) DEFAULT 0 NOT NULL,
    balance_real numeric(10,2),
    diferencia numeric(10,2),
    estado character varying(20) NOT NULL,
    fecha_apertura timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_cierre timestamp without time zone,
    abierto_por_usuario_id bigint,
    cerrado_por_usuario_id bigint,
    observaciones text,
    CONSTRAINT caja_diaria_egresos_check CHECK ((egresos >= (0)::numeric)),
    CONSTRAINT caja_diaria_estado_check CHECK (((estado)::text = ANY ((ARRAY['abierta'::character varying, 'cerrada'::character varying])::text[]))),
    CONSTRAINT caja_diaria_ingresos_check CHECK ((ingresos >= (0)::numeric)),
    CONSTRAINT caja_diaria_monto_apertura_check CHECK ((monto_apertura >= (0)::numeric)),
    CONSTRAINT caja_diaria_monto_cierre_check CHECK ((monto_cierre >= (0)::numeric))
);


ALTER TABLE public.caja_diaria OWNER TO clinica;

--
-- Name: TABLE caja_diaria; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.caja_diaria IS 'Registro diario de apertura y cierre de caja.';


--
-- Name: caja_diaria_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.caja_diaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.caja_diaria_id_seq OWNER TO clinica;

--
-- Name: caja_diaria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.caja_diaria_id_seq OWNED BY public.caja_diaria.id;


--
-- Name: cita; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.cita (
    id bigint NOT NULL,
    paciente_id bigint NOT NULL,
    doctor_id bigint NOT NULL,
    sede_id bigint NOT NULL,
    fecha_hora_inicio timestamp without time zone NOT NULL,
    fecha_hora_fin timestamp without time zone NOT NULL,
    estado character varying(20) NOT NULL,
    estado_pago character varying(20) DEFAULT 'pendiente'::character varying NOT NULL,
    pago_anticipado boolean DEFAULT false NOT NULL,
    reprogramaciones_restantes integer DEFAULT 2 NOT NULL,
    origen character varying(20) NOT NULL,
    creado_por_usuario_id bigint,
    justificacion_id bigint,
    consultorio_id bigint NOT NULL,
    CONSTRAINT cita_estado_check CHECK (((estado)::text = ANY ((ARRAY['programada'::character varying, 'confirmada'::character varying, 'cancelada'::character varying, 'reprogramada'::character varying, 'atendida'::character varying, 'no_asistida'::character varying, 'en_espera'::character varying])::text[]))),
    CONSTRAINT cita_estado_pago_check CHECK (((estado_pago)::text = ANY ((ARRAY['pendiente'::character varying, 'pagado'::character varying])::text[]))),
    CONSTRAINT cita_origen_check CHECK (((origen)::text = ANY ((ARRAY['web'::character varying, 'interno'::character varying])::text[]))),
    CONSTRAINT cita_reprogramaciones_restantes_check CHECK (((reprogramaciones_restantes >= 0) AND (reprogramaciones_restantes <= 2))),
    CONSTRAINT ck_cita_creador_origen CHECK (((((origen)::text = 'interno'::text) AND (creado_por_usuario_id IS NOT NULL)) OR ((origen)::text = 'web'::text))),
    CONSTRAINT ck_cita_rango_horario CHECK ((fecha_hora_fin > fecha_hora_inicio))
);


ALTER TABLE public.cita OWNER TO clinica;

--
-- Name: TABLE cita; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.cita IS 'Citas medicas programadas, reprogramadas, canceladas o atendidas.';


--
-- Name: cita_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.cita_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cita_id_seq OWNER TO clinica;

--
-- Name: cita_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.cita_id_seq OWNED BY public.cita.id;


--
-- Name: codigo_verificacion; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.codigo_verificacion (
    id bigint NOT NULL,
    email character varying(150) NOT NULL,
    codigo character varying(10) NOT NULL,
    tipo character varying(30) NOT NULL,
    usado boolean DEFAULT false NOT NULL,
    fecha_expiracion timestamp without time zone NOT NULL,
    CONSTRAINT codigo_verificacion_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['recuperacion'::character varying, 'verificacion_registro'::character varying])::text[])))
);


ALTER TABLE public.codigo_verificacion OWNER TO clinica;

--
-- Name: TABLE codigo_verificacion; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.codigo_verificacion IS 'Codigos temporales para recuperacion o verificacion por email.';


--
-- Name: codigo_verificacion_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.codigo_verificacion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.codigo_verificacion_id_seq OWNER TO clinica;

--
-- Name: codigo_verificacion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.codigo_verificacion_id_seq OWNED BY public.codigo_verificacion.id;


--
-- Name: configuracion_global; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.configuracion_global (
    clave character varying(50) NOT NULL,
    valor text NOT NULL
);


ALTER TABLE public.configuracion_global OWNER TO clinica;

--
-- Name: consulta; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.consulta (
    id bigint NOT NULL,
    paciente_id bigint NOT NULL,
    doctor_id bigint NOT NULL,
    cita_id bigint,
    sede_id bigint NOT NULL,
    fecha_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tipo character varying(20) NOT NULL,
    motivo_consulta text,
    diagnostico text,
    observaciones text,
    estado character varying(20) DEFAULT 'activa'::character varying NOT NULL,
    CONSTRAINT consulta_estado_check CHECK (((estado)::text = ANY ((ARRAY['activa'::character varying, 'finalizada'::character varying, 'anulada'::character varying])::text[]))),
    CONSTRAINT consulta_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['consulta'::character varying, 'control'::character varying, 'urgencia'::character varying, 'procedimiento'::character varying])::text[])))
);


ALTER TABLE public.consulta OWNER TO clinica;

--
-- Name: TABLE consulta; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.consulta IS 'Entrada principal del historial clinico.';


--
-- Name: consulta_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.consulta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.consulta_id_seq OWNER TO clinica;

--
-- Name: consulta_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.consulta_id_seq OWNED BY public.consulta.id;


--
-- Name: consultorio; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.consultorio (
    id bigint NOT NULL,
    sede_id bigint NOT NULL,
    nombre character varying(50) NOT NULL,
    piso character varying(10),
    area character varying(50),
    activo boolean DEFAULT true
);


ALTER TABLE public.consultorio OWNER TO clinica;

--
-- Name: TABLE consultorio; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.consultorio IS 'Consultorios fisicos de cada sede.';


--
-- Name: consultorio_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.consultorio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.consultorio_id_seq OWNER TO clinica;

--
-- Name: consultorio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.consultorio_id_seq OWNED BY public.consultorio.id;


--
-- Name: disponibilidad_base; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.disponibilidad_base (
    id bigint NOT NULL,
    doctor_id bigint NOT NULL,
    sede_id bigint NOT NULL,
    consultorio_id bigint,
    dia_semana integer NOT NULL,
    hora_inicio time without time zone NOT NULL,
    hora_fin time without time zone NOT NULL,
    CONSTRAINT ck_disponibilidad_base_rango CHECK ((hora_fin > hora_inicio)),
    CONSTRAINT disponibilidad_base_dia_semana_check CHECK (((dia_semana >= 1) AND (dia_semana <= 7)))
);


ALTER TABLE public.disponibilidad_base OWNER TO clinica;

--
-- Name: TABLE disponibilidad_base; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.disponibilidad_base IS 'Horario semanal recurrente de atencion por medico y sede.';


--
-- Name: COLUMN disponibilidad_base.dia_semana; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON COLUMN public.disponibilidad_base.dia_semana IS '1=Lunes, 7=Domingo.';


--
-- Name: disponibilidad_base_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.disponibilidad_base_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.disponibilidad_base_id_seq OWNER TO clinica;

--
-- Name: disponibilidad_base_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.disponibilidad_base_id_seq OWNED BY public.disponibilidad_base.id;


--
-- Name: doctor; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.doctor (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    especialidad_id bigint NOT NULL,
    subespecialidad_id bigint
);


ALTER TABLE public.doctor OWNER TO clinica;

--
-- Name: TABLE doctor; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.doctor IS 'Perfil medico asociado uno a uno a un usuario.';


--
-- Name: doctor_consultorio; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.doctor_consultorio (
    doctor_id bigint NOT NULL,
    consultorio_id bigint NOT NULL
);


ALTER TABLE public.doctor_consultorio OWNER TO clinica;

--
-- Name: TABLE doctor_consultorio; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.doctor_consultorio IS 'Relacion de consultorios preferidos/asignados por doctor.';


--
-- Name: doctor_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.doctor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.doctor_id_seq OWNER TO clinica;

--
-- Name: doctor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.doctor_id_seq OWNED BY public.doctor.id;


--
-- Name: doctor_sede; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.doctor_sede (
    doctor_id bigint NOT NULL,
    sede_id bigint NOT NULL
);


ALTER TABLE public.doctor_sede OWNER TO clinica;

--
-- Name: TABLE doctor_sede; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.doctor_sede IS 'Sedes donde atiende cada doctor.';


--
-- Name: enfermera; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.enfermera (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL
);


ALTER TABLE public.enfermera OWNER TO clinica;

--
-- Name: TABLE enfermera; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.enfermera IS 'Perfil de enfermera asociado a un usuario.';


--
-- Name: enfermera_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.enfermera_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.enfermera_id_seq OWNER TO clinica;

--
-- Name: enfermera_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.enfermera_id_seq OWNED BY public.enfermera.id;


--
-- Name: especialidad; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.especialidad (
    id bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion character varying(255),
    especialidad_padre_id bigint
);


ALTER TABLE public.especialidad OWNER TO clinica;

--
-- Name: TABLE especialidad; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.especialidad IS 'Especialidades y subespecialidades medicas.';


--
-- Name: COLUMN especialidad.especialidad_padre_id; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON COLUMN public.especialidad.especialidad_padre_id IS 'Referencia opcional para modelar subespecialidades.';


--
-- Name: especialidad_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.especialidad_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.especialidad_id_seq OWNER TO clinica;

--
-- Name: especialidad_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.especialidad_id_seq OWNED BY public.especialidad.id;


--
-- Name: estudio_complementario; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.estudio_complementario (
    id bigint NOT NULL,
    consulta_id bigint NOT NULL,
    tipo_estudio character varying(100),
    detalle text,
    estado character varying(20) DEFAULT 'pendiente'::character varying NOT NULL,
    archivo_resultado character varying(255),
    CONSTRAINT estudio_complementario_estado_check CHECK (((estado)::text = ANY ((ARRAY['pendiente'::character varying, 'realizado'::character varying])::text[])))
);


ALTER TABLE public.estudio_complementario OWNER TO clinica;

--
-- Name: TABLE estudio_complementario; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.estudio_complementario IS 'Estudios solicitados o registrados en una consulta.';


--
-- Name: estudio_complementario_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.estudio_complementario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.estudio_complementario_id_seq OWNER TO clinica;

--
-- Name: estudio_complementario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.estudio_complementario_id_seq OWNED BY public.estudio_complementario.id;


--
-- Name: excepcion_disponibilidad; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.excepcion_disponibilidad (
    id bigint NOT NULL,
    doctor_id bigint NOT NULL,
    fecha date NOT NULL,
    hora_inicio time without time zone NOT NULL,
    hora_fin time without time zone NOT NULL,
    motivo text,
    justificacion_id bigint,
    CONSTRAINT ck_excepcion_disponibilidad_rango CHECK ((hora_fin > hora_inicio))
);


ALTER TABLE public.excepcion_disponibilidad OWNER TO clinica;

--
-- Name: TABLE excepcion_disponibilidad; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.excepcion_disponibilidad IS 'Bloques excepcionales de disponibilidad o indisponibilidad por medico.';


--
-- Name: excepcion_disponibilidad_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.excepcion_disponibilidad_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.excepcion_disponibilidad_id_seq OWNER TO clinica;

--
-- Name: excepcion_disponibilidad_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.excepcion_disponibilidad_id_seq OWNED BY public.excepcion_disponibilidad.id;


--
-- Name: indicacion_medica; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.indicacion_medica (
    id bigint NOT NULL,
    consulta_id bigint NOT NULL,
    tipo character varying(20) NOT NULL,
    descripcion text,
    CONSTRAINT indicacion_medica_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['reposo'::character varying, 'derivacion'::character varying, 'estudios'::character varying])::text[])))
);


ALTER TABLE public.indicacion_medica OWNER TO clinica;

--
-- Name: TABLE indicacion_medica; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.indicacion_medica IS 'Indicaciones medicas no farmacologicas.';


--
-- Name: indicacion_medica_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.indicacion_medica_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.indicacion_medica_id_seq OWNER TO clinica;

--
-- Name: indicacion_medica_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.indicacion_medica_id_seq OWNED BY public.indicacion_medica.id;


--
-- Name: justificacion; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.justificacion (
    id bigint NOT NULL,
    doctor_id bigint NOT NULL,
    texto text NOT NULL,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tipo character varying(20) NOT NULL,
    CONSTRAINT justificacion_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['cambio_horario'::character varying, 'movimiento_cita'::character varying])::text[])))
);


ALTER TABLE public.justificacion OWNER TO clinica;

--
-- Name: TABLE justificacion; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.justificacion IS 'Justificaciones obligatorias para cambios sensibles de agenda o citas.';


--
-- Name: justificacion_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.justificacion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.justificacion_id_seq OWNER TO clinica;

--
-- Name: justificacion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.justificacion_id_seq OWNED BY public.justificacion.id;


--
-- Name: nota_evolucion; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.nota_evolucion (
    id bigint NOT NULL,
    consulta_id bigint NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    nota text NOT NULL,
    autor_id bigint NOT NULL
);


ALTER TABLE public.nota_evolucion OWNER TO clinica;

--
-- Name: TABLE nota_evolucion; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.nota_evolucion IS 'Notas de evolucion agregadas al historial clinico.';


--
-- Name: nota_evolucion_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.nota_evolucion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.nota_evolucion_id_seq OWNER TO clinica;

--
-- Name: nota_evolucion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.nota_evolucion_id_seq OWNED BY public.nota_evolucion.id;


--
-- Name: paciente; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.paciente (
    id bigint NOT NULL,
    dni character varying(20) NOT NULL,
    nombres character varying(100) NOT NULL,
    apellidos character varying(100) NOT NULL,
    sexo character varying(20),
    fecha_nacimiento date,
    telefono character varying(20) NOT NULL,
    email character varying(150),
    password_hash character varying(255),
    activo boolean DEFAULT true NOT NULL
);


ALTER TABLE public.paciente OWNER TO clinica;

--
-- Name: TABLE paciente; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.paciente IS 'Paciente de la clinica. El email es opcional para quienes no tienen cuenta web.';


--
-- Name: paciente_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.paciente_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.paciente_id_seq OWNER TO clinica;

--
-- Name: paciente_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.paciente_id_seq OWNED BY public.paciente.id;


--
-- Name: pago; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.pago (
    id bigint NOT NULL,
    cita_id bigint NOT NULL,
    monto numeric(10,2) NOT NULL,
    metodo character varying(20) NOT NULL,
    fecha_pago timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    registrado_por_usuario_id bigint,
    caja_diaria_id bigint,
    CONSTRAINT pago_metodo_check CHECK (((metodo)::text = ANY ((ARRAY['efectivo'::character varying, 'tarjeta'::character varying, 'transferencia'::character varying, 'web'::character varying, 'yape'::character varying, 'plin'::character varying])::text[]))),
    CONSTRAINT pago_monto_check CHECK ((monto >= (0)::numeric))
);


ALTER TABLE public.pago OWNER TO clinica;

--
-- Name: TABLE pago; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.pago IS 'Pago asociado a una cita. Solo puede existir un pago por cita.';


--
-- Name: pago_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.pago_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.pago_id_seq OWNER TO clinica;

--
-- Name: pago_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.pago_id_seq OWNED BY public.pago.id;


--
-- Name: permiso; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.permiso (
    id bigint NOT NULL,
    codigo character varying(100) NOT NULL,
    descripcion character varying(255)
);


ALTER TABLE public.permiso OWNER TO clinica;

--
-- Name: TABLE permiso; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.permiso IS 'Permisos atomicos usados por RBAC.';


--
-- Name: COLUMN permiso.codigo; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON COLUMN public.permiso.codigo IS 'Codigo estable usado por backend y frontend, por ejemplo citas.crear.';


--
-- Name: permiso_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.permiso_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.permiso_id_seq OWNER TO clinica;

--
-- Name: permiso_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.permiso_id_seq OWNED BY public.permiso.id;


--
-- Name: receta; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.receta (
    id bigint NOT NULL,
    consulta_id bigint NOT NULL,
    medicamento character varying(255) NOT NULL,
    dosis character varying(100),
    frecuencia character varying(100),
    duracion character varying(100),
    indicaciones text
);


ALTER TABLE public.receta OWNER TO clinica;

--
-- Name: TABLE receta; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.receta IS 'Medicamentos indicados dentro de una consulta.';


--
-- Name: receta_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.receta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.receta_id_seq OWNER TO clinica;

--
-- Name: receta_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.receta_id_seq OWNED BY public.receta.id;


--
-- Name: rol; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.rol (
    id bigint NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion character varying(255),
    activo boolean DEFAULT true NOT NULL
);


ALTER TABLE public.rol OWNER TO clinica;

--
-- Name: TABLE rol; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.rol IS 'Roles de acceso del sistema interno.';


--
-- Name: COLUMN rol.nombre; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON COLUMN public.rol.nombre IS 'Nombre visible del rol. La unicidad se valida sin distinguir mayusculas.';


--
-- Name: rol_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.rol_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.rol_id_seq OWNER TO clinica;

--
-- Name: rol_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.rol_id_seq OWNED BY public.rol.id;


--
-- Name: rol_permiso; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.rol_permiso (
    rol_id bigint NOT NULL,
    permiso_id bigint NOT NULL
);


ALTER TABLE public.rol_permiso OWNER TO clinica;

--
-- Name: TABLE rol_permiso; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.rol_permiso IS 'Relacion muchos a muchos entre roles y permisos.';


--
-- Name: secretaria; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.secretaria (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL
);


ALTER TABLE public.secretaria OWNER TO clinica;

--
-- Name: TABLE secretaria; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.secretaria IS 'Perfil de secretaria asociado a un usuario.';


--
-- Name: secretaria_doctor; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.secretaria_doctor (
    secretaria_id bigint NOT NULL,
    doctor_id bigint NOT NULL
);


ALTER TABLE public.secretaria_doctor OWNER TO clinica;

--
-- Name: TABLE secretaria_doctor; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.secretaria_doctor IS 'Medicos asignados a una secretaria para edicion de citas.';


--
-- Name: secretaria_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.secretaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.secretaria_id_seq OWNER TO clinica;

--
-- Name: secretaria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.secretaria_id_seq OWNED BY public.secretaria.id;


--
-- Name: sede; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.sede (
    id bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    direccion character varying(255),
    activo boolean DEFAULT true NOT NULL
);


ALTER TABLE public.sede OWNER TO clinica;

--
-- Name: TABLE sede; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.sede IS 'Sedes fisicas de la clinica.';


--
-- Name: sede_especialidad; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.sede_especialidad (
    sede_id bigint NOT NULL,
    especialidad_id bigint NOT NULL
);


ALTER TABLE public.sede_especialidad OWNER TO clinica;

--
-- Name: TABLE sede_especialidad; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.sede_especialidad IS 'Especialidades disponibles por sede.';


--
-- Name: sede_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.sede_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sede_id_seq OWNER TO clinica;

--
-- Name: sede_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.sede_id_seq OWNED BY public.sede.id;


--
-- Name: usuario; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.usuario (
    id bigint NOT NULL,
    dni character varying(20) NOT NULL,
    nombres character varying(100) NOT NULL,
    apellidos character varying(100) NOT NULL,
    email character varying(150) NOT NULL,
    telefono character varying(20),
    fecha_nacimiento date,
    password_hash character varying(255) NOT NULL,
    cambio_password_obligatorio boolean DEFAULT true NOT NULL,
    activo boolean DEFAULT true NOT NULL
);


ALTER TABLE public.usuario OWNER TO clinica;

--
-- Name: TABLE usuario; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.usuario IS 'Empleado interno con acceso al sistema.';


--
-- Name: COLUMN usuario.password_hash; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON COLUMN public.usuario.password_hash IS 'Hash BCrypt de la contrasena.';


--
-- Name: usuario_id_seq; Type: SEQUENCE; Schema: public; Owner: clinica
--

CREATE SEQUENCE public.usuario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.usuario_id_seq OWNER TO clinica;

--
-- Name: usuario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinica
--

ALTER SEQUENCE public.usuario_id_seq OWNED BY public.usuario.id;


--
-- Name: usuario_rol; Type: TABLE; Schema: public; Owner: clinica
--

CREATE TABLE public.usuario_rol (
    usuario_id bigint NOT NULL,
    rol_id bigint NOT NULL
);


ALTER TABLE public.usuario_rol OWNER TO clinica;

--
-- Name: TABLE usuario_rol; Type: COMMENT; Schema: public; Owner: clinica
--

COMMENT ON TABLE public.usuario_rol IS 'Roles asignados a usuarios internos.';


--
-- Name: adjunto id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.adjunto ALTER COLUMN id SET DEFAULT nextval('public.adjunto_id_seq'::regclass);


--
-- Name: audit_log id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.audit_log ALTER COLUMN id SET DEFAULT nextval('public.audit_log_id_seq'::regclass);


--
-- Name: caja_diaria id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.caja_diaria ALTER COLUMN id SET DEFAULT nextval('public.caja_diaria_id_seq'::regclass);


--
-- Name: cita id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.cita ALTER COLUMN id SET DEFAULT nextval('public.cita_id_seq'::regclass);


--
-- Name: codigo_verificacion id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.codigo_verificacion ALTER COLUMN id SET DEFAULT nextval('public.codigo_verificacion_id_seq'::regclass);


--
-- Name: consulta id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.consulta ALTER COLUMN id SET DEFAULT nextval('public.consulta_id_seq'::regclass);


--
-- Name: consultorio id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.consultorio ALTER COLUMN id SET DEFAULT nextval('public.consultorio_id_seq'::regclass);


--
-- Name: disponibilidad_base id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.disponibilidad_base ALTER COLUMN id SET DEFAULT nextval('public.disponibilidad_base_id_seq'::regclass);


--
-- Name: doctor id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.doctor ALTER COLUMN id SET DEFAULT nextval('public.doctor_id_seq'::regclass);


--
-- Name: enfermera id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.enfermera ALTER COLUMN id SET DEFAULT nextval('public.enfermera_id_seq'::regclass);


--
-- Name: especialidad id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.especialidad ALTER COLUMN id SET DEFAULT nextval('public.especialidad_id_seq'::regclass);


--
-- Name: estudio_complementario id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.estudio_complementario ALTER COLUMN id SET DEFAULT nextval('public.estudio_complementario_id_seq'::regclass);


--
-- Name: excepcion_disponibilidad id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.excepcion_disponibilidad ALTER COLUMN id SET DEFAULT nextval('public.excepcion_disponibilidad_id_seq'::regclass);


--
-- Name: indicacion_medica id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.indicacion_medica ALTER COLUMN id SET DEFAULT nextval('public.indicacion_medica_id_seq'::regclass);


--
-- Name: justificacion id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.justificacion ALTER COLUMN id SET DEFAULT nextval('public.justificacion_id_seq'::regclass);


--
-- Name: nota_evolucion id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.nota_evolucion ALTER COLUMN id SET DEFAULT nextval('public.nota_evolucion_id_seq'::regclass);


--
-- Name: paciente id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.paciente ALTER COLUMN id SET DEFAULT nextval('public.paciente_id_seq'::regclass);


--
-- Name: pago id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.pago ALTER COLUMN id SET DEFAULT nextval('public.pago_id_seq'::regclass);


--
-- Name: permiso id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.permiso ALTER COLUMN id SET DEFAULT nextval('public.permiso_id_seq'::regclass);


--
-- Name: receta id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.receta ALTER COLUMN id SET DEFAULT nextval('public.receta_id_seq'::regclass);


--
-- Name: rol id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.rol ALTER COLUMN id SET DEFAULT nextval('public.rol_id_seq'::regclass);


--
-- Name: secretaria id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.secretaria ALTER COLUMN id SET DEFAULT nextval('public.secretaria_id_seq'::regclass);


--
-- Name: sede id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.sede ALTER COLUMN id SET DEFAULT nextval('public.sede_id_seq'::regclass);


--
-- Name: usuario id; Type: DEFAULT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.usuario ALTER COLUMN id SET DEFAULT nextval('public.usuario_id_seq'::regclass);


--
-- Data for Name: adjunto; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: audit_log; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: caja_diaria; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: cita; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: codigo_verificacion; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: configuracion_global; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: consulta; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: consultorio; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: disponibilidad_base; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: doctor; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: doctor_consultorio; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: doctor_sede; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: enfermera; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: especialidad; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: estudio_complementario; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: excepcion_disponibilidad; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: indicacion_medica; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: justificacion; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: nota_evolucion; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: paciente; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: pago; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: permiso; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: receta; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: rol; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: rol_permiso; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: secretaria; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: secretaria_doctor; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: sede; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: sede_especialidad; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: usuario_rol; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Name: adjunto_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.adjunto_id_seq', 1, true);


--
-- Name: audit_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.audit_log_id_seq', 1, false);


--
-- Name: caja_diaria_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.caja_diaria_id_seq', 1, false);


--
-- Name: cita_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.cita_id_seq', 1, true);


--
-- Name: codigo_verificacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.codigo_verificacion_id_seq', 1, true);


--
-- Name: consulta_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.consulta_id_seq', 1, true);


--
-- Name: consultorio_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.consultorio_id_seq', 1, true);


--
-- Name: disponibilidad_base_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.disponibilidad_base_id_seq', 1, true);


--
-- Name: doctor_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.doctor_id_seq', 1, true);


--
-- Name: enfermera_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.enfermera_id_seq', 1, true);


--
-- Name: especialidad_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.especialidad_id_seq', 1, true);


--
-- Name: estudio_complementario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.estudio_complementario_id_seq', 1, true);


--
-- Name: excepcion_disponibilidad_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.excepcion_disponibilidad_id_seq', 1, true);


--
-- Name: indicacion_medica_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.indicacion_medica_id_seq', 1, true);


--
-- Name: justificacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.justificacion_id_seq', 1, true);


--
-- Name: nota_evolucion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.nota_evolucion_id_seq', 1, true);


--
-- Name: paciente_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.paciente_id_seq', 1, true);


--
-- Name: pago_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.pago_id_seq', 1, true);


--
-- Name: permiso_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.permiso_id_seq', 55, true);


--
-- Name: receta_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.receta_id_seq', 1, true);


--
-- Name: rol_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.rol_id_seq', 4, true);


--
-- Name: secretaria_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.secretaria_id_seq', 1, true);


--
-- Name: sede_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.sede_id_seq', 1, true);


--
-- Name: usuario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval('public.usuario_id_seq', 4, true);


--
-- Name: adjunto adjunto_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.adjunto
    ADD CONSTRAINT adjunto_pkey PRIMARY KEY (id);


--
-- Name: audit_log audit_log_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.audit_log
    ADD CONSTRAINT audit_log_pkey PRIMARY KEY (id);


--
-- Name: caja_diaria caja_diaria_fecha_key; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.caja_diaria
    ADD CONSTRAINT caja_diaria_fecha_key UNIQUE (fecha);


--
-- Name: caja_diaria caja_diaria_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.caja_diaria
    ADD CONSTRAINT caja_diaria_pkey PRIMARY KEY (id);


--
-- Name: cita cita_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.cita
    ADD CONSTRAINT cita_pkey PRIMARY KEY (id);


--
-- Name: codigo_verificacion codigo_verificacion_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.codigo_verificacion
    ADD CONSTRAINT codigo_verificacion_pkey PRIMARY KEY (id);


--
-- Name: configuracion_global configuracion_global_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.configuracion_global
    ADD CONSTRAINT configuracion_global_pkey PRIMARY KEY (clave);


--
-- Name: consulta consulta_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.consulta
    ADD CONSTRAINT consulta_pkey PRIMARY KEY (id);


--
-- Name: consultorio consultorio_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.consultorio
    ADD CONSTRAINT consultorio_pkey PRIMARY KEY (id);


--
-- Name: consultorio consultorio_sede_id_nombre_key; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.consultorio
    ADD CONSTRAINT consultorio_sede_id_nombre_key UNIQUE (sede_id, nombre);


--
-- Name: disponibilidad_base disponibilidad_base_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.disponibilidad_base
    ADD CONSTRAINT disponibilidad_base_pkey PRIMARY KEY (id);


--
-- Name: doctor_consultorio doctor_consultorio_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.doctor_consultorio
    ADD CONSTRAINT doctor_consultorio_pkey PRIMARY KEY (doctor_id, consultorio_id);


--
-- Name: doctor doctor_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.doctor
    ADD CONSTRAINT doctor_pkey PRIMARY KEY (id);


--
-- Name: doctor_sede doctor_sede_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.doctor_sede
    ADD CONSTRAINT doctor_sede_pkey PRIMARY KEY (doctor_id, sede_id);


--
-- Name: doctor doctor_usuario_id_key; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.doctor
    ADD CONSTRAINT doctor_usuario_id_key UNIQUE (usuario_id);


--
-- Name: enfermera enfermera_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.enfermera
    ADD CONSTRAINT enfermera_pkey PRIMARY KEY (id);


--
-- Name: enfermera enfermera_usuario_id_key; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.enfermera
    ADD CONSTRAINT enfermera_usuario_id_key UNIQUE (usuario_id);


--
-- Name: especialidad especialidad_nombre_key; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.especialidad
    ADD CONSTRAINT especialidad_nombre_key UNIQUE (nombre);


--
-- Name: especialidad especialidad_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.especialidad
    ADD CONSTRAINT especialidad_pkey PRIMARY KEY (id);


--
-- Name: estudio_complementario estudio_complementario_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.estudio_complementario
    ADD CONSTRAINT estudio_complementario_pkey PRIMARY KEY (id);


--
-- Name: excepcion_disponibilidad excepcion_disponibilidad_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.excepcion_disponibilidad
    ADD CONSTRAINT excepcion_disponibilidad_pkey PRIMARY KEY (id);


--
-- Name: indicacion_medica indicacion_medica_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.indicacion_medica
    ADD CONSTRAINT indicacion_medica_pkey PRIMARY KEY (id);


--
-- Name: justificacion justificacion_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.justificacion
    ADD CONSTRAINT justificacion_pkey PRIMARY KEY (id);


--
-- Name: nota_evolucion nota_evolucion_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.nota_evolucion
    ADD CONSTRAINT nota_evolucion_pkey PRIMARY KEY (id);


--
-- Name: paciente paciente_dni_key; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.paciente
    ADD CONSTRAINT paciente_dni_key UNIQUE (dni);


--
-- Name: paciente paciente_email_key; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.paciente
    ADD CONSTRAINT paciente_email_key UNIQUE (email);


--
-- Name: paciente paciente_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.paciente
    ADD CONSTRAINT paciente_pkey PRIMARY KEY (id);


--
-- Name: pago pago_cita_id_key; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.pago
    ADD CONSTRAINT pago_cita_id_key UNIQUE (cita_id);


--
-- Name: pago pago_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.pago
    ADD CONSTRAINT pago_pkey PRIMARY KEY (id);


--
-- Name: permiso permiso_codigo_key; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.permiso
    ADD CONSTRAINT permiso_codigo_key UNIQUE (codigo);


--
-- Name: permiso permiso_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.permiso
    ADD CONSTRAINT permiso_pkey PRIMARY KEY (id);


--
-- Name: receta receta_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.receta
    ADD CONSTRAINT receta_pkey PRIMARY KEY (id);


--
-- Name: rol_permiso rol_permiso_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.rol_permiso
    ADD CONSTRAINT rol_permiso_pkey PRIMARY KEY (rol_id, permiso_id);


--
-- Name: rol rol_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_pkey PRIMARY KEY (id);


--
-- Name: secretaria_doctor secretaria_doctor_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.secretaria_doctor
    ADD CONSTRAINT secretaria_doctor_pkey PRIMARY KEY (secretaria_id, doctor_id);


--
-- Name: secretaria secretaria_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.secretaria
    ADD CONSTRAINT secretaria_pkey PRIMARY KEY (id);


--
-- Name: secretaria secretaria_usuario_id_key; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.secretaria
    ADD CONSTRAINT secretaria_usuario_id_key UNIQUE (usuario_id);


--
-- Name: sede_especialidad sede_especialidad_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.sede_especialidad
    ADD CONSTRAINT sede_especialidad_pkey PRIMARY KEY (sede_id, especialidad_id);


--
-- Name: sede sede_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.sede
    ADD CONSTRAINT sede_pkey PRIMARY KEY (id);


--
-- Name: disponibilidad_base uk_disponibilidad_base_doctor_sede_dia; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.disponibilidad_base
    ADD CONSTRAINT uk_disponibilidad_base_doctor_sede_dia UNIQUE (doctor_id, sede_id, dia_semana);


--
-- Name: excepcion_disponibilidad uk_excepcion_disponibilidad_doctor_fecha_inicio; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.excepcion_disponibilidad
    ADD CONSTRAINT uk_excepcion_disponibilidad_doctor_fecha_inicio UNIQUE (doctor_id, fecha, hora_inicio);


--
-- Name: usuario usuario_dni_key; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_dni_key UNIQUE (dni);


--
-- Name: usuario usuario_email_key; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_email_key UNIQUE (email);


--
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);


--
-- Name: usuario_rol usuario_rol_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.usuario_rol
    ADD CONSTRAINT usuario_rol_pkey PRIMARY KEY (usuario_id, rol_id);


--
-- Name: idx_adjunto_consulta_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_adjunto_consulta_id ON public.adjunto USING btree (consulta_id);


--
-- Name: idx_audit_log_fecha; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_audit_log_fecha ON public.audit_log USING btree (fecha);


--
-- Name: idx_audit_log_usuario_dni; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_audit_log_usuario_dni ON public.audit_log USING btree (usuario_dni);


--
-- Name: idx_cita_creado_por_usuario_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_cita_creado_por_usuario_id ON public.cita USING btree (creado_por_usuario_id);


--
-- Name: idx_cita_doctor_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_cita_doctor_id ON public.cita USING btree (doctor_id);


--
-- Name: idx_cita_estado; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_cita_estado ON public.cita USING btree (estado);


--
-- Name: idx_cita_estado_pago; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_cita_estado_pago ON public.cita USING btree (estado_pago);


--
-- Name: idx_cita_fecha_hora_inicio; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_cita_fecha_hora_inicio ON public.cita USING btree (fecha_hora_inicio);


--
-- Name: idx_cita_justificacion_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_cita_justificacion_id ON public.cita USING btree (justificacion_id);


--
-- Name: idx_cita_paciente_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_cita_paciente_id ON public.cita USING btree (paciente_id);


--
-- Name: idx_cita_sede_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_cita_sede_id ON public.cita USING btree (sede_id);


--
-- Name: idx_codigo_verificacion_codigo; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_codigo_verificacion_codigo ON public.codigo_verificacion USING btree (codigo);


--
-- Name: idx_codigo_verificacion_email; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_codigo_verificacion_email ON public.codigo_verificacion USING btree (email);


--
-- Name: idx_codigo_verificacion_expiracion; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_codigo_verificacion_expiracion ON public.codigo_verificacion USING btree (fecha_expiracion);


--
-- Name: idx_consulta_cita_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_consulta_cita_id ON public.consulta USING btree (cita_id);


--
-- Name: idx_consulta_doctor_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_consulta_doctor_id ON public.consulta USING btree (doctor_id);


--
-- Name: idx_consulta_fecha_hora; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_consulta_fecha_hora ON public.consulta USING btree (fecha_hora);


--
-- Name: idx_consulta_paciente_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_consulta_paciente_id ON public.consulta USING btree (paciente_id);


--
-- Name: idx_consulta_sede_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_consulta_sede_id ON public.consulta USING btree (sede_id);


--
-- Name: idx_disponibilidad_base_doctor_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_disponibilidad_base_doctor_id ON public.disponibilidad_base USING btree (doctor_id);


--
-- Name: idx_disponibilidad_base_sede_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_disponibilidad_base_sede_id ON public.disponibilidad_base USING btree (sede_id);


--
-- Name: idx_doctor_especialidad_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_doctor_especialidad_id ON public.doctor USING btree (especialidad_id);


--
-- Name: idx_doctor_sede_sede_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_doctor_sede_sede_id ON public.doctor_sede USING btree (sede_id);


--
-- Name: idx_doctor_subespecialidad_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_doctor_subespecialidad_id ON public.doctor USING btree (subespecialidad_id);


--
-- Name: idx_doctor_usuario_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_doctor_usuario_id ON public.doctor USING btree (usuario_id);


--
-- Name: idx_enfermera_usuario_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_enfermera_usuario_id ON public.enfermera USING btree (usuario_id);


--
-- Name: idx_especialidad_padre_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_especialidad_padre_id ON public.especialidad USING btree (especialidad_padre_id);


--
-- Name: idx_estudio_complementario_consulta_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_estudio_complementario_consulta_id ON public.estudio_complementario USING btree (consulta_id);


--
-- Name: idx_excepcion_disponibilidad_doctor_fecha; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_excepcion_disponibilidad_doctor_fecha ON public.excepcion_disponibilidad USING btree (doctor_id, fecha);


--
-- Name: idx_excepcion_disponibilidad_justificacion_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_excepcion_disponibilidad_justificacion_id ON public.excepcion_disponibilidad USING btree (justificacion_id);


--
-- Name: idx_indicacion_medica_consulta_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_indicacion_medica_consulta_id ON public.indicacion_medica USING btree (consulta_id);


--
-- Name: idx_justificacion_doctor_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_justificacion_doctor_id ON public.justificacion USING btree (doctor_id);


--
-- Name: idx_justificacion_tipo; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_justificacion_tipo ON public.justificacion USING btree (tipo);


--
-- Name: idx_nota_evolucion_autor_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_nota_evolucion_autor_id ON public.nota_evolucion USING btree (autor_id);


--
-- Name: idx_nota_evolucion_consulta_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_nota_evolucion_consulta_id ON public.nota_evolucion USING btree (consulta_id);


--
-- Name: idx_paciente_activo; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_paciente_activo ON public.paciente USING btree (activo);


--
-- Name: idx_paciente_dni; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_paciente_dni ON public.paciente USING btree (dni);


--
-- Name: idx_paciente_email; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_paciente_email ON public.paciente USING btree (email);


--
-- Name: idx_pago_fecha_pago; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_pago_fecha_pago ON public.pago USING btree (fecha_pago);


--
-- Name: idx_pago_registrado_por_usuario_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_pago_registrado_por_usuario_id ON public.pago USING btree (registrado_por_usuario_id);


--
-- Name: idx_receta_consulta_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_receta_consulta_id ON public.receta USING btree (consulta_id);


--
-- Name: idx_rol_permiso_permiso_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_rol_permiso_permiso_id ON public.rol_permiso USING btree (permiso_id);


--
-- Name: idx_secretaria_doctor_doctor_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_secretaria_doctor_doctor_id ON public.secretaria_doctor USING btree (doctor_id);


--
-- Name: idx_secretaria_usuario_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_secretaria_usuario_id ON public.secretaria USING btree (usuario_id);


--
-- Name: idx_sede_activo; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_sede_activo ON public.sede USING btree (activo);


--
-- Name: idx_sede_especialidad_especialidad_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_sede_especialidad_especialidad_id ON public.sede_especialidad USING btree (especialidad_id);


--
-- Name: idx_usuario_activo; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_usuario_activo ON public.usuario USING btree (activo);


--
-- Name: idx_usuario_dni; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_usuario_dni ON public.usuario USING btree (dni);


--
-- Name: idx_usuario_email; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_usuario_email ON public.usuario USING btree (email);


--
-- Name: idx_usuario_rol_rol_id; Type: INDEX; Schema: public; Owner: clinica
--

CREATE INDEX idx_usuario_rol_rol_id ON public.usuario_rol USING btree (rol_id);


--
-- Name: uk_rol_nombre_lower; Type: INDEX; Schema: public; Owner: clinica
--

CREATE UNIQUE INDEX uk_rol_nombre_lower ON public.rol USING btree (lower((nombre)::text));


--
-- Name: adjunto adjunto_consulta_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.adjunto
    ADD CONSTRAINT adjunto_consulta_id_fkey FOREIGN KEY (consulta_id) REFERENCES public.consulta(id) ON DELETE CASCADE;


--
-- Name: caja_diaria caja_diaria_abierto_por_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.caja_diaria
    ADD CONSTRAINT caja_diaria_abierto_por_usuario_id_fkey FOREIGN KEY (abierto_por_usuario_id) REFERENCES public.usuario(id) ON DELETE SET NULL;


--
-- Name: caja_diaria caja_diaria_cerrado_por_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.caja_diaria
    ADD CONSTRAINT caja_diaria_cerrado_por_usuario_id_fkey FOREIGN KEY (cerrado_por_usuario_id) REFERENCES public.usuario(id) ON DELETE SET NULL;


--
-- Name: cita cita_consultorio_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.cita
    ADD CONSTRAINT cita_consultorio_id_fkey FOREIGN KEY (consultorio_id) REFERENCES public.consultorio(id) ON DELETE RESTRICT;


--
-- Name: cita cita_creado_por_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.cita
    ADD CONSTRAINT cita_creado_por_usuario_id_fkey FOREIGN KEY (creado_por_usuario_id) REFERENCES public.usuario(id) ON DELETE SET NULL;


--
-- Name: cita cita_doctor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.cita
    ADD CONSTRAINT cita_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor(id) ON DELETE RESTRICT;


--
-- Name: cita cita_justificacion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.cita
    ADD CONSTRAINT cita_justificacion_id_fkey FOREIGN KEY (justificacion_id) REFERENCES public.justificacion(id) ON DELETE SET NULL;


--
-- Name: cita cita_paciente_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.cita
    ADD CONSTRAINT cita_paciente_id_fkey FOREIGN KEY (paciente_id) REFERENCES public.paciente(id) ON DELETE RESTRICT;


--
-- Name: cita cita_sede_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.cita
    ADD CONSTRAINT cita_sede_id_fkey FOREIGN KEY (sede_id) REFERENCES public.sede(id) ON DELETE RESTRICT;


--
-- Name: consulta consulta_cita_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.consulta
    ADD CONSTRAINT consulta_cita_id_fkey FOREIGN KEY (cita_id) REFERENCES public.cita(id) ON DELETE SET NULL;


--
-- Name: consulta consulta_doctor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.consulta
    ADD CONSTRAINT consulta_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor(id) ON DELETE RESTRICT;


--
-- Name: consulta consulta_paciente_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.consulta
    ADD CONSTRAINT consulta_paciente_id_fkey FOREIGN KEY (paciente_id) REFERENCES public.paciente(id) ON DELETE RESTRICT;


--
-- Name: consulta consulta_sede_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.consulta
    ADD CONSTRAINT consulta_sede_id_fkey FOREIGN KEY (sede_id) REFERENCES public.sede(id) ON DELETE RESTRICT;


--
-- Name: consultorio consultorio_sede_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.consultorio
    ADD CONSTRAINT consultorio_sede_id_fkey FOREIGN KEY (sede_id) REFERENCES public.sede(id) ON DELETE RESTRICT;


--
-- Name: disponibilidad_base disponibilidad_base_consultorio_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.disponibilidad_base
    ADD CONSTRAINT disponibilidad_base_consultorio_id_fkey FOREIGN KEY (consultorio_id) REFERENCES public.consultorio(id) ON DELETE SET NULL;


--
-- Name: disponibilidad_base disponibilidad_base_doctor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.disponibilidad_base
    ADD CONSTRAINT disponibilidad_base_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor(id) ON DELETE CASCADE;


--
-- Name: disponibilidad_base disponibilidad_base_sede_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.disponibilidad_base
    ADD CONSTRAINT disponibilidad_base_sede_id_fkey FOREIGN KEY (sede_id) REFERENCES public.sede(id) ON DELETE CASCADE;


--
-- Name: doctor_consultorio doctor_consultorio_consultorio_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.doctor_consultorio
    ADD CONSTRAINT doctor_consultorio_consultorio_id_fkey FOREIGN KEY (consultorio_id) REFERENCES public.consultorio(id) ON DELETE CASCADE;


--
-- Name: doctor_consultorio doctor_consultorio_doctor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.doctor_consultorio
    ADD CONSTRAINT doctor_consultorio_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor(id) ON DELETE CASCADE;


--
-- Name: doctor doctor_especialidad_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.doctor
    ADD CONSTRAINT doctor_especialidad_id_fkey FOREIGN KEY (especialidad_id) REFERENCES public.especialidad(id) ON DELETE RESTRICT;


--
-- Name: doctor_sede doctor_sede_doctor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.doctor_sede
    ADD CONSTRAINT doctor_sede_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor(id) ON DELETE CASCADE;


--
-- Name: doctor_sede doctor_sede_sede_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.doctor_sede
    ADD CONSTRAINT doctor_sede_sede_id_fkey FOREIGN KEY (sede_id) REFERENCES public.sede(id) ON DELETE CASCADE;


--
-- Name: doctor doctor_subespecialidad_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.doctor
    ADD CONSTRAINT doctor_subespecialidad_id_fkey FOREIGN KEY (subespecialidad_id) REFERENCES public.especialidad(id) ON DELETE RESTRICT;


--
-- Name: doctor doctor_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.doctor
    ADD CONSTRAINT doctor_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(id) ON DELETE RESTRICT;


--
-- Name: enfermera enfermera_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.enfermera
    ADD CONSTRAINT enfermera_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(id) ON DELETE RESTRICT;


--
-- Name: especialidad especialidad_especialidad_padre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.especialidad
    ADD CONSTRAINT especialidad_especialidad_padre_id_fkey FOREIGN KEY (especialidad_padre_id) REFERENCES public.especialidad(id) ON DELETE RESTRICT;


--
-- Name: estudio_complementario estudio_complementario_consulta_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.estudio_complementario
    ADD CONSTRAINT estudio_complementario_consulta_id_fkey FOREIGN KEY (consulta_id) REFERENCES public.consulta(id) ON DELETE CASCADE;


--
-- Name: excepcion_disponibilidad excepcion_disponibilidad_doctor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.excepcion_disponibilidad
    ADD CONSTRAINT excepcion_disponibilidad_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor(id) ON DELETE CASCADE;


--
-- Name: excepcion_disponibilidad excepcion_disponibilidad_justificacion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.excepcion_disponibilidad
    ADD CONSTRAINT excepcion_disponibilidad_justificacion_id_fkey FOREIGN KEY (justificacion_id) REFERENCES public.justificacion(id) ON DELETE SET NULL;


--
-- Name: indicacion_medica indicacion_medica_consulta_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.indicacion_medica
    ADD CONSTRAINT indicacion_medica_consulta_id_fkey FOREIGN KEY (consulta_id) REFERENCES public.consulta(id) ON DELETE CASCADE;


--
-- Name: justificacion justificacion_doctor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.justificacion
    ADD CONSTRAINT justificacion_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor(id) ON DELETE RESTRICT;


--
-- Name: nota_evolucion nota_evolucion_autor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.nota_evolucion
    ADD CONSTRAINT nota_evolucion_autor_id_fkey FOREIGN KEY (autor_id) REFERENCES public.usuario(id) ON DELETE RESTRICT;


--
-- Name: nota_evolucion nota_evolucion_consulta_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.nota_evolucion
    ADD CONSTRAINT nota_evolucion_consulta_id_fkey FOREIGN KEY (consulta_id) REFERENCES public.consulta(id) ON DELETE CASCADE;


--
-- Name: pago pago_caja_diaria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.pago
    ADD CONSTRAINT pago_caja_diaria_id_fkey FOREIGN KEY (caja_diaria_id) REFERENCES public.caja_diaria(id) ON DELETE SET NULL;


--
-- Name: pago pago_cita_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.pago
    ADD CONSTRAINT pago_cita_id_fkey FOREIGN KEY (cita_id) REFERENCES public.cita(id) ON DELETE RESTRICT;


--
-- Name: pago pago_registrado_por_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.pago
    ADD CONSTRAINT pago_registrado_por_usuario_id_fkey FOREIGN KEY (registrado_por_usuario_id) REFERENCES public.usuario(id) ON DELETE SET NULL;


--
-- Name: receta receta_consulta_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.receta
    ADD CONSTRAINT receta_consulta_id_fkey FOREIGN KEY (consulta_id) REFERENCES public.consulta(id) ON DELETE CASCADE;


--
-- Name: rol_permiso rol_permiso_permiso_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.rol_permiso
    ADD CONSTRAINT rol_permiso_permiso_id_fkey FOREIGN KEY (permiso_id) REFERENCES public.permiso(id) ON DELETE CASCADE;


--
-- Name: rol_permiso rol_permiso_rol_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.rol_permiso
    ADD CONSTRAINT rol_permiso_rol_id_fkey FOREIGN KEY (rol_id) REFERENCES public.rol(id) ON DELETE CASCADE;


--
-- Name: secretaria_doctor secretaria_doctor_doctor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.secretaria_doctor
    ADD CONSTRAINT secretaria_doctor_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor(id) ON DELETE CASCADE;


--
-- Name: secretaria_doctor secretaria_doctor_secretaria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.secretaria_doctor
    ADD CONSTRAINT secretaria_doctor_secretaria_id_fkey FOREIGN KEY (secretaria_id) REFERENCES public.secretaria(id) ON DELETE CASCADE;


--
-- Name: secretaria secretaria_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.secretaria
    ADD CONSTRAINT secretaria_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(id) ON DELETE RESTRICT;


--
-- Name: sede_especialidad sede_especialidad_especialidad_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.sede_especialidad
    ADD CONSTRAINT sede_especialidad_especialidad_id_fkey FOREIGN KEY (especialidad_id) REFERENCES public.especialidad(id) ON DELETE CASCADE;


--
-- Name: sede_especialidad sede_especialidad_sede_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.sede_especialidad
    ADD CONSTRAINT sede_especialidad_sede_id_fkey FOREIGN KEY (sede_id) REFERENCES public.sede(id) ON DELETE CASCADE;


--
-- Name: usuario_rol usuario_rol_rol_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.usuario_rol
    ADD CONSTRAINT usuario_rol_rol_id_fkey FOREIGN KEY (rol_id) REFERENCES public.rol(id) ON DELETE CASCADE;


--
-- Name: usuario_rol usuario_rol_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY public.usuario_rol
    ADD CONSTRAINT usuario_rol_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--


