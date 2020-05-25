package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/rest/ships")
public class ShipRestController {

    @Autowired
    private ShipService shipService;

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public ResponseEntity<Ship> getShip(@PathVariable("id") Long shipId) {
        if (shipId == null || shipId % 1 != 0 || shipId < 1) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Ship ship = null;
        try {
            ship = this.shipService.getById(shipId);
        }
        catch (NoSuchElementException e) {
            return new ResponseEntity<Ship>(HttpStatus.NOT_FOUND);
        }
        if (ship == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Ship>> getAllShips(@RequestParam(required = false) String name,
                                                  @RequestParam(required = false) String planet,
                                                  @RequestParam(required = false) ShipType shipType,
                                                  @RequestParam(required = false) Long after,
                                                  @RequestParam(required = false) Long before,
                                                  @RequestParam(required = false) Boolean isUsed,
                                                  @RequestParam(required = false) Double minSpeed,
                                                  @RequestParam(required = false) Double maxSpeed,
                                                  @RequestParam(required = false) Integer minCrewSize,
                                                  @RequestParam(required = false) Integer maxCrewSize,
                                                  @RequestParam(required = false) Double minRating,
                                                  @RequestParam(required = false) Double maxRating,
                                                  @RequestParam(required = false) ShipOrder order,
                                                  @RequestParam(required = false) Integer pageNumber,
                                                  @RequestParam(required = false) Integer pageSize) {
        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 3;
        if (order == null) order = ShipOrder.ID;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
        List<Ship> ships = shipService.filteredData(filterData(name, planet, shipType, after, before, isUsed,
                minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating), pageable);
        if (ships.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(ships, HttpStatus.OK);
    }

    @RequestMapping(value = "count", method = RequestMethod.GET)
    public ResponseEntity<Integer> getShipCount(@RequestParam(required = false) String name,
                                                @RequestParam(required = false) String planet,
                                                @RequestParam(required = false) ShipType shipType,
                                                @RequestParam(required = false) Long after,
                                                @RequestParam(required = false) Long before,
                                                @RequestParam(required = false) Boolean isUsed,
                                                @RequestParam(required = false) Double minSpeed,
                                                @RequestParam(required = false) Double maxSpeed,
                                                @RequestParam(required = false) Integer minCrewSize,
                                                @RequestParam(required = false) Integer maxCrewSize,
                                                @RequestParam(required = false) Double minRating,
                                                @RequestParam(required = false) Double maxRating) {
        return new ResponseEntity<>(shipService.filteredData(filterData(name, planet, shipType, after, before,
                isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating)).size(), HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Ship> saveShip(@RequestBody Ship ship) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (ship.isUsed() == null) ship.setUsed(false);
        if (ship.getName() == null || ship.getPlanet() == null || ship.getShipType() == null ||
                ship.getProdDate() == null || ship.getSpeed() == null || ship.getCrewSize() == null ||
                ship.getName().length() > 50 || ship.getPlanet().length() > 50 || ship.getName().isEmpty() ||
                ship.getPlanet().isEmpty() || Math.round(ship.getSpeed() * 100) / 100.0 < 0.01 ||
                Math.round(ship.getSpeed() * 100) / 100.0 > 0.99 || ship.getCrewSize() < 1 || ship.getCrewSize() > 9999 ||
                ship.getProdDate().getYear() < 900 || ship.getProdDate().getYear() > 1119)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        ship.setRating(Math.round(100 * 80 * ship.getSpeed() * (ship.isUsed() ? 0.5 : 1) / (3019 - ship.getProdDate().getYear() - 1899)) / 100.0);
        this.shipService.save(ship);
        return new ResponseEntity<>(ship, httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.POST)
    public ResponseEntity<Ship> updateShip(@PathVariable("id") Long shipId, @RequestBody(required = false) Ship shipData) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (shipId == null || shipId % 1 != 0 || shipId < 1) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Ship ship = null;
        try {
            ship = this.shipService.getById(shipId);
        }
        catch (NoSuchElementException e) {
            return new ResponseEntity<Ship>(HttpStatus.NOT_FOUND);
        }
        if (ship == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (shipData.getName() != null) {
            if (shipData.getName().length() > 50 || shipData.getName().isEmpty())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            else ship.setName(shipData.getName());
        }
        if (shipData.getPlanet() != null) {
            if (shipData.getPlanet().length() > 50 || shipData.getPlanet().isEmpty())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            else ship.setPlanet(shipData.getPlanet());
        }
        if (shipData.getShipType() != null) ship.setShipType(shipData.getShipType());
        if (shipData.getProdDate() != null) {
            if (shipData.getProdDate().getYear() < 900 || shipData.getProdDate().getYear() > 1119)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            else ship.setProdDate(shipData.getProdDate());
        }
        if (shipData.isUsed() != null) ship.setUsed(shipData.isUsed());
        if (shipData.getSpeed() != null) {
            if (Math.round(shipData.getSpeed() * 100) / 100.0 < 0.01 ||
                    Math.round(shipData.getSpeed() * 100) / 100.0 > 0.99)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            else ship.setSpeed(shipData.getSpeed());
        }
        if (shipData.getCrewSize() != null) {
            if (shipData.getCrewSize() < 1 || shipData.getCrewSize() > 9999)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            else ship.setCrewSize(shipData.getCrewSize());
        }
        ship.setRating(Math.round(100 * 80 * ship.getSpeed() * (ship.isUsed() ? 0.5 : 1) / (3019 - ship.getProdDate().getYear() - 1899)) / 100.0);
        this.shipService.save(ship);
        return new ResponseEntity<>(ship, httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Ship> deleteShip(@PathVariable("id") Long shipId) {
        if (shipId == null || shipId % 1 != 0 || shipId < 1) return new ResponseEntity<Ship>(HttpStatus.BAD_REQUEST);
        Ship ship = null;
        try {
            ship = this.shipService.getById(shipId);
        }
        catch (NoSuchElementException e) {
            return new ResponseEntity<Ship>(HttpStatus.NOT_FOUND);
        }
        if (ship == null) return new ResponseEntity<Ship>(HttpStatus.NOT_FOUND);
        this.shipService.delete(shipId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Specification<Ship> filterData(String name, String planet, ShipType shipType, Long after, Long before,
                                  Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
                                  Integer maxCrewSize, Double minRating, Double maxRating) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();

                if(name != null) predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
                if(planet != null) predicates.add(criteriaBuilder.like(root.get("planet"), "%" + planet + "%"));
                if(shipType != null) predicates.add(criteriaBuilder.equal(root.get("shipType"), shipType));
                if (after != null || before != null) {
                    if (after == null) predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"), new Date(before)));
                    else if (before == null) predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), new Date(after)));
                    else predicates.add(criteriaBuilder.between(root.get("prodDate"), new Date(after), new Date(before)));
                }
                if (isUsed != null) predicates.add(criteriaBuilder.equal(root.get("isUsed"), isUsed));
                if (minSpeed != null || maxSpeed != null) {
                    if (minSpeed == null) predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("speed"), maxSpeed));
                    else if (maxSpeed == null) predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("speed"), minSpeed));
                    else predicates.add(criteriaBuilder.between(root.get("speed"), minSpeed, maxSpeed));
                }
                if (minCrewSize != null || maxCrewSize != null) {
                    if (minCrewSize == null) predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("crewSize"), maxCrewSize));
                    else if (maxCrewSize == null) predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("crewSize"), minCrewSize));
                    else predicates.add(criteriaBuilder.between(root.get("crewSize"), minCrewSize, maxCrewSize));
                }
                if (minRating != null || maxRating != null) {
                    if (minRating == null) predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("rating"), maxRating));
                    else if (maxRating == null) predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating));
                    else predicates.add(criteriaBuilder.between(root.get("rating"), minRating, maxRating));
                }
                return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).distinct(true).getRestriction();
            }
        };
    }
}
