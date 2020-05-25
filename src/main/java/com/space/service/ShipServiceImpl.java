package com.space.service;

import com.space.model.Ship;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShipServiceImpl implements ShipService {

    @Autowired
    ShipRepository shipRepository;

    @Override
    public Ship getById(Long id) {
        return shipRepository.findById(id).get();
    }

    @Override
    public void save(Ship ship) {
        shipRepository.save(ship);
    }

    @Override
    public void delete(Long id) {
        shipRepository.deleteById(id);
    }

    @Override
    public List<Ship> filteredData(Specification<Ship> spec) {
        return shipRepository.findAll(spec);
    }

    @Override
    public List<Ship> filteredData(Specification<Ship> spec, Pageable pageable) {
        return shipRepository.findAll(spec, pageable).getContent();
    }
}
