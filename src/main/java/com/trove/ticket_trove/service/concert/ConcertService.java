package com.trove.ticket_trove.service.concert;

import com.trove.ticket_trove.dto.concert.request.ConcertCreateRequest;
import com.trove.ticket_trove.dto.concert.request.ConcertUpdateRequest;
import com.trove.ticket_trove.dto.concert.response.ConcertDetailsInfoResponse;
import com.trove.ticket_trove.dto.concert.response.ConcertInfoResponse;
import com.trove.ticket_trove.dto.concert.response.ConcertUpdateResponse;
import com.trove.ticket_trove.dto.seatGrade.request.SeatGradeUpdateRequest;
import com.trove.ticket_trove.dto.seatGrade.response.SeatGradeInfoResponse;
import com.trove.ticket_trove.dto.seatGrade.response.SeatGradeUpdateResponse;
import com.trove.ticket_trove.exception.concert.ConcertExistsException;
import com.trove.ticket_trove.exception.concert.ConcertNotFoundException;
import com.trove.ticket_trove.exception.seatgrade.SeatGradeNotFoundException;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;
import com.trove.ticket_trove.model.storage.concert.ConcertRepository;
import com.trove.ticket_trove.model.storage.seat_grade.SeatGradeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final SeatGradeRepository seatGradeRepository;

    public ConcertService(
            ConcertRepository concertRepository,
            SeatGradeRepository seatGradeRepository) {

        this.concertRepository = concertRepository;
        this.seatGradeRepository = seatGradeRepository;
    }

    //콘서트 생성
    @Transactional
    public void addConcert(ConcertCreateRequest request) {
        validateConcert(request.concertName(), request.performer());
        var concertEntity = ConcertEntity.builder()
                .concertName(request.concertName())
                .performer(request.performer())
                .showStart(request.showStart())
                .showEnd(request.showEnd())
                .ticketingTime(request.ticketingTime())
                .build();
        var seatGradeCreateRequests = request.gradeTypes();

        //공연장 저장
        concertRepository.save(concertEntity);

        //공연장 등급 저장
        seatGradeCreateRequests.stream().map(sg ->
                SeatGradeEntity.from(
                        concertEntity, sg.grade().toUpperCase(),
                        sg.price(), sg.totalSeat()))
                .forEach(seatGradeRepository::save);
    }

    @Transactional
    //콘서트 정보 수정
    public ConcertUpdateResponse updateConcert(ConcertUpdateRequest request) {
        var concertEntity = getConcertEntity(request.concertId());
        List<SeatGradeUpdateResponse> seatGrades = null;

        if (!ObjectUtils.isEmpty(request.concertName()))
            concertEntity.setConcertName(request.concertName());

        if (!ObjectUtils.isEmpty(request.performer()))
            concertEntity.setPerformer(request.performer());

        if (!ObjectUtils.isEmpty(request.showStart()))
            concertEntity.setShowStart(request.showStart());

        if (!ObjectUtils.isEmpty(request.showEnd()))
            concertEntity.setShowEnd(request.showEnd());

        if (!ObjectUtils.isEmpty(request.ticketingTime()))
            concertEntity.setTicketingTime(request.ticketingTime());

        if (!ObjectUtils.isEmpty(request.gradeTypes())) {
            seatGrades = updateSeatGrade(concertEntity, request.gradeTypes());
        } else {
            seatGrades = seatGradeRepository.findByConcertIdOrderByPriceDesc(concertEntity)
                    .stream()
                    .map(SeatGradeUpdateResponse::from)
                    .toList();
        }

        return ConcertUpdateResponse
                .from(concertRepository.save(concertEntity), seatGrades);
    }

    //콘서트 전체 조회
    public List<ConcertInfoResponse> searchConcerts(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);

        return concertRepository.findAllByOrderByShowStartAsc(pageable).stream()
                .map(ConcertInfoResponse::from).toList();
    }

    //콘서트 단건 조회
    public ConcertDetailsInfoResponse searchConcert(Long id) {
        var concertEntity = getConcertEntity(id);
        var seatGrades = seatGradeRepository
                .findByConcertIdOrderByPriceDesc(concertEntity)
                .stream().map(SeatGradeInfoResponse::from)
                .toList();

        return ConcertDetailsInfoResponse.from(concertEntity, seatGrades);
    }

    @Transactional
    //콘서트 정보 삭제
    public void deleteConcert(Long id) {
        var concertEntity = getConcertEntity(id);
        seatGradeRepository.deleteAllByConcertId(concertEntity);
        concertRepository.delete(concertEntity);
    }

    //등급 테이블 업데이트
    private List<SeatGradeUpdateResponse> updateSeatGrade(
            ConcertEntity concertEntity,
            List<SeatGradeUpdateRequest> seatGradeUpdateRequests) {

        return seatGradeUpdateRequests.stream()
                .map(sgq -> {
                    if (!ObjectUtils.isEmpty(sgq.previousGrade())
                        && !ObjectUtils.isEmpty(sgq.previousPrice())) {
                        var seatGradeEntity = getSeatGradeEntity(
                                concertEntity,
                                sgq.previousGrade(),
                                sgq.previousPrice());

                        if (!ObjectUtils.isEmpty(sgq.updateGrade()))
                            seatGradeEntity.setGrade(sgq.updateGrade());

                        if (!ObjectUtils.isEmpty(sgq.updatePrice()))
                            seatGradeEntity.setPrice(sgq.updatePrice());

                        if (!ObjectUtils.isEmpty(sgq.updateTotalSeat()))
                            seatGradeEntity.setTotalSeat(sgq.updateTotalSeat());

                        return SeatGradeUpdateResponse.from(
                                seatGradeRepository.save(seatGradeEntity));
                    }
                    throw new SeatGradeNotFoundException();
                }).toList();
    }

    private SeatGradeEntity getSeatGradeEntity(
            ConcertEntity concertEntity, String grade, Integer price) {
        return seatGradeRepository
                .findByConcertIdAndGradeAndPrice(
                        concertEntity,
                        grade.toUpperCase(),
                        price)
                .orElseThrow(SeatGradeNotFoundException::new);
    }

    private ConcertEntity getConcertEntity(Long id) {
        return concertRepository.findById(id)
                .orElseThrow(ConcertNotFoundException::new);
    }

    private void validateConcert(String concertName, String performer) {
        concertRepository.findByConcertNameAndPerformer(
                concertName, performer)
                .ifPresent(concert -> {
                    throw new ConcertExistsException(concertName, performer);
        });
    }
}
