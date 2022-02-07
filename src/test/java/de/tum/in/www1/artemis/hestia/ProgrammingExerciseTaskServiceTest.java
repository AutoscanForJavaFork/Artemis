package de.tum.in.www1.artemis.hestia;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.tum.in.www1.artemis.AbstractSpringIntegrationBambooBitbucketJiraTest;
import de.tum.in.www1.artemis.domain.ProgrammingExercise;
import de.tum.in.www1.artemis.domain.ProgrammingExerciseTestCase;
import de.tum.in.www1.artemis.domain.hestia.CodeHint;
import de.tum.in.www1.artemis.domain.hestia.ProgrammingExerciseTask;
import de.tum.in.www1.artemis.repository.ProgrammingExerciseRepository;
import de.tum.in.www1.artemis.repository.hestia.CodeHintRepository;
import de.tum.in.www1.artemis.repository.hestia.ProgrammingExerciseTaskRepository;
import de.tum.in.www1.artemis.service.hestia.ProgrammingExerciseTaskService;

public class ProgrammingExerciseTaskServiceTest extends AbstractSpringIntegrationBambooBitbucketJiraTest {

    @Autowired
    private ProgrammingExerciseTaskService programmingExerciseTaskService;

    @Autowired
    private ProgrammingExerciseTaskRepository programmingExerciseTaskRepository;

    @Autowired
    private ProgrammingExerciseRepository programmingExerciseRepository;

    @Autowired
    private CodeHintRepository codeHintRepository;

    private ProgrammingExercise programmingExercise;

    @BeforeEach
    void init() {
        database.addCourseWithOneProgrammingExerciseAndSpecificTestCases();
        database.addUsers(2, 2, 1, 2);

        programmingExercise = programmingExerciseRepository.findAll().get(0);
        updateProblemStatement("""
                [task][Task 1](testClass[BubbleSort])
                [task][Task 2](testMethods[Context])
                """);
    }

    private void updateProblemStatement(String problemStatement) {
        programmingExercise.setProblemStatement(problemStatement);
        programmingExercise = programmingExerciseRepository.save(programmingExercise);
        programmingExerciseTaskService.updateTasksFromProblemStatement(programmingExercise);
    }

    @AfterEach
    public void resetDatabase() {
        database.resetDatabase();
    }

    @Test
    public void testNewExercise() {
        assertThat(programmingExerciseTaskRepository.findAll()).hasSize(2);
        var tasks = programmingExerciseTaskRepository.findByExerciseIdWithTestCases(programmingExercise.getId());
        assertThat(tasks).hasSize(2);

        assertThat(tasks).anyMatch(programmingExerciseTask -> {
            var testCases = programmingExerciseTask.getTestCases();
            return programmingExerciseTask.getTaskName().equals("Task 1") && !testCases.isEmpty()
                    && testCases.stream().findFirst().get().getTestName().equals("testClass[BubbleSort]");
        });
        assertThat(tasks).anyMatch(programmingExerciseTask -> {
            var testCases = programmingExerciseTask.getTestCases();
            return programmingExerciseTask.getTaskName().equals("Task 2") && !testCases.isEmpty()
                    && testCases.stream().findFirst().get().getTestName().equals("testMethods[Context]");
        });
    }

    @Test
    public void testAddTask() {
        var previousTaskIds = programmingExerciseTaskRepository.findByExerciseIdWithTestCases(programmingExercise.getId()).stream().map(ProgrammingExerciseTask::getId)
                .collect(Collectors.toSet());

        updateProblemStatement("""
                [task][Task 1](testClass[BubbleSort])
                [task][Task 2](testMethods[Context])
                [task][Task 3](testMethods[Policy])
                """);
        assertThat(programmingExerciseTaskRepository.findAll()).hasSize(3);
        var tasks = programmingExerciseTaskRepository.findByExerciseIdWithTestCases(programmingExercise.getId());
        assertThat(tasks).hasSize(3);

        assertThat(tasks).anyMatch(programmingExerciseTask -> {
            var testCases = programmingExerciseTask.getTestCases();
            return programmingExerciseTask.getTaskName().equals("Task 1") && !testCases.isEmpty()
                    && testCases.stream().findFirst().get().getTestName().equals("testClass[BubbleSort]");
        });
        assertThat(tasks).anyMatch(programmingExerciseTask -> {
            var testCases = programmingExerciseTask.getTestCases();
            return programmingExerciseTask.getTaskName().equals("Task 2") && !testCases.isEmpty()
                    && testCases.stream().findFirst().get().getTestName().equals("testMethods[Context]");
        });
        assertThat(tasks).anyMatch(programmingExerciseTask -> {
            var testCases = programmingExerciseTask.getTestCases();
            return programmingExerciseTask.getTaskName().equals("Task 3") && !testCases.isEmpty()
                    && testCases.stream().findFirst().get().getTestName().equals("testMethods[Policy]");
        });

        // Test that the other tasks were not removed and re-added.
        var newTaskIds = tasks.stream().map(ProgrammingExerciseTask::getId).collect(Collectors.toSet());
        assertThat(newTaskIds).containsAll(previousTaskIds);
    }

    @Test
    public void testRemoveAllTasks() {
        updateProblemStatement("Empty");
        assertThat(programmingExerciseTaskRepository.findAll()).isEmpty();
    }

    @Test
    public void testReduceToOneTask() {
        updateProblemStatement("[task][Task 1](testClass[BubbleSort],testMethods[Context],testMethods[Policy])");
        assertThat(programmingExerciseTaskRepository.findAll()).hasSize(1);
        var tasks = programmingExerciseTaskRepository.findByExerciseIdWithTestCases(programmingExercise.getId());
        assertThat(tasks).hasSize(1);
        var task = tasks.stream().findFirst().get();
        assertThat(task.getTaskName()).isEqualTo("Task 1");
        assertThat(task.getTestCases()).hasSize(3);
        assertThat(Set.of("testClass[BubbleSort]", "testMethods[Context]", "testMethods[Policy]"))
                .isEqualTo(task.getTestCases().stream().map(ProgrammingExerciseTestCase::getTestName).collect(Collectors.toSet()));
    }

    /**
     * Tests that renaming a task does not remove and read the task, but instead updates it
     */
    @Test
    public void testRenameTask() {
        var previousTaskIds = programmingExerciseTaskRepository.findByExerciseIdWithTestCases(programmingExercise.getId()).stream().map(ProgrammingExerciseTask::getId)
                .collect(Collectors.toSet());

        updateProblemStatement("""
                [task][Task 1a](testClass[BubbleSort])
                [task][Task 2](testMethods[Context])
                """);

        assertThat(programmingExerciseTaskRepository.findAll()).hasSize(2);
        var tasks = programmingExerciseTaskRepository.findByExerciseIdWithTestCases(programmingExercise.getId());

        var newTaskIds = tasks.stream().map(ProgrammingExerciseTask::getId).collect(Collectors.toSet());
        assertThat(previousTaskIds).isEqualTo(newTaskIds);

        assertThat(tasks).isEqualTo(programmingExerciseTaskRepository.findByExerciseIdWithTestCases(programmingExercise.getId()));

        assertThat(tasks).anyMatch(programmingExerciseTask -> {
            var testCases = programmingExerciseTask.getTestCases();
            return programmingExerciseTask.getTaskName().equals("Task 1a") && !testCases.isEmpty()
                    && testCases.stream().findFirst().get().getTestName().equals("testClass[BubbleSort]");
        });
        assertThat(tasks).anyMatch(programmingExerciseTask -> {
            var testCases = programmingExerciseTask.getTestCases();
            return programmingExerciseTask.getTaskName().equals("Task 2") && !testCases.isEmpty()
                    && testCases.stream().findFirst().get().getTestName().equals("testMethods[Context]");
        });
    }

    /**
     * Tests that not changing any tasks in the problem statment will not update any tasks
     */
    @Test
    public void testNoChanges() {
        var previousTaskIds = programmingExerciseTaskRepository.findByExerciseIdWithTestCases(programmingExercise.getId()).stream().map(ProgrammingExerciseTask::getId)
                .collect(Collectors.toSet());

        updateProblemStatement("""
                Test
                [task][Task 1](testClass[BubbleSort])
                [task][Task 2](testMethods[Context])
                """);

        assertThat(programmingExerciseTaskRepository.findAll()).hasSize(2);

        var newTaskIds = programmingExerciseTaskRepository.findByExerciseIdWithTestCases(programmingExercise.getId()).stream().map(ProgrammingExerciseTask::getId)
                .collect(Collectors.toSet());
        assertThat(previousTaskIds).isEqualTo(newTaskIds);
    }

    @Test
    public void testDeleteWithCodeHints() {
        var task = programmingExerciseTaskRepository.findByExerciseId(programmingExercise.getId()).stream().filter(task1 -> task1.getTaskName().equals("Task 1")).findFirst()
                .orElse(null);
        assertThat(task).isNotNull();

        var codeHint = new CodeHint();
        codeHint.setExercise(programmingExercise);
        codeHint.setProgrammingExerciseTask(task);
        codeHintRepository.save(codeHint);

        programmingExerciseTaskService.delete(task);
        assertThat(programmingExerciseTaskRepository.findAll()).hasSize(1);
        assertThat(programmingExerciseTaskRepository.findById(task.getId())).isEmpty();
        assertThat(codeHintRepository.findAll()).isEmpty();
    }
}
