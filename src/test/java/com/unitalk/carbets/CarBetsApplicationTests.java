package com.unitalk.carbets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CarBetsApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private BetService betService;

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

        List<CarBet> result = betService.get(null);
        assertEquals(5, result.size());
        for (CarBet bet : result) {
            assertEquals(expectedBetsPerCar, bet.getAmount());
        }
    }
}
