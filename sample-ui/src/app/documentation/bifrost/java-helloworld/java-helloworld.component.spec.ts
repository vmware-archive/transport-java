import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JavaHelloworldComponent } from './java-helloworld.component';

describe('JavaHelloworldComponent', () => {
  let component: JavaHelloworldComponent;
  let fixture: ComponentFixture<JavaHelloworldComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JavaHelloworldComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JavaHelloworldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
