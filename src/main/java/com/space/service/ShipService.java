package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;

public interface ShipService {
    Ship getById(Long id);

    void save(Ship ship);

    void delete(Long id);

    List<Ship> filteredData(Specification<Ship> spec);

    List<Ship> filteredData(Specification<Ship> spec, Pageable pageable);
}
