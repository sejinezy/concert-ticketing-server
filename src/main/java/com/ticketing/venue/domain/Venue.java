package com.ticketing.venue.domain;

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
import org.springframework.util.StringUtils;


@Getter
@Entity
@Table(name = "venues")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Venue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    private Venue(String name, String address) {
        validateName(name);
        validateAddress(address);
        this.name = name;
        this.address = address;
    }

    public static Venue create(String name, String address) {
        return new Venue(name, address);
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("공연장 이름은 필수입니다.");
        }
    }

    private void validateAddress(String address) {
        if (!StringUtils.hasText(address)) {
            throw new CoreException(ErrorType.INVALID_VENUE_ADDRESS);
        }
    }
}
