import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JavaConfiguringComponent } from './java-configuring.component';

describe('JavaConfiguringComponent', () => {
  let component: JavaConfiguringComponent;
  let fixture: ComponentFixture<JavaConfiguringComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JavaConfiguringComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JavaConfiguringComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
