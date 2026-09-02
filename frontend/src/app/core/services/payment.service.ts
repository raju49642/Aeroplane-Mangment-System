import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MembershipPaymentRequest, PaymentResponse } from '../models/models';
@Injectable({providedIn:'root'}) export class PaymentService {
 constructor(private http:HttpClient){}
 pay(request:MembershipPaymentRequest):Observable<PaymentResponse>{return this.http.post<PaymentResponse>(`${environment.apiBaseUrl}/payments/membership`,request);}
 confirmUpi(paymentId:number):Observable<PaymentResponse>{return this.http.post<PaymentResponse>(`${environment.apiBaseUrl}/payments/membership/${paymentId}/confirm`,{});}
 mine():Observable<PaymentResponse[]>{return this.http.get<PaymentResponse[]>(`${environment.apiBaseUrl}/payments/my`);}
}
