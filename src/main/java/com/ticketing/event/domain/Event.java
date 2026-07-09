package com.ticketing.event.domain;

import com.ticketing.support.entity.BaseEntity;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "events")
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;


    private Event(String title, String description) {
        validateTitle(title);
        this.title = title;
        this.description = description;
    }

    public static Event create(String title, String description) {
        return new Event(title, description);
    }

    public void update(String title, String description) {
        if (title != null) {
            validateTitle(title);
            this.title = title;
        }

        if (description != null) {
            this.description = description.isBlank() ? null : description;
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new CoreException(ErrorType.INVALID_EVENT_TITLE);
        }
    }

}
