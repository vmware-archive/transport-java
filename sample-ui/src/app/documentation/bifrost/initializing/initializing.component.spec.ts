import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { InitializingComponent } from './initializing.component';

describe('InitializingComponent', () => {
  let component: InitializingComponent;
  let fixture: ComponentFixture<InitializingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ InitializingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InitializingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
