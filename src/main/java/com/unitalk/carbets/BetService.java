package com.unitalk.carbets;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BetService {

    private final List<CarBet> bets = List.of(
            new CarBet("Hummer", 0),
            new CarBet("Ferrari", 0),
            new CarBet("BMW", 0),
            new CarBet("Audi", 0),
            new CarBet("Honda", 0)
        );
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public String add(CarBet carBet) {
        String error = validate(carBet);
        if (error.isEmpty()) {
            String carName = CarBrand.get(carBet.getCar());
            lock.writeLock().lock();
            try {
                bets.stream().findFirst()(carName, carBet.getAmount(), Integer::sum);
            } finally {
                lock.writeLock().unlock();
            }
            return "A bet of " + carBet.getAmount() +
                    "$ has been added to the " + carName + " car bets. Current bet sum:" +
                    bets.get(carName) + "$";
        } else {
            return error;
        }
    }

    private String validate(CarBet carBet) {
        String error = "";
        if (carBet.getCar() == null || carBet.getCar().isEmpty()) {
            error += "Car is absent in the request. Please add a car to request body\n";
        } else if (bets.stream().anyMatch(e -> e.getCar().equalsIgnoreCase(carBet.getCar()))) {
            error += "Unfortunately there is no car: " + carBet.getCar() + " in our list.\n" +
                    "Please choose one of the following: " + Arrays.toString(CarBrand.values()) + "\n";
        }
        if (carBet.getAmount() == null) {
            error += "Bet amount is absent in the request. Please add a bet to request body\n";
        } else if (carBet.getAmount() <= 0) {
            error += "Bet amount value must be positive integer\n";
        }
        return error;
    }

    public Set<CarBet> get(String car) {
        if (car == null) {
            return getAll();
        } else if (CarBrand.contains(car)) {
            car = CarBrand.get(car);
            return getByCar(car);
        } else {
            return getAbsent(car);
        }
    }

    private Set<CarBet> getAll() {
        return bets.entrySet().stream()
                .map(entry -> new CarBet(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    private Set<CarBet> getByCar(String car) {
        return Set.of(new CarBet(car, bets.getOrDefault(car, 0)));
    }

    private Set<CarBet> getAbsent(String car) {
        return Set.of(new CarBet("Unfortunately there is no car: " + car + " in our list.\n" +
                "Please choose one of the following: " + Arrays.toString(CarBrand.values()) + "\n", null));
    }
}
