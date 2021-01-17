import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, Subject} from "rxjs";
import {filter, map} from "rxjs/operators";
import {flatMap} from "rxjs/internal/operators";

interface AccGet {
  type: string,
  id: string
}

@Injectable({
  providedIn: 'root'
})
export class ShowcaseService {

  private listAllReq = new Subject<string>()
  private getOneReq = new Subject<AccGet>()

  constructor(private http: HttpClient) {
  }

  listAll(type: string): void {
    this.listAllReq.next(type)
  }

  getOne(type: string, id: string) {
    this.getOneReq.next({
      type: type,
      id: id
    })
    console.log(type, id)
  }

  doListAll(): Observable<[string, Object[]]> {
    return this.listAllReq.pipe(
      flatMap(type => {
        let uri = `http://localhost:3000/${type}`
        return this.http.get<Object[]>(uri).pipe(map(objects => {
          let res: [string, Object[]] = [type, objects]
          return res
        }))
      })
    )
  }

  doGetOne(): Observable<Object> {
    return this.getOneReq.pipe(
      flatMap(accGet => {
        let {type, id} = accGet
        let uri = `http://localhost:3000/${type}/${id}`
        console.log(uri)
        return this.http.get<Object>(uri)
      })
    )
  }
}
