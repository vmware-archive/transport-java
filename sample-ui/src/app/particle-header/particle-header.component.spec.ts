import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ParticleHeaderComponent } from './particle-header.component';

describe('ParticleHeaderComponent', () => {
  let component: ParticleHeaderComponent;
  let fixture: ComponentFixture<ParticleHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ParticleHeaderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ParticleHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
