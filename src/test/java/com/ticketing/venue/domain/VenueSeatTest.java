package com.ticketing.venue.domain;

import com.ticketing.venue.repository.VenueRepository;
import com.ticketing.venue.repository.VenueSeatRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class VenueSeatRepositoryTest {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private VenueSeatRepository venueSeatRepository;

    @Test
    @DisplayName("동일 공연장에서 좌석 코드는 중복될 수 없다.")
    void duplicateSeatCodeInSameVenueIsNotAllowed() {
        Venue venue = venueRepository.save(Venue.create("올림픽홀", "서울특별시 송파구 올림픽대로 424"));

        VenueSeat seat1 = VenueSeat.create(venue, "A", "1", "1");
        VenueSeat seat2 = VenueSeat.create(venue, "A", "1", "1");

        venueSeatRepository.save(seat1);

        Assertions.assertThatThrownBy(() -> {
            venueSeatRepository.saveAndFlush(seat2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("다른 공연장이라면 같은 좌석 코드를 사용할 수 있다.")
    void sameSeatCodeInDifferentVenueIsAllowed() {
        Venue venue1 = venueRepository.save(Venue.create("올림픽홀", "서울특별시 송파구 올림픽대로 424"));
        Venue venue2 = venueRepository.save(Venue.create("잠실체육관", "서울특별시 송파구 올림픽로 25"));

        venueSeatRepository.saveAndFlush(VenueSeat.create(venue1, "A", "1", "1"));
        venueSeatRepository.saveAndFlush(VenueSeat.create(venue2, "A", "1", "1"));

    }
}