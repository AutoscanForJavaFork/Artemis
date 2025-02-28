import { ArtemisTestModule } from '../../test.module';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StructuredGradingInstructionsAssessmentLayoutComponent } from 'app/assessment/structured-grading-instructions-assessment-layout/structured-grading-instructions-assessment-layout.component';
import { GradingInstruction } from 'app/exercises/shared/structured-grading-criterion/grading-instruction.model';
import { MockPipe } from 'ng-mocks';
import { ArtemisTranslatePipe } from 'app/shared/pipes/artemis-translate.pipe';

describe('StructuredGradingInstructionsAssessmentLayoutComponent', () => {
    let comp: StructuredGradingInstructionsAssessmentLayoutComponent;
    let fixture: ComponentFixture<StructuredGradingInstructionsAssessmentLayoutComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [ArtemisTestModule],
            declarations: [StructuredGradingInstructionsAssessmentLayoutComponent, MockPipe(ArtemisTranslatePipe)],
            providers: [],
        })
            .compileComponents()
            .then(() => {
                fixture = TestBed.createComponent(StructuredGradingInstructionsAssessmentLayoutComponent);
                comp = fixture.componentInstance;
            });
    });

    it('should initialize', () => {
        comp.readonly = true;
        comp.ngOnInit();
        expect(comp.allowDrop).toBe(false);
        expect(comp.disableDrag()).toBe(false);
    });

    it('should set display elements', () => {
        const gradingInstruction = { id: 1, feedback: 'feedback', credits: 1 } as GradingInstruction;

        expect(comp.setScore(gradingInstruction.credits)).toBe('1P');
        expect(comp.setTooltip(gradingInstruction)).toBe('Feedback: feedback');
        expect(comp.setInstrColour(gradingInstruction)).toBe('#e3f0da');
        gradingInstruction.credits = 0;
        fixture.detectChanges();
        expect(comp.setInstrColour(gradingInstruction)).toBe('#fff2cc');
        gradingInstruction.credits = -1;
        fixture.detectChanges();
        expect(comp.setInstrColour(gradingInstruction)).toBe('#fbe5d6');
    });
});
