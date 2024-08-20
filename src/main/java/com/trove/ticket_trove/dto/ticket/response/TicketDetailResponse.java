package com.trove.ticket_trove.dto.ticket.response;

import com.trove.ticket_trove.model.entity.ticket.TicketEntity;

import java.time.LocalDateTime;

public record TicketDetailResponse(
        Long ticketId,
        String name,
        String email,
        String concertName,
        String performer,
        String grade,
        Integer seatNumber,
        Integer price,
        LocalDateTime showStart,
        LocalDateTime showEnd,
        LocalDateTime createdAt,
        LocalDateTime deletedAt
) {
    public static TicketDetailResponse from(
            TicketEntity ticketEntity
    ) {
        var concertEntity = ticketEntity.getConcertId();
        return new TicketDetailResponse(
                ticketEntity.getId(),
                ticketEntity.getMemberEmail().getName(),
                ticketEntity.getMemberEmail().getEmail(),
                concertEntity.getConcertName(),
                concertEntity.getPerformer(),
                ticketEntity.getSeatGrade().getGrade(),
                ticketEntity.getSeatNumber(),
                ticketEntity.getSeatGrade().getPrice(),
                concertEntity.getShowStart(),
                concertEntity.getShowEnd(),
                ticketEntity.getCreatedAt(),
                ticketEntity.getDeletedAt());
    }

    public static TicketDetailResponse from(
            Object object
    ) {
        if (object != null) {
            var ticketEntity = (TicketEntity) object;
            return from(ticketEntity);
        }
        return null;
    }
}