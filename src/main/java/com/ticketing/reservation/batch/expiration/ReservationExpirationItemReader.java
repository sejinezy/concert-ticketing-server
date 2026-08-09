package com.ticketing.reservation.batch.expiration;

import com.ticketing.reservation.domain.ReservationStatus;
import com.ticketing.reservation.repository.projection.ReservationExpirationTarget;
import java.time.LocalDateTime;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.database.Order;
import org.springframework.batch.infrastructure.item.database.PagingQueryProvider;
import org.springframework.batch.infrastructure.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class ReservationExpirationItemReader extends JdbcPagingItemReader<ReservationExpirationTarget> {

    public static final String READER_NAME = "reservationExpirationItemReader";

    public ReservationExpirationItemReader(
            DataSource dataSource,

            @Value(
                    "#{stepExecutionContext['reservationExpiration.cutoffAt']}"
            )
            String cutoffAt,

            @Value("${reservation.expiration.chunk-size:100}")
            int pageSize

    ) {
        super(dataSource, createQueryProvider());

        LocalDateTime cutoffDateTime = LocalDateTime.parse(cutoffAt);

        setName(READER_NAME);
        setPageSize(pageSize);
        setFetchSize(pageSize);
        setSaveState(true);

        setParameterValues(
                Map.of(
                        "status",
                        ReservationStatus.RESERVED.name(),

                        "cutoffAt",
                        cutoffDateTime
                )

        );

        setRowMapper(
                (resultSet, rowNumber) ->
                        new ReservationExpirationTarget(
                                resultSet.getLong("id"),
                                resultSet.getLong("performance_seat_id")
                        )
        );
    }

    private static PagingQueryProvider createQueryProvider() {
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();

        queryProvider.setSelectClause(
                """
                 r.id AS id,
                 r.performance_seat_id AS performance_seat_id
                 """
        );

        queryProvider.setFromClause(
                "reservations r"
        );

        queryProvider.setWhereClause(
                """
                r.status = :status
                AND r.expires_at <= :cutoffAt
                """
        );

        queryProvider.setSortKeys(
                Map.of(
                        "id",
                        Order.ASCENDING
                )
        );

        return queryProvider;
    }

}
