package com.ticketing.venue.domain;

import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "venue_seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_venue_seats_venue_id_seat_code",
                        columnNames = {"venue_id", "seat_code"}
                )
        }
)
public class VenueSeat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "venue_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_venue_seats_venue_id")
    )
    private Venue venue;

    @Column(name = "section", nullable = false, length = 20)
    private String section;

    @Column(name = "row_label", nullable = false, length = 10)
    private String rowLabel;

    @Column(name = "seat_no", nullable = false, length = 10)
    private String seatNo;

    @Column(name = "seat_code", nullable = false, length = 50)
    private String seatCode;


    private VenueSeat(
            Venue venue,
            String section,
            String rowLabel,
            String seatNo
    ) {
        validateVenue(venue);
        validateSection(section);
        validateRowLabel(rowLabel);
        validateSeatNo(seatNo);

        String seatCode = generateSeatCode(section, rowLabel, seatNo);

        this.venue = venue;
        this.section = section;
        this.rowLabel = rowLabel;
        this.seatNo = seatNo;
        this.seatCode = seatCode;

    }

    private String generateSeatCode(String section, String rowLabel, String seatNo) {
        return section + "-" + rowLabel + "-" + seatNo;
    }

    public static VenueSeat create(
            Venue venue,
            String section,
            String rowLabel,
            String seatNo
    ) {
        return new VenueSeat(venue, section, rowLabel, seatNo);
    }

    private void validateVenue(Venue venue) {
        if (venue == null) {
            throw new IllegalArgumentException("좌석은 반드시 공연장에 속해야 합니다.");
        }
    }

    private void validateSection(String section) {
        validateRequired(section, "구역은 필수입니다.");
    }

    private void validateRowLabel(String rowLabel) {
        validateRequired(rowLabel, "열은 필수입니다.");
    }

    private void validateSeatNo(String seatNo) {
        validateRequired(seatNo, "좌석 번호는 필수입니다.");
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

}
