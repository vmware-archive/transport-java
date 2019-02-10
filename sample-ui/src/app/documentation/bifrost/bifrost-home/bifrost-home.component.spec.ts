import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BifrostHomeComponent } from './bifrost-home.component';

describe('BifrostHomeComponent', () => {
  let component: BifrostHomeComponent;
  let fixture: ComponentFixture<BifrostHomeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BifrostHomeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BifrostHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
