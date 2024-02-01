package com.routes.routemanager.controller;


import com.routes.routemanager.loader.AirportCodeLoader;
import com.routes.routemanager.model.Route;
import com.routes.routemanager.model.RouteResponse;
import com.routes.routemanager.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping()
public class RouteController {

    private final RouteService routeService;


    @Autowired
    public RouteController(RouteService routeService, AirportCodeLoader airportCodeLoader) {
        this.routeService = routeService;
        this.airportCodeLoader = airportCodeLoader;
    }

    private final AirportCodeLoader airportCodeLoader;

    // Crea un trayecto con los datos brindados, solo un usuario autorizado puede realizar esta operación.

    @PostMapping("/routes")
    public ResponseEntity<?> createJourney(@RequestBody Route route) {

        // Validar la presencia de todos los campos necesarios
        ResponseEntity<?> missingFieldsResponse = validateRequiredFields(route);
        if (missingFieldsResponse != null) {
            return missingFieldsResponse;
        }

        // Validar que los códigos de aeropuerto sean válidos
        if (isValidAirportCode(route.getSourceAirportCode()) || isValidAirportCode(route.getDestinyAirportCode())) {
            return ResponseEntity.badRequest().body("{\"msg\": \"Código de aeropuerto inválido\"}");
        }
        try {
            // Validar si el flightId ya existe
            if (routeService.isFlightIdExists(route.getFlightId())) {
                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
            }

            Route createdJourney = routeService.createJourney(route);
            RouteResponse routeResponse = new RouteResponse(createdJourney.getId(), createdJourney.getCreatedAt());
            return new ResponseEntity<>(routeResponse, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("{\"msg\": \"Las fechas del trayecto no son válidas\"}");
        }
    }

    // Retorna todos los trayectos o aquellos que corresponden a los parámetros de búsqueda. Solo un usuario autorizado puede realizar esta operación.

    @GetMapping("/routes")
    public ResponseEntity<?> getJourneys(@RequestParam(required = false) String flight) {
        System.err.println("flightId: " + flight);
        try {
            // Si se proporciona el parámetro flightId, buscar trayectos por ese id
            if (flight != null && !flight.isEmpty()) {
                List<Route> routes = routeService.getRoutesByFlightId(flight);
                return ResponseEntity.ok(routes);
            } else {
                // Si no se proporciona el parámetro flightId, retornar todos los trayectos
                List<Route> routes = routeService.getAllRoutes();
                return ResponseEntity.ok(routes);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Retorna un trayecto, solo un usuario autorizado puede realizar esta operación.
    @GetMapping("/routes/{routeId}")
    public ResponseEntity<?> getJourneyById(@PathVariable String routeId) {
        try {
            UUID uuid = UUID.fromString(routeId); // Attempt to convert the string to UUID
            Route route = routeService.getJourneyById(uuid);
            if (route != null) {
                return ResponseEntity.ok(route);
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
            if (routeService.existsById(uuid)) {
                boolean deleted = routeService.deleteJourneyById(uuid);
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
        routeService.resetDatabase();
        return ResponseEntity.ok("{\"msg\": \"Todos los datos fueron eliminados\"}");
    }

    /******************************* Validations ********************************************/

    // Método para validar la presencia de todos los campos necesarios
    private ResponseEntity<?> validateRequiredFields(Route route) {
        if (route.getFlightId() == null || route.getSourceAirportCode() == null || route.getSourceCountry() == null || route.getDestinyAirportCode() == null || route.getDestinyCountry() == null || route.getBagCost() == null || route.getPlannedStartDate() == null || route.getPlannedEndDate() == null) {
            return ResponseEntity.badRequest().build();
        }
        return null; // Indicates all required fields are present
    }

    // Método para validar códigos de aeropuerto
    private boolean isValidAirportCode(String airportCode) {
        Set<String> validAirportCodes = airportCodeLoader.getAirportCodes();
        return !validAirportCodes.contains(airportCode.toUpperCase());
    }
}
