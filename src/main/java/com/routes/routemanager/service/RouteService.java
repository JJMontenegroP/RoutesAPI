package com.routes.routemanager.service;

import com.routes.routemanager.model.Route;
import com.routes.routemanager.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
public class RouteService {

    private final RouteRepository journeyRepository;

    @Autowired
    public RouteService(RouteRepository journeyRepository) {
        this.journeyRepository = journeyRepository;
    }

    @Transactional
    public Route createJourney(Route journey) {
        try {
            // Validate date formats
            if (isValidDateTime(journey.getPlannedStartDate().toString()) || isValidDateTime(journey.getPlannedEndDate().toString())) {
                throw new IllegalArgumentException("Invalid date format");
            }
            // Ensure start date is before end date
            if (journey.getPlannedStartDate().isAfter(journey.getPlannedEndDate())) {
                throw new IllegalArgumentException("Start date must be before end date");
            }

            if (journey.getPlannedStartDate().isBefore(Instant.now()) || journey.getPlannedEndDate().isBefore(Instant.now())) {
                throw new IllegalArgumentException("Start date must be after current date");
            }

            journeyRepository.save(journey);
            return journey;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Las fechas del trayecto no son válidas");
        }
    }

    // Method to validate date-time format
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static boolean isValidDateTime(String dateTimeString) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);
            LocalDateTime currentUTC = LocalDateTime.now(ZoneOffset.UTC);
            return !dateTime.isBefore(currentUTC);
        } catch (DateTimeParseException e) {
            return false; // Invalid format
        }
    }

    public boolean isFlightIdExists(String flightId) {
        return journeyRepository.existsByFlightId(flightId);
    }

    public Route getJourneyById(UUID id) {
        if (id == null) {
            return null; // or throw an IllegalArgumentException
        }
        return journeyRepository.findById(id.toString()).orElse(null);
    }

    public boolean deleteJourneyById(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        journeyRepository.deleteById(uuid.toString());
        return true;
    }

    public void resetDatabase() {
        journeyRepository.deleteAll();
    }

    public boolean existsById(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return journeyRepository.existsById(uuid.toString());
    }

    public List<Route> getRoutesByFlightId(String flightId) {
        return journeyRepository.findByFlightId(flightId);
    }

    public List<Route> getAllRoutes() {
        return journeyRepository.findAll();
    }
}
