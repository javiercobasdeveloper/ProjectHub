package com.projecthub.project;

// El package mantiene esta clase dentro del mismo paquete de dominio que Project.
// Así se relacionan claramente entre sí y se pueden invocar sin necesidad de rutas extra.

// import org.junit.jupiter.api.Test;
// Importa la anotación @Test de JUnit 5, que marca un método como prueba automática.
import org.junit.jupiter.api.Test;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// Importa un método estático de JUnit para comprobar si dos valores son iguales.
import static org.junit.jupiter.api.Assertions.assertEquals;

// import static org.junit.jupiter.api.Assertions.assertThrows;
// Importa un método estático para comprobar que una acción lanza una excepción esperada.
import static org.junit.jupiter.api.Assertions.assertThrows;

// Agrupa las pruebas que verifican las reglas de nombre de Project.
class ProjectTest {
    // Comprueba que se puede crear un proyecto con un nombre válido.
    @Test
    void acceptsANameWithinTheAllowedLength() {
        // Define un nombre de ejemplo dentro del límite permitido.
        String name = "ProjectHub";

        // Crea el objeto; el constructor debe aceptar este nombre.
        Project project = new Project(name);

        // Comprueba que el objeto conserva el nombre que recibió.
        assertEquals(name, project.getName());
    }

    // Comprueba que el límite superior de 100 caracteres también se acepta.
    @Test
    void acceptsANameAtTheMaximumAllowedLength() {
        // Genera un nombre de exactamente 100 caracteres.
        String name = "a".repeat(100);

        // Crea el objeto con el valor que está justo en el límite.
        Project project = new Project(name);

        // Comprueba que el valor del límite se conserva correctamente.
        assertEquals(name, project.getName());
    }

    // Comprueba que el constructor rechaza nombres ausentes o sin contenido.
    @Test
    void rejectsNullOrBlankNames() {
        // Verifica que null provoca la excepción definida por la regla de dominio.
        assertThrows(IllegalArgumentException.class, () -> new Project(null));
        // Verifica que una cadena con solo espacios también se considera vacía.
        assertThrows(IllegalArgumentException.class, () -> new Project("   "));
    }

    // Comprueba que un nombre que excede el límite no permite crear el proyecto.
    @Test
    void rejectsNamesLongerThanOneHundredCharacters() {
        // Genera un valor con un carácter más que el máximo permitido.
        String name = "a".repeat(101);

        // Verifica que el constructor comunica el incumplimiento mediante una excepción.
        assertThrows(IllegalArgumentException.class, () -> new Project(name));
    }

    // Comprueba que un proyecto nuevo empieza en estado activo.
    @Test
    void startsAsActiveByDefault() {
        // Crea un proyecto con un nombre válido.
        Project project = new Project("ProjectHub");

        // Verifica que el proyecto aún no está archivado al nacer.
        assertEquals(ProjectStatus.ACTIVE, project.getStatus());
    }

    // Comprueba que archivar un proyecto cambia su estado a archivado.
    @Test
    void canArchiveAnActiveProject() {
        // Crea un proyecto activo.
        Project project = new Project("ProjectHub");

        // Archivamos el proyecto siguiendo la regla del dominio.
        project.archive();

        // Verifica que el estado ha cambiado a ARCHIVED.
        assertEquals(ProjectStatus.ARCHIVED, project.getStatus());
    }
}