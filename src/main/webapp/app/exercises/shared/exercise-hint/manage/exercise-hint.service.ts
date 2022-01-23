import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ExerciseHint } from 'app/entities/hestia/exercise-hint.model';

export type ExerciseHintResponse = HttpResponse<ExerciseHint>;

export interface IExerciseHintService {
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
}

@Injectable({ providedIn: 'root' })
export class ExerciseHintService implements IExerciseHintService {
    public resourceUrl = SERVER_API_URL + 'api/exercise-hints';

    constructor(protected http: HttpClient) {}

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
     * Fetches the title of the hint with the given id
     *
     * @param exerciseHintId the id of the hint
     * @return the title of the hint in an HttpResponse, or an HttpErrorResponse on error
     */
    getTitle(exerciseHintId: number): Observable<HttpResponse<string>> {
        return this.http.get(`${this.resourceUrl}/${exerciseHintId}/title`, { observe: 'response', responseType: 'text' });
    }
}
