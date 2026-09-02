import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TicketService } from '../core/services/ticket.service';
import { TicketResponse } from '../core/models/models';

const POLICIES = [
  'This ticket is valid only for the passenger, flight and travel date shown above.',
  'Arrive at the airport sufficiently before the scheduled departure time and carry valid identification.',
  'Seat allocation is subject to availability at the time of booking.',
  'Cancellation and refunds follow the carrier policy configured in AMS and depend on the remaining days before travel.',
  'Flight schedules may change. Check the latest available schedule before travelling.'
];

@Component({ selector: 'app-ticket', standalone: true, imports: [CommonModule, RouterLink, DatePipe], template: `
<main class="container ticket-page">
  <p class="text-muted" *ngIf="loading">Preparing your ticket…</p>
  <div class="alert alert-error" *ngIf="error">{{ error }}</div>
  <article class="ticket card" *ngFor="let ticket of tickets">
    <header><div><span class="brand">✈ AMS</span><p class="text-muted">Electronic travel ticket</p></div><span class="status" [class.cancelled]="ticket.ticketStatus === 'CANCELLED'">{{ ticket.ticketStatus }}</span></header>
    <section class="ticket-number"><span>Ticket number</span><strong>{{ ticket.ticketNumber }}</strong></section>
    <section class="flight"><div><span>{{ ticket.carrierName }}</span><strong>{{ ticket.flightNumber }}</strong></div><div class="route"><strong>{{ ticket.origin }}</strong><span>✈</span><strong>{{ ticket.destination }}</strong></div></section>
    <section class="details"><div><span>Passenger</span><strong>{{ ticket.passengerName || 'Historical booking' }}</strong></div><div *ngIf="ticket.passengerAge"><span>Age</span><strong>{{ ticket.passengerAge }}</strong></div><div><span>Travel date</span><strong>{{ ticket.travelDate | date:'dd MMMM yyyy' }}</strong></div><div><span>Departure</span><strong>{{ ticket.departureTime }}</strong></div><div><span>Arrival</span><strong>{{ ticket.arrivalTime }}</strong></div><div><span>Seat category</span><strong>{{ ticket.seatCategory }}</strong></div></section>
    <section class="fare"><h3>Fare summary</h3><p><span>Base fare × {{ ticket.numberOfSeats }}</span><strong>₹{{ ticket.baseFare * ticket.numberOfSeats | number:'1.2-2' }}</strong></p><p><span>Discount</span><strong>-₹{{ ticket.discountAmount | number:'1.2-2' }}</strong></p><p class="total"><span>Amount paid</span><strong>₹{{ ticket.paidAmount | number:'1.2-2' }}</strong></p><p><span>Payment</span><strong>{{ ticket.paymentStatus }} · #{{ ticket.paymentId }}</strong></p></section>
    <section class="cancel-note" *ngIf="ticket.ticketStatus === 'CANCELLED'"><h3>Booking cancelled</h3><p>Original paid amount: ₹{{ ticket.paidAmount | number:'1.2-2' }} · Refund: ₹{{ ticket.refundAmount || 0 | number:'1.2-2' }}</p></section>
    <section class="policies"><h3>Important travel policies</h3><ol><li *ngFor="let policy of policies">{{ policy }}</li></ol><small>These are AMS demonstration/business rules and are not legal airline policy.</small></section>
    <footer><a routerLink="/bookings" class="btn btn-secondary">View My Bookings</a><button class="btn btn-primary" type="button" (click)="print()">Print Ticket</button></footer>
  </article>
</main>`, styles: [`
.ticket { max-width: 850px; margin: 0 auto; }.ticket header,.flight,.details,.ticket footer { display:flex; justify-content:space-between; gap:18px; }.brand { font-size:1.55rem; font-weight:900; }.status { align-self:flex-start; padding:8px 12px; background:rgba(50,215,164,.2); color:#9ff8d8; border:1px solid rgba(50,215,164,.4); border-radius:999px; font-weight:800; }.status.cancelled { background:rgba(255,87,113,.2); color:#ffb8c4; border-color:rgba(255,120,143,.4); }.ticket-number { margin:22px 0; padding:17px 0; border-top:1px dashed rgba(255,255,255,.35); border-bottom:1px dashed rgba(255,255,255,.35); display:flex; flex-direction:column; gap:5px; }.ticket-number span,.details span { color:var(--ams-muted); font-size:.82rem; }.ticket-number strong { font-size:1.8rem; letter-spacing:.08em; }.flight { align-items:center; padding-bottom:20px; }.flight div { display:grid; gap:6px; }.route { flex:1; display:flex !important; justify-content:center; align-items:center; gap:15px; font-size:1.15rem; }.details { display:grid; grid-template-columns:repeat(5,1fr); padding:18px 0; border-block:1px solid rgba(255,255,255,.16); }.details div { display:grid; gap:5px; }.fare,.policies,.cancel-note { margin-top:22px; padding:18px; border-radius:12px; background:rgba(255,255,255,.07); }.fare p { display:flex; justify-content:space-between; gap:16px; }.total { padding-top:12px; border-top:1px solid rgba(255,255,255,.2); font-size:1.08rem; }.cancel-note { border:1px solid rgba(255,120,143,.4); }.policies li { margin:9px 0; color:#e4ecfb; }.ticket footer { margin-top:22px; } @media(max-width:650px){.flight,.ticket header,.ticket footer{flex-direction:column}.details{grid-template-columns:repeat(2,1fr)}.route{justify-content:flex-start}.ticket-number strong{font-size:1.35rem}}
` ] })
export class TicketComponent implements OnInit { tickets:TicketResponse[]=[]; loading=true; error=''; policies=POLICIES; constructor(private route:ActivatedRoute,private service:TicketService){} ngOnInit(){this.service.getByBooking(Number(this.route.snapshot.paramMap.get('bookingId'))).subscribe({next:t=>{this.tickets=t;this.loading=false;},error:(e:HttpErrorResponse)=>{this.error=e.status===404?'Tickets could not be found for this booking.':'Unable to load your tickets. Please try again.';this.loading=false;}});} print(){window.print();} }
