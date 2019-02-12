import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TsConfiguringAngularComponent } from './ts-configuring-angular.component';

describe('TsConfiguringAngularComponent', () => {
  let component: TsConfiguringAngularComponent;
  let fixture: ComponentFixture<TsConfiguringAngularComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TsConfiguringAngularComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TsConfiguringAngularComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
