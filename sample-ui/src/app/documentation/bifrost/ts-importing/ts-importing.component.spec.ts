import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TsImportingComponent } from './ts-importing.component';

describe('TsImportingComponent', () => {
  let component: TsImportingComponent;
  let fixture: ComponentFixture<TsImportingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TsImportingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TsImportingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
