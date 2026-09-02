export type Role = 'SUPER_ADMIN' | 'ADMIN' | 'CUSTOMER';
export type AdminStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type SeatCategory = 'BUSINESS' | 'ECONOMY' | 'EXECUTIVE';
export type CustomerCategory = 'REGULAR' | 'SILVER' | 'GOLD' | 'PLATINUM';

export interface User {
  userId: number;
  userName: string;
  role: Role;
  adminStatus?: AdminStatus;
  customerCategory?: CustomerCategory;
  phone?: string;
  emailId?: string;
  address1?: string;
  address2?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  dob?: string;
}

export interface RegisterUserRequest {
  userName: string;
  password: string;
  customerCategory: CustomerCategory;
  phone: string;
  emailId: string;
  address1: string;
  address2?: string;
  city: string;
  state: string;
  zipCode: string;
  dob: string;
  favouriteSport: string;
  favouriteHobby: string;
}

export interface RegisterAdminRequest {
  userName: string;
  password: string;
  emailId: string;
  phone: string;
  favouriteSport: string;
  favouriteHobby: string;
}

export interface LoginRequest {
  userName: string;
  password: string;
}

export interface LoginResponse {
  userId: number;
  userName: string;
  role: Role;
  token: string;
  message: string;
}

export interface AdminRequestResponse {
  userId: number;
  userName: string;
  emailId: string;
  phone: string;
  status: AdminStatus;
}

export interface Carrier {
  carrierId: number;
  carrierName: string;
  carrierCode: string;
  discount30DaysAdvance: number;
  discount60DaysAdvance: number;
  discount90DaysAdvance: number;
  bulkBookingDiscount: number;
  silverUserDiscount: number;
  goldUserDiscount: number;
  platinumUserDiscount: number;
  refund2DaysBefore: number;
  refund10DaysBefore: number;
  refund20DaysOrMore: number;
  businessClassMultiplier: number;
  executiveClassMultiplier: number;
}

export type CarrierRequest = Omit<Carrier, 'carrierId'>;

export interface Flight {
  flightId: number;
  flightNumber: string;
  carrierName: string;
  carrierId: number;
  origin: string;
  destination: string;
  baseFare: number;
  economyFare: number;
  businessFare: number;
  executiveFare: number;
  seatCapacityBusinessClass: number;
  seatCapacityEconomyClass: number;
  seatCapacityExecutiveClass: number;
}

export interface FlightRequest {
  carrierId: number;
  flightNumber: string;
  origin: string;
  destination: string;
  baseFare: number;
  seatCapacityBusinessClass: number;
  seatCapacityEconomyClass: number;
  seatCapacityExecutiveClass: number;
}

export interface FlightScheduleRequest {
  travelDate: string;
  departureTime: string; // "HH:mm"
  arrivalTime: string;   // "HH:mm"
}

export interface FlightSchedule {
  scheduleId: number;
  flightId: number;
  flightNumber: string;
  carrierName: string;
  travelDate: string;
  departureTime: string;
  arrivalTime: string;
  bookedBusinessSeats: number;
  bookedEconomySeats: number;
  bookedExecutiveSeats: number;
  status: 'SCHEDULED' | 'CANCELLED' | 'COMPLETED';
}

export interface FlightScheduleTemplateRequest {
  departureTime: string;
  arrivalTime: string;
  operatingDays: string[];
  effectiveFrom: string;
  effectiveTo?: string | null;
  active?: boolean;
}

export interface FlightScheduleTemplate extends FlightScheduleTemplateRequest {
  scheduleTemplateId: number;
  flightId: number;
  flightNumber: string;
  carrierName: string;
  active: boolean;
}

export interface FlightSearchResult {
  scheduleId: number;
  flightId: number;
  flightNumber: string;
  carrierName: string;
  origin: string;
  destination: string;
  travelDate: string;
  departureTime: string;
  arrivalTime: string;
  baseFare: number;
  economyFare: number;
  businessFare: number;
  executiveFare: number;
  availableBusinessSeats: number;
  availableEconomySeats: number;
  availableExecutiveSeats: number;
}

export interface BookingRequest {
  scheduleId: number;
  noOfSeats: number;
  seatCategory: SeatCategory;
  paymentMethod: PaymentMethod;
  passengers: PassengerRequest[];
}
export interface BookingPaymentRequest { paymentMethod: PaymentMethod; cardHolderName?: string; cardNumber?: string; expiryMonth?: number; expiryYear?: number; cvv?: string; }
export interface PassengerRequest { passengerName: string; age: number; }
export interface PassengerResponse { passengerId: number; passengerName: string; age: number; }

export interface BookingResponse {
  bookingId: number;
  scheduleId: number;
  flightId: number;
  flightNumber: string;
  origin: string;
  destination: string;
  carrierName: string;
  travelDate: string;
  departureTime: string;
  arrivalTime: string;
  userId: number;
  userName: string;
  noOfSeats: number;
  seatCategory: SeatCategory;
  bookingDateTime: string;
  bookingStatus: 'BOOKED' | 'CANCELLED';
  bookingAmount: number;
  ticketNumber?: string;
  passengers: PassengerResponse[];
}

export interface TicketResponse {
  ticketId: number; ticketNumber: string; bookingId: number; paymentId: number; passengerId?: number; passengerName?: string; passengerAge?: number;
  carrierName: string; carrierCode: string; flightNumber: string; origin: string; destination: string;
  travelDate: string; departureTime: string; arrivalTime: string; seatCategory: SeatCategory; numberOfSeats: number;
  baseFare: number; discountAmount: number; paidAmount: number; paymentStatus: string; ticketStatus: 'CONFIRMED' | 'CANCELLED'; issuedAt: string; refundAmount?: number; cancelledAt?: string;
}

export interface CancellationPreview { refundPercentage: number; refundAmount: number; daysRemaining: number; eligible: boolean; reason?: string; }

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export type PaymentMethod = 'CREDIT_CARD' | 'UPI';
export interface MembershipPaymentRequest { customerCategory: CustomerCategory; paymentMethod: PaymentMethod; cardNumber?: string; cardHolderName?: string; expiryMonth?: number; expiryYear?: number; cvv?: string; }
export interface PaymentResponse { paymentId: number; customerCategory: CustomerCategory; amount: number; paymentMethod: PaymentMethod; maskedCardNumber?: string; upiTransactionRef?: string; status: 'PENDING' | 'SUCCESS'; paidAt?: string; qrCodePayload?: string; }
