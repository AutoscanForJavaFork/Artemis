package de.tum.in.www1.artemis.domain.hestia;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import de.tum.in.www1.artemis.domain.DomainObject;
import de.tum.in.www1.artemis.domain.ProgrammingExerciseTestCase;

/**
 * A ProgrammingExerciseSolutionEntry.
 */
@Entity
@Table(name = "programming_exercise_solution_entry")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProgrammingExerciseSolutionEntry extends DomainObject {

    @Column(name = "file_path")
    private String filePath;

    // The line where the old code segment is in the template
    @Column(name = "previous_line")
    private Integer previousLine;

    // The line where the new code segment is in the solution
    @Column(name = "line")
    private Integer line;

    // The old code segment to be replaced by the new code segment
    @Column(name = "previous_code")
    private String previousCode;

    // The new code segment that replaces the old code segment
    @Column(name = "code")
    private String code;

    // Fetched lazily, as we never need the code hint when fetching solution entries
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties("solutionEntries")
    private CodeHint codeHint;

    @ManyToOne
    @JsonIgnoreProperties("solutionEntries")
    private ProgrammingExerciseTestCase testCase;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String file) {
        this.filePath = file;
    }

    public Integer getPreviousLine() {
        return previousLine;
    }

    public void setPreviousLine(Integer previousLine) {
        this.previousLine = previousLine;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public String getPreviousCode() {
        return previousCode;
    }

    public void setPreviousCode(String previousCode) {
        this.previousCode = previousCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public CodeHint getCodeHint() {
        return codeHint;
    }

    public void setCodeHint(CodeHint codeHint) {
        this.codeHint = codeHint;
    }

    public ProgrammingExerciseTestCase getTestCase() {
        return this.testCase;
    }

    public void setTestCase(ProgrammingExerciseTestCase testCase) {
        this.testCase = testCase;
    }
}
