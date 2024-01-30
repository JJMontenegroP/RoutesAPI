package com.routes.routemanager.repository;

import com.routes.routemanager.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, String> {
    boolean existsByFlightId(String flightId);

    List<Route> findByFlightId(String flightId);
}
