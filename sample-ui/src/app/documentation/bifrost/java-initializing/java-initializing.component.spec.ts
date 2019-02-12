import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JavaInitializingComponent } from './java-initializing.component';

describe('JavaInitializingComponent', () => {
  let component: JavaInitializingComponent;
  let fixture: ComponentFixture<JavaInitializingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JavaInitializingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JavaInitializingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
