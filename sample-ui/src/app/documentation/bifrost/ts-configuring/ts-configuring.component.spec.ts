import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TsConfiguringComponent } from './ts-configuring.component';

describe('TsConfiguringComponent', () => {
  let component: TsConfiguringComponent;
  let fixture: ComponentFixture<TsConfiguringComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TsConfiguringComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TsConfiguringComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
