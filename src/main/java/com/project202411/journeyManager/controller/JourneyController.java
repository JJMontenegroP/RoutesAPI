package com.project202411.journeyManager.controller;


import com.project202411.journeyManager.loader.AirportCodeLoader;
import com.project202411.journeyManager.model.Journey;
import com.project202411.journeyManager.model.JourneyResponse;
import com.project202411.journeyManager.service.JourneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping()
public class JourneyController {

    private final JourneyService journeyService;


    @Autowired
    public JourneyController(JourneyService journeyService) {
        this.journeyService = journeyService;
    }

    @Autowired
    private AirportCodeLoader airportCodeLoader;

    // Crea un trayecto con los datos brindados, solo un usuario autorizado puede realizar esta operación.

    @PostMapping("/routes")
    public ResponseEntity<?> createJourney(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody Journey journey) {

        // Validar la presencia de todos los campos necesarios
        if (journey.getFlightId() == null ||
                journey.getSourceAirportCode() == null ||
                journey.getSourceCountry() == null ||
                journey.getDestinyAirportCode() == null ||
                journey.getDestinyCountry() == null ||
                journey.getBagCost() == null ||
                journey.getPlannedStartDate() == null ||
                journey.getPlannedEndDate() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Validar que los códigos de aeropuerto sean válidos
        if (isValidAirportCode(journey.getSourceAirportCode()) ||
                isValidAirportCode(journey.getDestinyAirportCode())) {
            return ResponseEntity.badRequest().body("{\"msg\": \"Código de aeropuerto inválido\"}");
        }
        try {
            // Validar si el flightId ya existe
            if (journeyService.isFlightIdExists(journey.getFlightId())) {
                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                        .build();
            }

            Journey createdJourney = journeyService.createJourney(journey);
            JourneyResponse journeyResponse = new JourneyResponse(createdJourney.getId(), createdJourney.getCreatedAt());
            return new ResponseEntity<>(journeyResponse, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                    .body("{\"msg\": \"Las fechas del trayecto no son válidas\"}");
        }
    }

    // Retorna todos los trayectos o aquellos que corresponden a los parámetros de búsqueda. Solo un usuario autorizado puede realizar esta operación.

    @GetMapping("/routes")
    public ResponseEntity<?> getJourneys(@RequestParam(required = false) String flightId) {
        try {
            // Si se proporciona el parámetro flightId, buscar trayectos por ese id
            if (flightId != null && !flightId.isEmpty()) {
                List<Journey> journey = journeyService.getJourneysByFlightId(flightId);
                return ResponseEntity.ok(journey);
            } else {
                // Si no se proporciona el parámetro flightId, retornar todos los trayectos
                List<Journey> journeys = journeyService.getAllJourneys();
                return ResponseEntity.ok(journeys);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Retorna un trayecto, solo un usuario autorizado puede realizar esta operación.
    @GetMapping("/routes/{id}")
    public ResponseEntity<?> getJourneyById(@PathVariable String id) {
        try {
            UUID uuid = UUID.fromString(id); // Attempt to convert the string to UUID
            Journey journey = journeyService.getJourneyById(uuid);
            if (journey != null) {
                return ResponseEntity.ok(journey);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Elimina el trayecto, solo un usuario autorizado puede realizar esta operación.

    @DeleteMapping("/routes/{id}")
    public ResponseEntity<?> deleteJourneyById(@PathVariable String id) {
        try {
            UUID uuid = UUID.fromString(id); // Intenta convertir la cadena a UUID

            // Verifica si el trayecto existe antes de intentar eliminarlo
            if (journeyService.existsById(uuid)) {
                boolean deleted = journeyService.deleteJourneyById(uuid);
                if (deleted) {
                    return ResponseEntity.ok().body("{\"msg\": \"el trayecto fue eliminado\"}"); // Retorna 204 No Content si se eliminó con éxito
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"msg\": \"ocurrió un error al intentar eliminar el trayecto\"}");
                }
            } else {
                return ResponseEntity.notFound().build(); // Retorna 404 Not Found si el trayecto no existe
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"msg\": \"ocurrió un error interno\"}");
        }
    }

    // Usado para verificar el estado del servicio.

    @GetMapping("/routes/ping")
    public ResponseEntity<String> pingService() {
        return ResponseEntity.ok("pong");
    }

    // Usado para reiniciar la base de datos.

    @PostMapping("/routes/reset")
    @Transactional
    public ResponseEntity<String> resetDatabase() {
        journeyService.resetDatabase();
        return ResponseEntity.ok("{\"msg\": \"Todos los datos fueron eliminados\"}");
    }

    // Método para validar códigos de aeropuerto
    private boolean isValidAirportCode(String airportCode) {
        Set<String> validAirportCodes = airportCodeLoader.getAirportCodes();
        return !validAirportCodes.contains(airportCode.toUpperCase());
    }
}
