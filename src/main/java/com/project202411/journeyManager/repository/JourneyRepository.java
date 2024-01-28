package com.project202411.journeyManager.repository;

import com.project202411.journeyManager.model.Journey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JourneyRepository extends JpaRepository<Journey, String> {
    boolean existsByFlightId(String flightId);
    List<Journey> findByFlightId(String flightId);
}
