import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ImportingComponent } from './importing.component';

describe('ImportingComponent', () => {
  let component: ImportingComponent;
  let fixture: ComponentFixture<ImportingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ImportingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ImportingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
