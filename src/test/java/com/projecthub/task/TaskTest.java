package com.projecthub.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Agrupa las pruebas del flujo de estado de una tarea.
class TaskTest {
    // Comprueba que una tarea recién creada comienza en TODO.
    @Test
    void startsAsTodoByDefault() {
        // Creamos una tarea con título válido.
        Task task = new Task("Diseñar dashboard");

        // La tarea nueva debe estar pendiente.
        assertEquals(TaskStatus.TODO, task.getStatus());
    }

    // Comprueba que una tarea recibe MEDIUM cuando no se especifica prioridad.
    @Test
    void startsWithMediumPriorityByDefault() {
        // Crea una tarea sin indicar una prioridad alternativa.
        Task task = new Task("Diseñar dashboard");
        // Verifica el valor inicial acordado para el MVP.
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
    }

    // Comprueba que el título acepta el máximo de 150 caracteres definido por los requisitos.
    @Test
    void acceptsTitleAtTheMaximumAllowedLength() {
        // Genera un título que ocupa exactamente el máximo permitido.
        String title = "a".repeat(150);

        // Crea la tarea justo en el límite válido.
        Task task = new Task(title);

        // Verifica que el título se conserva sin cambios.
        assertEquals(title, task.getTitle());
    }

    // Comprueba que la descripción opcional admite el límite de 2000 caracteres.
    @Test
    void acceptsDescriptionAtTheMaximumAllowedLength() {
        // Genera una descripción que ocupa exactamente el máximo permitido.
        String description = "a".repeat(2000);
        // Construye la tarea con la descripción en su longitud límite.
        Task task = new Task("Título válido", description);
        // Verifica que el valor se conserva sin cambios.
        assertEquals(description, task.getDescription());
    }

    // Comprueba que el constructor rechaza descripciones mayores de 2000 caracteres.
    @Test
    void rejectsDescriptionLongerThanMaximumAllowedLength() {
        // Genera una descripción que supera el máximo por un carácter.
        String description = "a".repeat(2001);
        // Verifica que el dominio impide crear la tarea con ese valor inválido.
        assertThrows(IllegalArgumentException.class, () -> new Task("Título válido", description));
    }

    // Comprueba que el constructor rechaza títulos ausentes, vacíos o demasiado largos.
    @Test
    void rejectsInvalidTitles() {
        // Verifica que un título ausente provoca una excepción de validación.
        assertThrows(IllegalArgumentException.class, () -> new Task(null));
        // Verifica que un título formado por espacios no se considera contenido válido.
        assertThrows(IllegalArgumentException.class, () -> new Task("   "));
        // Verifica que se rechazan títulos que superan los 150 caracteres.
        assertThrows(IllegalArgumentException.class, () -> new Task("a".repeat(151)));
    }

    // Comprueba que una tarea puede pasar a IN_PROGRESS desde TODO.
    @Test
    void canMoveFromTodoToInProgress() {
        // Creamos una tarea nueva.
        Task task = new Task("Diseñar dashboard");

        // La tarea cambia a trabajo en curso.
        task.start();

        // Debe quedar marcada como en progreso.
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    // Comprueba que una tarea puede completarse desde IN_PROGRESS.
    @Test
    void canMoveFromInProgressToDone() {
        // Creamos una tarea y la ponemos en marcha.
        Task task = new Task("Diseñar dashboard");
        task.start();

        // La tarea se marca como finalizada.
        task.complete();

        // Debe quedar marcada como completada.
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    // Comprueba que la transición directa de TODO a DONE está prohibida.
    @Test
    void cannotMoveFromTodoToDoneDirectly() {
        // Creamos una tarea nueva.
        Task task = new Task("Diseñar dashboard");

        // Verifica que el paso directo queda bloqueado por la regla de negocio.
        assertThrows(IllegalStateException.class, task::complete);
    }

    // Comprueba que no se puede volver a DONE desde IN_PROGRESS ni tampoco reabrir una tarea terminada.
    @Test
    void cannotMoveFromDoneBackToInProgress() {
        // Creamos una tarea, la ponemos en marcha y la terminamos.
        Task task = new Task("Diseñar dashboard");
        task.start();
        task.complete();

        // Verifica que no se permite reabrir una tarea ya completada.
        assertThrows(IllegalStateException.class, task::start);
    }

    // Comprueba que una tarea en curso puede volver a TODO y no una ya terminada.
    @Test
    void canReturnAnInProgressTaskToTodoButNotACompletedTask() {
        // Crea una tarea y la pone en curso antes de devolverla a pendiente.
        Task task = new Task("Diseñar dashboard");
        task.start();
        task.returnToTodo();

        // Verifica que la transición permitida deja la tarea en TODO.
        assertEquals(TaskStatus.TODO, task.getStatus());

        // Completa la tarea para comprobar que una tarea terminada no puede volver a TODO.
        task.start();
        task.complete();
        assertThrows(IllegalStateException.class, task::returnToTodo);
    }
}
