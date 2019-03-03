import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TypescriptBadgesComponent } from './typescript-badges.component';

describe('TypescriptBadgesComponent', () => {
  let component: TypescriptBadgesComponent;
  let fixture: ComponentFixture<TypescriptBadgesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TypescriptBadgesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TypescriptBadgesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
