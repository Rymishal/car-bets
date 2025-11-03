package com.unitalk.carbets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

//@ExtendWith(MockitoExtension.class)
class BetServiceTest {

//    @Spy
    private final List<CarBet> bets = List.of(
            new CarBet("Hummer", 100),
            new CarBet("Ferrari", 0),
            new CarBet("BMW", 0),
            new CarBet("Audi", 0),
            new CarBet("Honda", 0)
    );;

    private final BetService betService = new BetService();

    @BeforeEach
    void setUp() throws Exception {
        Field field = BetService.class.getDeclaredField("bets");
        field.setAccessible(true);
        field.set(betService, bets);
        field.setAccessible(false);
    }

    @Test
    public void shouldCreateNewBet() {
        CarBet bet = new CarBet("Audi", 1000);
        String result = betService.add(bet);
        assertEquals("A bet of 1000$ has been added to the Audi car bets. Current bet sum:1000$",
                result);
    }

    @Test
    public void shouldCreateNewBetWithCaseMistaken() {
        CarBet bet = new CarBet("AUDI", 1000);
        String result = betService.add(bet);
        assertEquals("A bet of 1000$ has been added to the Audi car bets. Current bet sum:1000$",
                result);
    }

    @Test
    public void shouldReturnNullCarError() {
        CarBet bet = new CarBet(null, 1000);
        String result = betService.add(bet);
        assertEquals("Car is absent in the request. Please add a car to request body\n",
                result);
    }

    @Test
    public void shouldReturnNotInListCarError() {
        CarBet bet = new CarBet("Bentley", 1000);
        String result = betService.add(bet);
        assertEquals("Unfortunately there is no car: " + bet.getCar() + " in our list.\n" +
                        "Please choose one of the following: " + Arrays.toString(CarBrand.values()) + "\n",
                result);
    }

    @Test
    public void shouldReturnNullBetError() {
        CarBet bet = new CarBet("Hummer", null);
        String result = betService.add(bet);
        assertEquals("Bet amount is absent in the request. Please add a bet to request body\n",
                result);
    }

    @Test
    public void shouldReturnNotPositiveBetError() {
        CarBet bet = new CarBet("Hummer", 0);
        String result = betService.add(bet);
        assertEquals("Bet amount value must be positive integer\n",
                result);
    }

    @Test
    public void shouldGetBet() {
        List<CarBet> result = betService.get("Hummer");

        assertEquals(1, result.size());
        for (CarBet bet : result) {
            assertEquals("Hummer", bet.getCar());
            assertEquals(100, bet.getAmount());
        }
    }

    @Test
    public void shouldGetWithCaseMistaken() {
        List<CarBet> result = betService.get("HUMMER");

        assertEquals(1, result.size());
        for (CarBet bet : result) {
            assertEquals("Hummer", bet.getCar());
            assertEquals(100, bet.getAmount());
        }
    }

    @Test
    public void shouldReturnNoCarError() {
        List<CarBet> result = betService.get("Bentley");

        assertEquals(1, result.size());
        for (CarBet bet : result) {
            assertEquals("Unfortunately there is no car: Bentley in our list.\n" +
                    "Please choose one of the following: " + Arrays.toString(CarBrand.values()) + "\n", bet.getCar());
            assertNull(bet.getAmount());
        }
    }

    @Test
    public void shouldGetAllBets() {
        List<CarBet> result = betService.get(null);

        List<CarBet> expected = List.of(
                new CarBet("Hummer", 100),
                new CarBet("Ferrari", 0),
                new CarBet("BMW", 0),
                new CarBet("Audi", 0),
                new CarBet("Honda", 0)
        );

        assertEquals(5, result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getCar(), result.get(i).getCar());
            assertEquals(expected.get(i).getAmount(), result.get(i).getAmount());
        }
    }
}