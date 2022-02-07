package de.tum.in.www1.artemis.web.rest.hestia;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.tum.in.www1.artemis.domain.hestia.ProgrammingExerciseTask;
import de.tum.in.www1.artemis.repository.hestia.ProgrammingExerciseTaskRepository;

/**
 * REST controller for managing {@link de.tum.in.www1.artemis.domain.hestia.ProgrammingExerciseTask}.
 */
@RestController
@RequestMapping("/api")
public class ProgrammingExerciseTaskResource {

    private final Logger log = LoggerFactory.getLogger(ProgrammingExerciseTaskResource.class);

    private final ProgrammingExerciseTaskRepository programmingExerciseTaskRepository;

    public ProgrammingExerciseTaskResource(ProgrammingExerciseTaskRepository programmingExerciseTaskRepository) {
        this.programmingExerciseTaskRepository = programmingExerciseTaskRepository;
    }

    /**
     * Get all tasks with test cases and solution entries for a programming exercise
     *
     * @param exerciseId of the exercise
     * @return All tasks with test cases and solution entries
     */
    @GetMapping("/programming-exercises/{exerciseId}/tasks")
    @PreAuthorize("hasRole('TA')")
    public ResponseEntity<Set<ProgrammingExerciseTask>> getTasks(@PathVariable Long exerciseId) {
        log.debug("REST request to retrieve ProgrammingExerciseTasks for ProgrammingExercise with id : {}", exerciseId);
        Set<ProgrammingExerciseTask> tasks = programmingExerciseTaskRepository.findByExerciseIdWithTestCaseAndSolutionEntriesElseThrow(exerciseId);
        return ResponseEntity.ok(tasks);
    }
}
