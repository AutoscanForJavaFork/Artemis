package de.tum.in.www1.artemis.web.rest;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.tum.in.www1.artemis.domain.ProgrammingExercise;
import de.tum.in.www1.artemis.domain.hestia.ProgrammingExerciseSolutionEntry;
import de.tum.in.www1.artemis.domain.hestia.ProgrammingExerciseTask;
import de.tum.in.www1.artemis.repository.ProgrammingExerciseRepository;
import de.tum.in.www1.artemis.repository.ProgrammingExerciseSolutionEntryRepository;
import de.tum.in.www1.artemis.repository.ProgrammingExerciseTaskRepository;
import de.tum.in.www1.artemis.repository.ProgrammingExerciseTestCaseRepository;
import de.tum.in.www1.artemis.security.Role;
import de.tum.in.www1.artemis.service.AuthorizationCheckService;

/**
 * REST controller for managing {@link de.tum.in.www1.artemis.domain.hestia.ProgrammingExerciseSolutionEntry}.
 */
@RestController
@RequestMapping("/api")
public class ProgrammingExerciseSolutionEntryResource {

    private final Logger log = LoggerFactory.getLogger(ProgrammingExerciseSolutionEntryResource.class);

    private final ProgrammingExerciseSolutionEntryRepository programmingExerciseSolutionEntryRepository;

    private final ProgrammingExerciseRepository programmingExerciseRepository;

    private final ProgrammingExerciseTaskRepository programmingExerciseTaskRepository;

    private final ProgrammingExerciseTestCaseRepository programmingExerciseTestCaseRepository;

    private final AuthorizationCheckService authCheckService;

    public ProgrammingExerciseSolutionEntryResource(ProgrammingExerciseSolutionEntryRepository programmingExerciseSolutionEntryRepository,
            ProgrammingExerciseRepository programmingExerciseRepository, ProgrammingExerciseTaskRepository programmingExerciseTaskRepository,
            ProgrammingExerciseTestCaseRepository programmingExerciseTestCaseRepository, AuthorizationCheckService authCheckService) {
        this.programmingExerciseSolutionEntryRepository = programmingExerciseSolutionEntryRepository;
        this.programmingExerciseTaskRepository = programmingExerciseTaskRepository;
        this.programmingExerciseTestCaseRepository = programmingExerciseTestCaseRepository;
        this.authCheckService = authCheckService;
        this.programmingExerciseRepository = programmingExerciseRepository;
    }

    /**
     * {@code GET  /programming-exercise-solution-entries/:id} : get the "id" programmingExerciseSolutionEntry.
     *
     * @param programmingExerciseSolutionEntryId the id of the programmingExerciseSolutionEntry to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the programmingExerciseSolutionEntry, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/programming-exercise-solution-entries/{programmingExerciseSolutionEntryId}")
    @PreAuthorize("hasRole('EDITOR')")
    public ResponseEntity<ProgrammingExerciseSolutionEntry> getProgrammingSolutionEntry(@PathVariable Long programmingExerciseSolutionEntryId) {
        log.debug("REST request to get Programming Exercise Solution Entry : {}", programmingExerciseSolutionEntryId);
        var solutionEntry = programmingExerciseSolutionEntryRepository.findByIdWithTestCaseAndProgrammingExerciseElseThrow(programmingExerciseSolutionEntryId);
        authCheckService.checkHasAtLeastRoleForExerciseElseThrow(Role.EDITOR, solutionEntry.getTestCase().getExercise(), null);
        return ResponseEntity.ok().body(solutionEntry);
    }

    /**
     * {@code GET  /programming-exercises/:exerciseId/programming-exercise-solution-entries} : get the programmingExerciseSolutionEntries of a provided exercise.
     *
     * @param exerciseId the exercise id of which to retrieve the solution entries.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the programmingExerciseSolutionEntries, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/programming-exercises/{exerciseId}/programming-exercise-solution-entries")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Set<ProgrammingExerciseSolutionEntry>> getProgrammingSolutionEntryForProgrammingExercise(@PathVariable Long exerciseId) {
        log.debug("REST request to get Programming Exercise Solution Entry : {}", exerciseId);
        ProgrammingExercise programmingExercise = programmingExerciseRepository.findByIdElseThrow(exerciseId);
        authCheckService.checkHasAtLeastRoleForExerciseElseThrow(Role.STUDENT, programmingExercise, null);
        Set<ProgrammingExerciseSolutionEntry> solutionEntries = programmingExerciseTestCaseRepository.findByExerciseIdWithSolutionEntries(programmingExercise.getId()).stream()
                .flatMap(testCase -> testCase.getSolutionEntries().stream()).collect(Collectors.toSet());
        return ResponseEntity.ok(solutionEntries);
    }

    /**
     * {@code GET  /programming-exercise-tasks/:taskId/programming-exercise-solution-entries} : get the programmingExerciseSolutionEntries of a provided exercise task.
     *
     * @param taskId the task id of which to retrieve the solution entries.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the programmingExerciseSolutionEntries, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/programming-exercise-tasks/{taskId}/programming-exercise-solution-entries")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Set<ProgrammingExerciseSolutionEntry>> getProgrammingSolutionEntryForProgrammingExerciseTask(@PathVariable Long taskId) {
        log.debug("REST request to get Programming Exercise Solution Entry : {}", taskId);
        ProgrammingExerciseTask programmingExerciseTask = programmingExerciseTaskRepository.findByIdWithTestCaseAndSolutionEntriesAndProgrammingExerciseElseThrow(taskId);
        ProgrammingExercise programmingExercise = programmingExerciseRepository.findByIdElseThrow(programmingExerciseTask.getExercise().getId());
        authCheckService.checkHasAtLeastRoleForExerciseElseThrow(Role.STUDENT, programmingExercise, null);

        Set<ProgrammingExerciseSolutionEntry> solutionEntries = programmingExerciseTask.getTestCases().stream().flatMap(testCase -> testCase.getSolutionEntries().stream())
                .collect(Collectors.toSet());
        return ResponseEntity.ok(solutionEntries);
    }
}
