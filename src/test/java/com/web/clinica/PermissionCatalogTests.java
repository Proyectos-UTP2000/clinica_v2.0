package com.web.clinica;

import static org.assertj.core.api.Assertions.assertThat;

import com.web.clinica.security.PermissionCatalog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class PermissionCatalogTests {

    @Test
    void catalogoContienePermisosFuturosDeConsultorios() {
        Set<String> codigos = PermissionCatalog.all().stream()
                .map(PermissionCatalog.PermissionDefinition::codigo)
                .collect(Collectors.toSet());

        assertThat(codigos).contains(
                "consultorios.ver",
                "consultorios.crear",
                "consultorios.editar",
                "consultorios.eliminar"
        );
    }

    @Test
    void catalogoContieneTodosLosPermisosUsadosPorControllers() throws IOException {
        Set<String> usados = Files.walk(Path.of("src/main/java/com/web/clinica/controller"))
                .filter(path -> path.toString().endsWith(".java"))
                .flatMap(path -> extraerPermisos(path).stream())
                .collect(Collectors.toSet());
        Set<String> catalogo = PermissionCatalog.all().stream()
                .map(PermissionCatalog.PermissionDefinition::codigo)
                .collect(Collectors.toSet());

        assertThat(catalogo).containsAll(usados);
    }

    private Set<String> extraerPermisos(Path path) {
        try {
            String source = Files.readString(path);
            return Pattern.compile("\\\"([a-zA-Z0-9_.]+)\\\"")
                    .matcher(source)
                    .results()
                    .map(match -> match.group(1))
                    .filter(value -> value.contains("."))
                    .collect(Collectors.toSet());
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
