package com.web.clinica.security;

import java.util.Comparator;
import java.util.List;

public final class PermissionCatalog {

    private PermissionCatalog() {
    }

    public record PermissionDefinition(String codigo, String descripcion) {
    }

    public static List<PermissionDefinition> all() {
        return List.of(
                permission("roles.ver", "Ver lista de roles"),
                permission("roles.crear", "Crear nuevo rol"),
                permission("roles.editar", "Editar rol existente"),
                permission("roles.eliminar", "Eliminar rol"),
                permission("usuarios.ver", "Ver lista de empleados"),
                permission("usuarios.crear", "Crear empleado"),
                permission("usuarios.editar", "Editar empleado"),
                permission("usuarios.desactivar", "Activar/desactivar empleado"),
                permission("pacientes.ver", "Ver pacientes"),
                permission("pacientes.crear", "Crear pacientes"),
                permission("pacientes.editar", "Editar pacientes"),
                permission("pacientes.eliminar", "Eliminar pacientes"),
                permission("medicos.ver", "Ver medicos"),
                permission("medicos.crear", "Crear medicos"),
                permission("medicos.editar", "Editar medicos"),
                permission("medicos.eliminar", "Eliminar medicos"),
                permission("sedes.ver", "Ver sedes"),
                permission("sedes.crear", "Crear sede"),
                permission("sedes.editar", "Editar sede"),
                permission("sedes.eliminar", "Eliminar sede"),
                permission("especialidades.ver", "Ver especialidades"),
                permission("especialidades.crear", "Crear especialidad"),
                permission("especialidades.editar", "Editar especialidad"),
                permission("especialidades.eliminar", "Eliminar especialidad"),
                permission("consultorios.ver", "Ver consultorios"),
                permission("consultorios.crear", "Crear consultorio"),
                permission("consultorios.editar", "Editar consultorio"),
                permission("consultorios.eliminar", "Eliminar consultorio"),
                permission("citas.ver_todas", "Ver todas las citas"),
                permission("citas.ver_propias", "Ver solo citas propias"),
                permission("citas.ver_asignados", "Ver citas de medicos asignados"),
                permission("citas.crear", "Agendar nueva cita"),
                permission("citas.editar_propias", "Editar citas propias"),
                permission("citas.editar_asignados", "Editar/reprogramar citas de medicos asignados"),
                permission("citas.cancelar", "Cancelar citas"),
                permission("pagos.ver", "Ver pagos"),
                permission("pagos.crear", "Registrar pago manual"),
                permission("historial.ver_todos", "Ver cualquier historial clinico"),
                permission("historial.ver_propios", "Ver historial de pacientes atendidos"),
                permission("historial.ver_basico", "Ver historial en modo consulta"),
                permission("historial.crear", "Crear consulta"),
                permission("historial.editar", "Editar consulta propia"),
                permission("disponibilidad.ver_todas", "Ver disponibilidad de todos los medicos"),
                permission("disponibilidad.ver_propia", "Ver disponibilidad propia"),
                permission("disponibilidad.editar_todas", "Editar disponibilidad de todos los medicos"),
                permission("disponibilidad.editar_propia", "Editar disponibilidad propia"),
                permission("justificaciones.ver_todas", "Ver todas las justificaciones"),
                permission("justificaciones.ver_propias", "Ver solo justificaciones propias"),
                permission("reportes.ver", "Acceder a reportes y analiticas"),
                permission("dashboard.ver", "Ver dashboard general"),
                permission("caja.gestionar", "Abrir y cerrar caja diaria"),
                permission("caja.ver", "Ver reportes y balances de caja"),
                permission("audit.ver", "Ver historial de auditoria"),
                permission("config.ver", "Ver configuración global"),
                permission("config.editar", "Editar configuración global")
        ).stream().sorted(Comparator.comparing(PermissionDefinition::codigo)).toList();
    }

    private static PermissionDefinition permission(String codigo, String descripcion) {
        return new PermissionDefinition(codigo, descripcion);
    }
}
