package com.unitalk.carbets;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BetService {

    private final Map<String, Integer> bets = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public String add(CarBet carBet) {
        String error = validate(carBet);
        if (error.isEmpty()) {
            String carName = CarBrand.get(carBet.car());
            lock.writeLock().lock();
            try {
                bets.merge(carName, carBet.amount(), Integer::sum);
            } finally {
                lock.writeLock().unlock();
            }
            return "A bet of " + carBet.amount() +
                    "$ has been added to the " + carName + " car bets. Current bet sum:" +
                    bets.get(carName) + "$";
        } else {
            return error;
        }
    }

    private String validate(CarBet carBet) {
        String error = "";
        if (carBet.car() == null || carBet.car().isEmpty()) {
            error += "Car is absent in the request. Please add a car to request body\n";
        } else if (!CarBrand.contains(carBet.car())) {
            error += "Unfortunately there is no car: " + carBet.car() + " in our list.\n" +
                    "Please choose one of the following: " + Arrays.toString(CarBrand.values()) + "\n";
        }
        if (carBet.amount() == null) {
            error += "Bet amount is absent in the request. Please add a bet to request body\n";
        } else if (carBet.amount() <= 0) {
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
