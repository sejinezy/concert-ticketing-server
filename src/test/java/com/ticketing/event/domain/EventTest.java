package com.ticketing.event.domain;

import static org.assertj.core.api.Assertions.*;

import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventTest {

    @Test
    @DisplayName("이벤트를 생성할 수 있다.")
    void createEvent() {
        Event event = Event.create("아이유 콘서트", "서울 공연");

        assertThat(event.getTitle()).isEqualTo("아이유 콘서트");
        assertThat(event.getDescription()).isEqualTo("서울 공연");
    }

    @Test
    @DisplayName("이벤트 제목은 필수다.")
    void titleIsRequired() {
        assertThatThrownBy(() -> Event.create(null, "서울 공연"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_EVENT_TITLE.getMessage());
    }

    @Test
    @DisplayName("이벤트 제목은 빈 값일 수 없다.")
    void titleCannotBeBlank() {
        assertThatThrownBy(() -> Event.create("", "서울 공연"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_EVENT_TITLE.getMessage());

        assertThatThrownBy(() -> Event.create("    ", "서울 공연"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_EVENT_TITLE.getMessage());
    }

    @Test
    @DisplayName("이벤트 정보를 수정할 수 있다.")
    void updateEvent() {
        Event event = Event.create("아이유 콘서트", "서울 공연");

        event.update("아이유 앵콜 콘서트", "부산 공연");

        assertThat(event.getTitle()).isEqualTo("아이유 앵콜 콘서트");
        assertThat(event.getDescription()).isEqualTo("부산 공연");
    }

    @Test
    @DisplayName("수정 시 title이 null이면 기존 title을 유지한다.")
    void updateWithNullTitleKeepsOriginalTitle() {
        Event event = Event.create("아이유 콘서트", "서울 공연");

        event.update(null, "부산 공연");

        assertThat(event.getTitle()).isEqualTo("아이유 콘서트");
        assertThat(event.getDescription()).isEqualTo("부산 공연");
    }

    @Test
    @DisplayName("수정 시 description이 null이면 기존 description을 유지한다.")
    void updateWithNullDescriptionKeepsOriginalDescription() {
        Event event = Event.create("아이유 콘서트", "서울 공연");

        event.update("아이유 앵콜 콘서트", null);

        assertThat(event.getTitle()).isEqualTo("아이유 앵콜 콘서트");
        assertThat(event.getDescription()).isEqualTo("서울 공연");
    }

    @Test
    @DisplayName("수정 시 title은 빈 값일 수 없다.")
    void updateTitleCannotBeBlank() {
        Event event = Event.create("아이유 콘서트", "서울 공연");

        assertThatThrownBy(() -> event.update("", "수정된 설명"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_EVENT_TITLE.getMessage());

        assertThatThrownBy(() -> event.update("   ", "수정된 설명"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_EVENT_TITLE.getMessage());
    }
}