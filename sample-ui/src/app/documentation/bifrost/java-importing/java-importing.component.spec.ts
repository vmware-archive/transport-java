import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JavaImportingComponent } from './java-importing.component';

describe('JavaImportingComponent', () => {
  let component: JavaImportingComponent;
  let fixture: ComponentFixture<JavaImportingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JavaImportingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JavaImportingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
