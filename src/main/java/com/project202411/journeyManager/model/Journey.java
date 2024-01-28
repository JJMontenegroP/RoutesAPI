package com.project202411.journeyManager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "journey")
public class Journey {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "flight_id", nullable = false)
    private String flightId;

    @Column(name = "source_airport_code", nullable = false)
    private String sourceAirportCode;

    @Column(name = "source_country", nullable = false)
    private String sourceCountry;

    @Column(name = "destiny_airport_code", nullable = false)
    private String destinyAirportCode;

    @Column(name = "destiny_country", nullable = false)
    private String destinyCountry;

    @Column(name = "bag_cost", nullable = false)
    private Integer bagCost;

    @Column(name = "bag_weight", nullable = false)
    private LocalDateTime plannedStartDate;

    @Column(name = "planned_end_date", nullable = false)
    private LocalDateTime plannedEndDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Other entity fields and methods

    @PrePersist
    protected void onCreate() {
        // Obtener la fecha y hora actual en UTC+0
        ZonedDateTime utcDateTime = ZonedDateTime.now(ZoneId.of("UTC"));

        // Asignar el tiempo de creación a la fecha y hora actual en UTC+0
        createdAt = utcDateTime.toLocalDateTime();
    }


    @Column(name = "updated_at", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;


    // Constructor

    //toString

    @Override
    public String toString() {
        return "Journey{" +
                "id='" + id + '\'' +
                ", flightId='" + flightId + '\'' +
                ", sourceAirportCode='" + sourceAirportCode + '\'' +
                ", sourceCountry='" + sourceCountry + '\'' +
                ", destinyAirportCode='" + destinyAirportCode + '\'' +
                ", destinyCountry='" + destinyCountry + '\'' +
                ", bagCost=" + bagCost +
                ", plannedStartDate=" + plannedStartDate +
                ", plannedEndDate=" + plannedEndDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
