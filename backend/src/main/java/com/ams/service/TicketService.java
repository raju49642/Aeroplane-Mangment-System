package com.ams.service;
import com.ams.dto.TicketResponse;
import java.util.List;
public interface TicketService { TicketResponse getById(Integer ticketId); TicketResponse getByNumber(String ticketNumber); List<TicketResponse> getByBooking(Integer bookingId); List<TicketResponse> getMyTickets(); }
