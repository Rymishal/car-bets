package com.unitalk.carbets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BetController.class)
class BetControllerTest {

    @MockitoBean
    private BetService betService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String url = "http://localhost:9090/api/bet";

    @Test
    public void shouldCreateNewBet() throws Exception {
        CarBet bet = new CarBet("Hummer", 1000);
        when(betService.add(eq(bet)))
                .thenReturn("A bet of 1000$ has been added to the Hummer car bets. Current bet sum:1000$");
        String response = mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bet))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals("A bet of 1000$ has been added to the Hummer car bets. Current bet sum:1000$",
                response);
    }

    @Test
    public void shouldReturnErrorMessagesWithOkStatus() throws Exception {
        CarBet bet = new CarBet(null, null);
        when(betService.add(eq(bet)))
                .thenReturn("""
                        Car is absent in the request. Please add a car to request body
                        Bet amount is absent in the request. Please add a bet to request body
                        """);
        String response = mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bet))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals("""
                        Car is absent in the request. Please add a car to request body
                        Bet amount is absent in the request. Please add a bet to request body
                        """,
                response);
    }

    @Test
    public void shouldGetBet() throws Exception {
        CarBet bet = new CarBet("Audi", 100);
        when(betService.get(eq("Audi")))
                .thenReturn(Set.of(bet));
        Set<CarBet> result = objectMapper.readValue(mockMvc.perform(MockMvcRequestBuilders.get(url + "/Audi"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), new TypeReference<>() {
        });
        assertEquals(1, result.size());
        for (CarBet resultBet : result) {
            assertEquals("Audi", resultBet.car());
            assertEquals(100, resultBet.amount());
        }
    }

    @Test
    public void shouldGetAllBets() throws Exception {
        Set<CarBet> bets = Set.of(new CarBet("Audi", 100),
                new CarBet("BMW", 200),
                new CarBet("Hummer", 300));
        when(betService.get(null))
                .thenReturn(bets);
        Set<CarBet> result = objectMapper.readValue(mockMvc.perform(MockMvcRequestBuilders.get(url)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), new TypeReference<>() {
        });

        Set<CarBet> expected = Set.of(new CarBet("Audi", 100),
                new CarBet("BMW", 200),
                new CarBet("Hummer", 300));

        assertEquals(3, result.size());
        for (CarBet bet : result) {
            assertTrue(expected.stream()
                    .anyMatch(b -> b.car().equals(bet.car())
                            && b.amount().equals(bet.amount())));
        }
    }

}