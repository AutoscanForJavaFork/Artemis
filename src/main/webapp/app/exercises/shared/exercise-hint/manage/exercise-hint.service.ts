import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ExerciseService } from 'app/exercises/shared/exercise/exercise.service';
import { ExerciseHint } from 'app/entities/hestia/exercise-hint.model';

export type ExerciseHintResponse = HttpResponse<ExerciseHint>;

export interface IExerciseHintService {
    /**
     * Creates an exercise hint
     * @param exerciseHint Exercise hint to create
     */
    create(exerciseHint: ExerciseHint): Observable<ExerciseHintResponse>;

    /**
     * Updates an exercise hint
     * @param exerciseHint Exercise hint to update
     */
    update(exerciseHint: ExerciseHint): Observable<ExerciseHintResponse>;

    /**
     * Finds an exercise hint
     * @param id Id of exercise hint to find
     */
    find(id: number): Observable<ExerciseHintResponse>;

    /**
     * Finds all exercise hints by exercise id
     * @param exerciseId Id of exercise
     */
    findByExerciseId(exerciseId: number): Observable<HttpResponse<ExerciseHint[]>>;

    /**
     * Deletes an exercise hint
     * @param id Id of exercise hint to delete
     */
    delete(id: number): Observable<HttpResponse<void>>;
}

@Injectable({ providedIn: 'root' })
export class ExerciseHintService implements IExerciseHintService {
    public resourceUrl = SERVER_API_URL + 'api/exercise-hints';

    constructor(protected http: HttpClient) {}

    /**
     * Creates an exercise hint
     * @param exerciseHint Exercise hint to create
     */
    create(exerciseHint: ExerciseHint): Observable<ExerciseHintResponse> {
        exerciseHint.exercise = ExerciseService.convertDateFromClient(exerciseHint.exercise!);
        if (exerciseHint.exercise.categories) {
            exerciseHint.exercise.categories = ExerciseService.stringifyExerciseCategories(exerciseHint.exercise);
        }
        return this.http.post<ExerciseHint>(this.resourceUrl, exerciseHint, { observe: 'response' });
    }

    /**
     * Updates an exercise hint
     * @param exerciseHint Exercise hint to update
     */
    update(exerciseHint: ExerciseHint): Observable<ExerciseHintResponse> {
        exerciseHint.exercise = ExerciseService.convertDateFromClient(exerciseHint.exercise!);
        exerciseHint.exercise.categories = ExerciseService.stringifyExerciseCategories(exerciseHint.exercise);
        return this.http.put<ExerciseHint>(`${this.resourceUrl}/${exerciseHint.id}`, exerciseHint, { observe: 'response' });
    }

    /**
     * Finds an exercise hint
     * @param exerciseHintId Id of exercise hint to find
     */
    find(exerciseHintId: number): Observable<ExerciseHintResponse> {
        return this.http.get<ExerciseHint>(`${this.resourceUrl}/${exerciseHintId}`, { observe: 'response' });
    }

    /**
     * Finds all exercise hints by exercise id
     * @param exerciseId Id of exercise
     */
    findByExerciseId(exerciseId: number): Observable<HttpResponse<ExerciseHint[]>> {
        return this.http.get<ExerciseHint[]>(`api/exercises/${exerciseId}/exercise-hints`, { observe: 'response' });
    }

    /**
     * Fetches the title of the exercise hint with the given id
     *
     * @param exerciseHintId the id of the hint
     * @return the title of the hint in an HttpResponse, or an HttpErrorResponse on error
     */
    getTitle(exerciseHintId: number): Observable<HttpResponse<string>> {
        return this.http.get(`${this.resourceUrl}/${exerciseHintId}/title`, { observe: 'response', responseType: 'text' });
    }

    /**
     * Deletes an exercise hint
     * @param exerciseHintId Id of exercise hint to delete
     */
    delete(exerciseHintId: number): Observable<HttpResponse<void>> {
        return this.http.delete<void>(`${this.resourceUrl}/${exerciseHintId}`, { observe: 'response' });
    }
}
