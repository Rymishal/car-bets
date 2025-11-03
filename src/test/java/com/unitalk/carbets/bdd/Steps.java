package com.unitalk.carbets.bdd;

import com.unitalk.carbets.BetService;
import com.unitalk.carbets.CarBet;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Steps {

    @Autowired
    private BetService betService;

    @Autowired
    private TestRestTemplate restTemplate;

    private final String url = "/api/bet";

    private final List<String> addResponses = new ArrayList<>();
    private final List<Set<CarBet>> getByCarResponses = new ArrayList<>();
    private Set<CarBet> getAllResponse;

    @Given("bets exists")
    public void betsExist(DataTable dataTable) throws NoSuchFieldException, IllegalAccessException {
        List<List<String>> lists = dataTable.asLists();
        Map<String, Integer> bets = new HashMap<>();
        for (int i = 1; i < lists.size(); i++) {
            List<String> list = lists.get(i);
            bets.put(list.getFirst(), Integer.valueOf(list.getLast()));
        }

        Field field = BetService.class.getDeclaredField("bets");
        field.setAccessible(true);
        field.set(betService, bets);
        field.setAccessible(false);
    }

    @When("user creates bet of {int}$ for car: {string}")
    public void addBetForCar(int amount, String car) {
        CarBet bet = new CarBet(car, amount);
        HttpEntity<CarBet> httpEntity = new HttpEntity<>(bet);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        addResponses.add(responseEntity.getBody());
    }

    @When("user retrieves bet for car: {string}")
    public void retrieveBetForCar(String car) {
        ResponseEntity<Set<CarBet>> responseEntity = restTemplate.exchange(url + "/" + car, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });
        getByCarResponses.add(responseEntity.getBody());
    }

    @When("user retrieves all bets")
    public void retrieveAll() {
        ResponseEntity<Set<CarBet>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });
        getAllResponse = responseEntity.getBody();
    }

    @Then("creation results should be got")
    public void createdResultsShouldBeGot(List<String> expectedResponses) {
        assertEquals(expectedResponses.size(), addResponses.size());
        for (int i = 0; i < expectedResponses.size(); i++) {
            assertTrue(addResponses.get(i).contains(expectedResponses.get(i)));
        }
    }

    @Then("should be got by names")
    public void shouldBeGotByNames(DataTable dataTable) {
        List<List<String>> lists = dataTable.asLists();
        List<CarBet> expectedBets = new ArrayList<>();
        for (int i = 1; i < lists.size(); i++) {
            List<String> list = lists.get(i);
            String amount = list.getLast();
            expectedBets.add(new CarBet(list.getFirst(),
                    amount != null ? Integer.valueOf(amount) : null));
        }

        assertEquals(expectedBets.size(), getByCarResponses.size());

        for (int i = 0; i < expectedBets.size(); ++i) {
            int finalI = i;
            assertTrue(getByCarResponses.get(i).stream()
                            .anyMatch(resp -> resp.getCar().equals(expectedBets.get(finalI).getCar())
                                    && (resp.getAmount() == null && expectedBets.get(finalI).getAmount() == null
                                    || resp.getAmount().equals(expectedBets.get(finalI).getAmount()))),
                    () -> "Expected bet " + expectedBets.get(finalI).getAmount() + " for car " + expectedBets.get(finalI).getCar() +
                            " But result set contains: \n" + getByCarResponses.get(finalI));
        }
    }

    @Then("all bets should be got")
    public void allBetsShouldBeGot(DataTable dataTable) {
        List<List<String>> lists = dataTable.asLists();
        List<CarBet> expectedBets = new ArrayList<>();
        for (int i = 1; i < lists.size(); i++) {
            List<String> list = lists.get(i);
            String amount = list.getLast();
            expectedBets.add(new CarBet(list.getFirst(),
                    amount != null ? Integer.valueOf(amount) : null));
        }

        assertEquals(expectedBets.size(), getAllResponse.size());

        for (CarBet bet : expectedBets) {
            assertTrue(getAllResponse.stream()
                            .anyMatch(resp -> resp.getCar().equals(bet.getCar())
                                    && (resp.getAmount().equals(bet.getAmount()))),
                    () -> "Expected bet " + bet.getAmount() + " for car " + bet.getCar() +
                            " But result set contains: \n" + getAllResponse);
        }
    }
}
