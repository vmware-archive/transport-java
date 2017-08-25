import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PeerListComponent } from './peer-list.component';

describe('PeerListComponent', () => {
  let component: PeerListComponent;
  let fixture: ComponentFixture<PeerListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PeerListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PeerListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
