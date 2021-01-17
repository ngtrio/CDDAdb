import { TestBed } from '@angular/core/testing';

import { ShowcaseService } from './showcase.service';

describe('HomeService', () => {
  let service: ShowcaseService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ShowcaseService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
