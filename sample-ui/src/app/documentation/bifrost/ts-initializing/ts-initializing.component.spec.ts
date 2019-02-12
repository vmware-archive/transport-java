import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TsInitializingComponent } from './ts-initializing.component';

describe('TsInitializingComponent', () => {
  let component: TsInitializingComponent;
  let fixture: ComponentFixture<TsInitializingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TsInitializingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TsInitializingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
