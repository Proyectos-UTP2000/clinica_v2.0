package com.web.clinica.security;

import com.web.clinica.model.Permiso;
import com.web.clinica.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PermissionCatalogInitializer implements ApplicationRunner {

    private final PermisoRepository permisoRepository;

    /** Asegura que la base tenga todos los permisos conocidos por el sistema. */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        PermissionCatalog.all().forEach(definition -> {
            Permiso permiso = permisoRepository.findByCodigo(definition.codigo())
                    .orElseGet(Permiso::new);
            permiso.setCodigo(definition.codigo());
            permiso.setDescripcion(definition.descripcion());
            permisoRepository.save(permiso);
        });
    }
}
