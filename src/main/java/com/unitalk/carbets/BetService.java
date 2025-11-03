package com.unitalk.carbets;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
            CarBet bet;
            String carName;
            lock.writeLock().lock();
            try {
                bet = bets.stream().filter(e -> e.getCar().equalsIgnoreCase(carBet.getCar())).findFirst().get();
                carName = bet.getCar();
                bet.setAmount(bet.getAmount() + carBet.getAmount());
            } finally {
                lock.writeLock().unlock();
            }
            return "A bet of " + carBet.getAmount() +
                    "$ has been added to the " + carName + " car bets. Current bet sum:" +
                    bet.getAmount() + "$";
        } else {
            return error;
        }
    }

    private String validate(CarBet carBet) {
        String error = "";
        if (carBet.getCar() == null || carBet.getCar().isEmpty()) {
            error += "Car is absent in the request. Please add a car to request body\n";
        } else if (bets.stream().noneMatch(e -> e.getCar().equalsIgnoreCase(carBet.getCar()))) {
            error += "Unfortunately there is no car: " + carBet.getCar() + " in our list.\n" +
                    "Please choose one of the following: " + Arrays.toString(bets.stream().map(CarBet::getCar).toArray()) + "\n";
        }
        if (carBet.getAmount() == null) {
            error += "Bet amount is absent in the request. Please add a bet to request body\n";
        } else if (carBet.getAmount() <= 0) {
            error += "Bet amount value must be positive integer\n";
        }
        return error;
    }

    public List<CarBet> get(String car) {
        if (car == null) {
            return bets;
        } else {
            return getByCar(car);
        }
    }

    private List<CarBet> getByCar(String car) {
        List<CarBet> result = new ArrayList<>(bets.stream().filter(e -> e.getCar().equalsIgnoreCase(car)).toList());
        if (result.isEmpty()) {
            result.add(new CarBet("Unfortunately there is no car: " + car + " in our list.\n" +
                    "Please choose one of the following: " + Arrays.toString(bets.stream().map(CarBet::getCar).toArray()) + "\n",
                    null));
        }
        return result;
    }
}
