package com.unitalk.carbets;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BetService {

    private final Map<String, Integer> bets = new ConcurrentHashMap<>();

    public String add(CarBet carBet) {
        if(carBet.car() == null) {
            return "Car is absent. Please add a car to request body";
        } else if (carBet.bet() == null) {
            return "Bet is absent. Please add a bet to request body";
        } else if (carBet.bet() <= 0) {
            return "Bet value must be positive integer";
        } else {
            bets.merge(carBet.car(), carBet.bet(), Integer::sum);
            return "A bet of " + carBet.bet() +
                    "$ has been added to the " + carBet.car() + " car bets. Current bet sum:" +
                    bets.get(carBet.car()) + "$";
        }
    }

    public Set<CarBet> get(String car) {
        if(car == null) {
            return getAll();
        } else if (bets.containsKey(car)) {
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
        return Set.of(new CarBet(car, bets.get(car)));
    }

    private Set<CarBet> getAbsent(String car) {
        return Set.of(new CarBet("There is no car: " + car, null));
    }
}
