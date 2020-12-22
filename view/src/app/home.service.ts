import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class HomeService {

  constructor(private http: HttpClient) { }

  getMonsters(): Observable<Object[]> {
    let uri = 'http://localhost:9000/monster'
    return this.http.get<Object[]>(uri)
  }
}
