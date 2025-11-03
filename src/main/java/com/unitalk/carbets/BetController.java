package com.unitalk.carbets;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/bet")
@RequiredArgsConstructor
public class BetController {

    private final BetService betService;

    @PostMapping
    public String add(@RequestBody CarBet carBet) {
        return betService.add(carBet);
    }

    @GetMapping(value = {"/{car}", ""})
    public List<CarBet> get(@PathVariable(required = false) String car) {
        return betService.get(car);
    }
}
