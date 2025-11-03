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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Steps {

    @Autowired
    private BetService betService;

    @Autowired
    private TestRestTemplate restTemplate;

    private final String url = "/api/bet";

    private final List<String> addResponses = new ArrayList<>();
    private final List<List<CarBet>> getByCarResponses = new ArrayList<>();
    private List<CarBet> getAllResponse;

    @Given("no bets exists")
    public void noBetsExist() throws NoSuchFieldException, IllegalAccessException {
        List<CarBet> bets = List.of(
                new CarBet("Hummer", 0),
                new CarBet("Ferrari", 0),
                new CarBet("BMW", 0),
                new CarBet("Audi", 0),
                new CarBet("Honda", 0)
        );

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
        ResponseEntity<List<CarBet>> responseEntity = restTemplate.exchange(url + "/" + car, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });
        getByCarResponses.add(responseEntity.getBody());
    }

    @When("user retrieves all bets")
    public void retrieveAll() {
        ResponseEntity<List<CarBet>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });
        getAllResponse = responseEntity.getBody();
    }

    @Then("creation results should be got")
    public void createdResultsShouldBeGot(List<String> expectedResponses) {
        assertEquals(expectedResponses.size(), addResponses.size());
        for (int i = 0; i < expectedResponses.size(); i++) {
            System.out.println(addResponses.get(i));
            assertEquals(expectedResponses.get(i), addResponses.get(i));
        }
    }

    @Then("should be got by names")
    public void shouldBeGotByNames(DataTable dataTable) {
        List<List<String>> lists = dataTable.asLists();
        List<CarBet> expectedBets = new ArrayList<>();
        for (int i = 1; i < lists.size(); ++i) {
            String amount = lists.get(i).getLast();
            expectedBets.add(new CarBet(lists.get(i).getFirst(), amount != null ? Integer.valueOf(amount) : null));
        }

        assertEquals(expectedBets.size(), getByCarResponses.size());

        for (int i = 0; i < expectedBets.size(); i++) {
            System.out.println(getByCarResponses.get(i));
            assertEquals(expectedBets.get(i).getCar(), getByCarResponses.get(i).getFirst().getCar());
            assertEquals(expectedBets.get(i).getAmount(), getByCarResponses.get(i).getFirst().getAmount());
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

        for (int i = 0; i < expectedBets.size(); i++) {
            assertEquals(expectedBets.get(i).getCar(), getAllResponse.get(i).getCar());
            assertEquals(expectedBets.get(i).getAmount(), getAllResponse.get(i).getAmount());
        }
    }
}
