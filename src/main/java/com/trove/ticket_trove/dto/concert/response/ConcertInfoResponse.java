package com.trove.ticket_trove.dto.concert.response;

import com.trove.ticket_trove.model.entity.concert.ConcertEntity;

import java.time.LocalDateTime;

public record ConcertInfoResponse(
        Long id,
        String concertName,
        String performer,
        LocalDateTime showStart,
        LocalDateTime showEnd
) {

    public static ConcertInfoResponse from(ConcertEntity concert) {
        return new ConcertInfoResponse(
                concert.getId(),
                concert.getConcertName(),
                concert.getPerformer(),
                concert.getShowStart(),
                concert.getShowEnd());
    }
}