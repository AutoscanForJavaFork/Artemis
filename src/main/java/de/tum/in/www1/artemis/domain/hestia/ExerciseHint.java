package de.tum.in.www1.artemis.domain.hestia;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DiscriminatorOptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.tum.in.www1.artemis.domain.DomainObject;
import de.tum.in.www1.artemis.domain.Exercise;

/**
 * An ExerciseHint.
 */
@Entity
@Table(name = "exercise_hint")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discriminator", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("H")
@DiscriminatorOptions(force = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = TextHint.class, name = "text"), @JsonSubTypes.Type(value = CodeHint.class, name = "code") })
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class ExerciseHint extends DomainObject {

    @Column(name = "title")
    private String title;

    @ManyToOne
    @JsonIgnoreProperties("exerciseHints")
    private Exercise exercise;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ExerciseHint title(String title) {
        this.title = title;
        return this;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public ExerciseHint exercise(Exercise exercise) {
        this.exercise = exercise;
        return this;
    }

    public abstract String toString();
}
