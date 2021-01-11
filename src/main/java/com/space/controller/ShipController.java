package com.space.controller;

import com.space.model.Ship;
import com.space.repository.ShipRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

@RestController
public class ShipController {
    private final
    ShipRepository repository;

    public ShipController(ShipRepository shipRepository) {
        this.repository = shipRepository;
    }

    @RequestMapping(value = "/rest/ships", method = RequestMethod.POST)
    public Ship createShip(@RequestBody Ship ship) {
        if (shipIsInvalid(ship)) {
            throw new InvalidRequestException();
        }
        if (ship.getUsed() == null) {
            ship.setUsed(false);
        }
        ship.setRating(calculateRating(ship));
        return repository.save(ship);
    }

    @RequestMapping(value = "/rest/ships/{id}", method = RequestMethod.POST)
    public Ship updateShip(@PathVariable(value = "id") String pathId, @RequestBody Ship update) {

        Ship shipToSave = getShip(pathId);
        updateNonNullParameters(shipToSave, update);

        if (shipIsInvalid(shipToSave)) {
            throw new InvalidRequestException();
        }
        shipToSave.setRating(calculateRating(shipToSave));
        return repository.save(shipToSave);
    }

    @RequestMapping(value = "/rest/ships/{id}", method = RequestMethod.DELETE)
    public void deleteShip(@PathVariable(value = "id") String pathId) {

        Ship shipToDelete = getShip(pathId);
        repository.delete(shipToDelete);
    }

    @RequestMapping(value = "/rest/ships/{id}", method = RequestMethod.GET)
    public Ship getShip(@PathVariable(value = "id") String pathId) {
        if (idIsInvalid(pathId)) {
            throw new InvalidRequestException();
        }
        Long id = Long.parseLong(pathId);
        return repository.findById(id).orElseThrow(ShipNotFoundException::new);
    }

    private double calculateRating(Ship ship) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ship.getProdDate().getTime());
        int prodYear = calendar.get(Calendar.YEAR);
        return BigDecimal.valueOf(80).multiply(BigDecimal.valueOf(ship.getSpeed())).multiply(BigDecimal.valueOf(ship.getUsed() ? 0.5 : 1))
                .divide(BigDecimal.valueOf(3019 - prodYear + 1), 3, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
    }



    private void updateNonNullParameters(Ship existing, Ship update) {
        if (update.getName() != null) {
            existing.setName(update.getName());
        }


        if (update.getPlanet() != null) {
            existing.setPlanet(update.getPlanet());
        }
        if (update.getShipType() != null) {
            existing.setShipType(update.getShipType());
        }
        if (update.getProdDate() != null) {
            existing.setProdDate(update.getProdDate());
        }

        if (update.getSpeed() != null) {
            existing.setSpeed(update.getSpeed());
        }

        if (update.getCrewSize() != null) {
            existing.setCrewSize(update.getCrewSize());
        }
        if (update.getUsed() != null) {
            existing.setUsed(update.getUsed());
        }
    }
    private boolean idIsInvalid(String requestId) {
        long id;
        try {
            id = Long.parseLong(requestId);
        } catch (NumberFormatException e) {
            return true;
        }
        return id <= 0;
    }

    private boolean shipIsInvalid(Ship ship) {
        if (ship.getName() == null || ship.getPlanet() == null || ship.getShipType() == null || ship.getProdDate() == null || ship.getSpeed() == null || ship.getCrewSize() == null) {
            return true;
        }
        if (ship.getName().length() > 50 || ship.getPlanet().length() > 50) {
            return true;
        }
        if (ship.getName().trim().length() == 0 || ship.getPlanet().trim().length() == 0) {
            return true;
        }
        BigDecimal decimal = BigDecimal.valueOf(ship.getSpeed()).setScale(2, RoundingMode.HALF_UP);
        if (decimal.compareTo(BigDecimal.ONE) >= 1 || decimal.compareTo(new BigDecimal("0.01")) < 0) {
            return true;
        }
        if (ship.getCrewSize() < 1 || ship.getCrewSize() > 9999) {
            return true;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ship.getProdDate().getTime());
        int prodYear = calendar.get(Calendar.YEAR);
        return prodYear < 2800 || prodYear > 3019;

    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private class InvalidRequestException extends RuntimeException {
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private class ShipNotFoundException extends RuntimeException {
    }
}
