package com.trove.ticket_trove.model.entity.ticket;


import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@Table(
        name = "ticket",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "member_email", "seat_grade",
                                "concert_id", "seat_number"
                        })
        },
        indexes = {
            @Index(name = "idx_ticket_conid_s_grade_s_number",
                    columnList =
                            "concert_id, seat_grade, " +
                            "seat_number, member_email")
        }
)
@SQLDelete(sql = "UPDATE ticket " +
        "SET deleted_at = CURRENT_TIMESTAMP " +
        "WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
public class TicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_email")
    private MemberEntity memberEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id")
    private ConcertEntity concertId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_grade")
    private SeatGradeEntity seatGrade;

    @Column(name = "seat_number")
    private Integer seatNumber;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    public static TicketEntity from(
            MemberEntity memberEmail,
            ConcertEntity concertId,
            SeatGradeEntity seatGradeEntity,
            Integer seatNumber
    ) {
        return TicketEntity.builder()
                .memberEmail(memberEmail)
                .concertId(concertId)
                .seatGrade(seatGradeEntity)
                .seatNumber(seatNumber)
                .build();
    }
}
