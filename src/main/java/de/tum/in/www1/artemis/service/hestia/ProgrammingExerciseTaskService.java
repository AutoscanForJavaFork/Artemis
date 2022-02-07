package de.tum.in.www1.artemis.service.hestia;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.ProgrammingExercise;
import de.tum.in.www1.artemis.domain.hestia.ProgrammingExerciseTask;
import de.tum.in.www1.artemis.repository.ProgrammingExerciseTestCaseRepository;
import de.tum.in.www1.artemis.repository.hestia.CodeHintRepository;
import de.tum.in.www1.artemis.repository.hestia.ProgrammingExerciseTaskRepository;

@Service
public class ProgrammingExerciseTaskService {

    private ProgrammingExerciseTaskRepository programmingExerciseTaskRepository;

    private ProgrammingExerciseTestCaseRepository programmingExerciseTestCaseRepository;

    private CodeHintRepository codeHintRepository;

    public ProgrammingExerciseTaskService(ProgrammingExerciseTaskRepository programmingExerciseTaskRepository,
            ProgrammingExerciseTestCaseRepository programmingExerciseTestCaseRepository, CodeHintRepository codeHintRepository) {
        this.programmingExerciseTaskRepository = programmingExerciseTaskRepository;
        this.programmingExerciseTestCaseRepository = programmingExerciseTestCaseRepository;
        this.codeHintRepository = codeHintRepository;
    }

    /**
     * Deletes a ProgrammingExerciseTask together with its CodeHints
     * This has to be manually done, as there is no orphanRemoval between the two entities
     *
     * @param task The task to delete
     */
    public void delete(ProgrammingExerciseTask task) {
        var codeHints = codeHintRepository.findByTaskId(task.getId());
        codeHintRepository.deleteAll(codeHints);
        programmingExerciseTaskRepository.delete(task);
    }

    /**
     * Extracts all tasks from the problem statement of an exercise and saves them to the database.
     * All tasks that no longer exist will be deleted.
     * If there is already a task with the same test cases as a new one, but with a different name the existing one will be renamed.
     *
     * @param exercise The programming exercise to extract the tasks from
     */
    public void updateTasksFromProblemStatement(ProgrammingExercise exercise) {
        var previousTasks = programmingExerciseTaskRepository.findByExerciseIdWithTestCases(exercise.getId());
        var extractedTasks = extractTasks(exercise);
        // No changes
        if (previousTasks.equals(extractedTasks)) {
            return;
        }
        // Add all tasks that did not change
        var newTasks = new HashSet<>(previousTasks);
        newTasks.retainAll(extractedTasks);
        previousTasks.removeAll(newTasks);
        extractedTasks.removeAll(newTasks);
        // Check for tasks where only the name changed
        Iterator<ProgrammingExerciseTask> extractedTaskIterator = extractedTasks.iterator();
        while (extractedTaskIterator.hasNext()) {
            ProgrammingExerciseTask extractedTask = extractedTaskIterator.next();
            Iterator<ProgrammingExerciseTask> previousTaskIterator = previousTasks.iterator();
            while (previousTaskIterator.hasNext()) {
                ProgrammingExerciseTask previousTask = previousTaskIterator.next();
                if (previousTask.getTestCases().equals(extractedTask.getTestCases())) {
                    previousTask.setTaskName(extractedTask.getTaskName());
                    newTasks.add(previousTask);
                    extractedTaskIterator.remove();
                    previousTaskIterator.remove();
                    break;
                }
            }
        }
        // Add all newly created tasks
        newTasks.addAll(extractedTasks);
        // Remove old tasks
        for (ProgrammingExerciseTask task : previousTasks) {
            delete(task);
        }
        // Save all tasks
        for (ProgrammingExerciseTask task : newTasks) {
            task.setExercise(exercise);
            programmingExerciseTaskRepository.save(task);
        }
    }

    private Set<ProgrammingExerciseTask> extractTasks(ProgrammingExercise exercise) {
        var problemStatement = exercise.getProblemStatement();
        var pattern = Pattern.compile("\\[task]\\[(?<name>[^\\[\\]]+)]\\((?<tests>.*)\\)");
        var matcher = pattern.matcher(problemStatement);
        var testCases = programmingExerciseTestCaseRepository.findByExerciseIdAndActive(exercise.getId(), true);
        var tasks = new HashSet<ProgrammingExerciseTask>();
        while (matcher.find()) {
            var taskName = matcher.group("name");
            var testCaseNames = matcher.group("tests");

            var task = new ProgrammingExerciseTask();
            task.setTaskName(taskName);
            task.setExercise(exercise);
            String[] testNames = testCaseNames.split(",");
            for (String testName : testNames) {
                testCases.stream().filter(tc -> tc.getTestName().equals(testName)).findFirst().ifPresent(task.getTestCases()::add);
            }
            tasks.add(task);
        }
        return tasks;
    }
}
