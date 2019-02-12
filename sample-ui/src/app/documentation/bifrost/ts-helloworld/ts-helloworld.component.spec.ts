import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TsHelloworldComponent } from './ts-helloworld.component';

describe('TsHelloworldComponent', () => {
  let component: TsHelloworldComponent;
  let fixture: ComponentFixture<TsHelloworldComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TsHelloworldComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TsHelloworldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
