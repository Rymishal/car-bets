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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BetServiceTest {

    @Spy
    private Map<String, Integer> bets = new HashMap<>();

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
        CarBet bet = new CarBet("Hummer", 1000);
        String result = betService.add(bet);
        assertEquals("A bet of 1000$ has been added to the Hummer car bets. Current bet sum:1000$",
                result);
    }

    @Test
    public void shouldCreateNewBetWithCaseMistaken() {
        CarBet bet = new CarBet("hUMMER", 1000);
        String result = betService.add(bet);
        assertEquals("A bet of 1000$ has been added to the Hummer car bets. Current bet sum:1000$",
                result);
    }

    @Test
    public void shouldAddBetToExistingCar() {
        List<CarBet> bets = List.of(new CarBet("Hummer", 1000),
                new CarBet("Hummer", 1200));
        int totalBet = 0;
        String result = "";
        for (CarBet bet : bets) {
            totalBet += bet.amount();
            result = betService.add(bet);
        }
        assertEquals("A bet of " + bets.getLast().amount() + "$ has been added to the Hummer car bets. Current bet sum:"
                        + totalBet + "$",
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
        assertEquals("Unfortunately there is no car: " + bet.car() + " in our list.\n" +
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
        when(bets.getOrDefault(eq("Hummer"), eq(0))).thenReturn(100);
        Set<CarBet> result = betService.get("Hummer");

        assertEquals(1, result.size());
        for (CarBet bet : result) {
            assertEquals("Hummer", bet.car());
            assertEquals(100, bet.amount());
        }
    }

    @Test
    public void shouldGetWithCaseMistaken() {
        when(bets.getOrDefault(eq("Audi"), eq(0))).thenReturn(100);
        Set<CarBet> result = betService.get("AUDI");

        assertEquals(1, result.size());
        for (CarBet bet : result) {
            assertEquals("Audi", bet.car());
            assertEquals(100, bet.amount());
        }
    }

    @Test
    public void shouldReturnDefaultValue() {
        Set<CarBet> result = betService.get("Hummer");

        assertEquals(1, result.size());
        for (CarBet bet : result) {
            assertEquals("Hummer", bet.car());
            assertEquals(0, bet.amount());
        }
    }

    @Test
    public void shouldReturnError() {
        Set<CarBet> result = betService.get("Bentley");

        assertEquals(1, result.size());
        for (CarBet bet : result) {
            assertEquals("Unfortunately there is no car: Bentley in our list.\n" +
                    "Please choose one of the following: " + Arrays.toString(CarBrand.values()) + "\n", bet.car());
            assertNull(bet.amount());
        }
    }

    @Test
    public void shouldGetAllBets() {
        Map<String, Integer> resultMap = Map.of("Hummer", 1000,
                "Honda", 100, "BMW", 200);
        when(bets.entrySet()).thenReturn(resultMap.entrySet());
        Set<CarBet> result = betService.get(null);

        Set<CarBet> expected = Set.of(new CarBet("BMW", 200),
                new CarBet("Honda", 100),
                new CarBet("Hummer", 1000));

        assertEquals(3, result.size());
        for (CarBet bet : expected) {
            assertTrue(result.stream()
                    .anyMatch(b -> b.car().equals(bet.car())
                            && b.amount().equals(bet.amount())), () -> "Expected bet " + bet.amount() + " for car " + bet.car() +
                    " But result set contains: \n" + result);
        }
    }

    @Test
    public void shouldAddBetsConcurrently() throws InterruptedException {
        int threads = 50;
        int betsPerThread = 1000;
        int expectedBetsPerCar = betsPerThread * threads / CarBrand.values().length;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; ++i) {
            String car = CarBrand.values()[i % CarBrand.values().length].name();
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < betsPerThread; ++j) {
                        betService.add(new CarBet(car, 1));
                    }
                } catch (InterruptedException ignored) {

                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        doneLatch.await();
        executor.shutdown();

        Set<CarBet> result = betService.get(null);
        assertEquals(5, result.size());
        for (CarBet bet : result) {
            assertEquals(expectedBetsPerCar, bet.amount());
        }
    }
}