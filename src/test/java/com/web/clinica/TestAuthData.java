package com.web.clinica;

import com.web.clinica.model.Permiso;
import com.web.clinica.model.Rol;
import com.web.clinica.model.Usuario;

final class TestAuthData {

    private TestAuthData() {}

    static Usuario usuarioConRolPermisos(String dni, String rolNombre, String... permisosCodigos) {
        Usuario usuario = new Usuario();
        usuario.setDni(dni);
        usuario.setNombres("Ada");
        usuario.setApellidos("Lovelace");
        usuario.setActivo(true);
        usuario.setCambioPasswordObligatorio(false);
        Rol rol = new Rol();
        rol.setNombre(rolNombre);
        for (String codigo : permisosCodigos) {
            Permiso permiso = new Permiso();
            permiso.setCodigo(codigo);
            permiso.setDescripcion(codigo);
            rol.getPermisos().add(permiso);
        }
        usuario.getRoles().add(rol);
        return usuario;
    }
}
