package com.project202411.journeyManager.service;

import com.project202411.journeyManager.model.Journey;
import com.project202411.journeyManager.repository.JourneyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
public class JourneyService {

    private final JourneyRepository journeyRepository;

    @Autowired
    public JourneyService(JourneyRepository journeyRepository) {
        this.journeyRepository = journeyRepository;
    }

    @Transactional
    public Journey createJourney(Journey journey) {
        try {
            // Validate date formats
            if (!isValidDateTime(journey.getPlannedStartDate().toString()) ||
                    !isValidDateTime(journey.getPlannedEndDate().toString())) {
                throw new IllegalArgumentException("Invalid date format");
            }

            // Ensure start date is before end date
            if (journey.getPlannedStartDate().isAfter(journey.getPlannedEndDate())) {
                throw new IllegalArgumentException("Start date must be before end date");
            }

            journeyRepository.save(journey);
            return journey;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Las fechas del trayecto no son válidas");
        }
    }

    // Method to validate date-time format
    private boolean isValidDateTime(String dateTimeString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime.parse(dateTimeString, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public boolean isFlightIdExists(String flightId) {
        return journeyRepository.existsByFlightId(flightId);
    }

    public List<Journey> getJourneysByFlightId(String flightId) {
        return journeyRepository.findByFlightId(flightId);
    }

    public List<Journey> getAllJourneys() {
        return journeyRepository.findAll();
    }

    public Journey getJourneyById(UUID id) {
        System.err.println("id: " + id);
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
}
