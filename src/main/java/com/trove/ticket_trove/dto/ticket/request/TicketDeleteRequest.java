package com.trove.ticket_trove.dto.ticket.request;

import jakarta.validation.constraints.NotBlank;

public record TicketDeleteRequest(
        @NotBlank
        Long concertId,
        @NotBlank
        String seatGrade,
        @NotBlank
        Integer seatNumber,
        @NotBlank
        String email
) {
}