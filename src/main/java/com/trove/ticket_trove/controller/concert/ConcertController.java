package com.trove.ticket_trove.controller.concert;

import com.trove.ticket_trove.dto.concert.request.ConcertCreateRequest;
import com.trove.ticket_trove.dto.concert.request.ConcertUpdateRequest;
import com.trove.ticket_trove.dto.concert.response.ConcertDetailsInfoResponse;
import com.trove.ticket_trove.dto.concert.response.ConcertInfoResponse;
import com.trove.ticket_trove.dto.concert.response.ConcertUpdateResponse;
import com.trove.ticket_trove.service.concert.ConcertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/concert")
public class ConcertController {
    private final ConcertService concertService;

    public ConcertController(ConcertService concertService) {
        this.concertService = concertService;
    }
    //콘서트 생성
    @PostMapping
    public ResponseEntity<HttpStatus> addConcert(
            @RequestBody
            ConcertCreateRequest request
    ) {
        concertService.addConcert(request);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    //콘서트 조회
    @GetMapping
    public ResponseEntity<List<ConcertInfoResponse>> getConcerts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "3") Integer size
    ) {
        var concertResponses = concertService.searchConcerts(page, size);
        return ResponseEntity.ok(concertResponses);
    }

    //콘서트 단건 조회
    @GetMapping("/{concertId}")
    public ResponseEntity<ConcertDetailsInfoResponse> getConcert(
            @PathVariable Long concertId) {
        var concertDetailsResponse = concertService.searchConcert(concertId);
        return ResponseEntity.ok(concertDetailsResponse);
    }

    //콘서트정보 수정
    @PatchMapping
    public ResponseEntity<ConcertUpdateResponse> updateConcert(
            @RequestBody
            ConcertUpdateRequest request
    ){
        var concertUpdateResp = concertService.updateConcert(request);

        return ResponseEntity.ok(concertUpdateResp);
    }

    //콘서트 삭제
    @DeleteMapping("/{concertId}")
    public ResponseEntity<HttpStatus> deleteConcert(
            @PathVariable Long concertId
    ) {
        concertService.deleteConcert(concertId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

}
