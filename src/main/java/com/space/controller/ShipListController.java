package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class ShipListController {
    private final ShipRepository repository;

    public ShipListController(ShipRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/rest/ships", method = RequestMethod.GET)
    List<Ship> getAll(@RequestParam(value = "name", required = false) String name,
                      @RequestParam(value = "planet", required = false) String planet,
                      @RequestParam(value = "shipType", required = false) ShipType shipType,
                      @RequestParam(value = "after", required = false) Long after,
                      @RequestParam(value = "before", required = false) Long before,
                      @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                      @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                      @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                      @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                      @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                      @RequestParam(value = "minRating", required = false) Double minRating,
                      @RequestParam(value = "maxRating", required = false) Double maxRating,
                      @RequestParam(value = "order", required = false) ShipOrder order,
                      @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                      @RequestParam(value = "pageSize", required = false) Integer pageSize) {

        List<Ship> filtered = getFilteredShips(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);


        int pageSizeParam = pageSize == null ? 3 : pageSize;
        int pageNumberParam = pageNumber == null ? 0 : pageNumber;

        int firstNumber = pageSizeParam * pageNumberParam;
        int lastNumber = Math.min(firstNumber + pageSizeParam, filtered.size());
        List<Ship> paged = IntStream.range(firstNumber, lastNumber).mapToObj(filtered::get).collect(Collectors.toList());

        if (order != null) {
            Comparator<Ship> shipComparator = null;

            switch (order) {
                case SPEED: {
                    shipComparator = Comparator.comparing(Ship::getSpeed);
                    break;
                }
                case RATING: {
                    shipComparator = Comparator.comparing(Ship::getRating);
                    break;
                }
                case ID: {
                    shipComparator = Comparator.comparing(Ship::getId);
                    break;
                }
                case DATE: {
                    shipComparator = Comparator.comparing(Ship::getProdDate);
                    break;
                }
            }
            paged.sort(shipComparator);
        }


        System.out.println(paged);
        //        ships.add(new Ship(1L,"one", "Jupiter", new Date().getTime()));
        //        ships.add(new Ship(2L,"two", "Venus", new Date().getTime()));
        return paged;

    }

    private List<Ship> getFilteredShips(@RequestParam(value = "name", required = false) String name,
                                        @RequestParam(value = "planet", required = false) String planet,
                                        @RequestParam(value = "shipType", required = false) ShipType shipType,
                                        @RequestParam(value = "after", required = false) Long after,
                                        @RequestParam(value = "before", required = false) Long before,
                                        @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                        @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                        @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                        @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                        @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                        @RequestParam(value = "minRating", required = false) Double minRating,
                                        @RequestParam(value = "maxRating", required = false) Double maxRating) {
        List<Ship> ships = new ArrayList<>();
        Iterable<Ship> all = repository.findAll();
        all.forEach(ships::add);

        return ships.stream().filter(t -> name == null || t.getName().contains(name))
                .filter((t -> planet == null || t.getPlanet().contains(planet)))
                .filter(t -> shipType == null || t.getShipType() == shipType)
                .filter(t -> after == null || t.getProdDate().getTime() > after)
                .filter(t -> before == null || t.getProdDate().getTime() < before)
                .filter(t -> isUsed == null || t.getUsed().equals(isUsed))
                .filter(t -> minRating == null || Double.compare(t.getRating(), minRating) >= 0)
                .filter(t -> maxRating == null || Double.compare(t.getRating(), maxRating) <= 0)
                .filter(t -> minSpeed == null || Double.compare(t.getSpeed(), minSpeed) >= 0)
                .filter(t -> maxSpeed == null || Double.compare(t.getSpeed(), maxSpeed) <= 0)
                .filter(t -> minCrewSize == null || t.getCrewSize() >= minCrewSize)
                .filter(t -> maxCrewSize == null || t.getCrewSize() <= maxCrewSize)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/rest/ships/count", method = RequestMethod.GET)
    long getShipCount(@RequestParam(value = "name", required = false) String name,
                      @RequestParam(value = "planet", required = false) String planet,
                      @RequestParam(value = "shipType", required = false) ShipType shipType,
                      @RequestParam(value = "after", required = false) Long after,
                      @RequestParam(value = "before", required = false) Long before,
                      @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                      @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                      @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                      @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                      @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                      @RequestParam(value = "minRating", required = false) Double minRating,
                      @RequestParam(value = "maxRating", required = false) Double maxRating) {
        return getFilteredShips(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating).size();
    }



}
