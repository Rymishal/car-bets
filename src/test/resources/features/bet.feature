Feature: Bet

  Scenario: Multiple betting
    Given no bets exists
    When user creates bet of 100$ for car: "Audi"
    And user creates bet of 100$ for car: "AudI"
    And user creates bet of 100$ for car: "Hummer"
    And user creates bet of 1000$ for car: "Hummer"
    And user creates bet of 1000$ for car: "BMW"
    And user creates bet of -1$ for car: "Hummer"
    And user creates bet of 100$ for car: ""
    And user creates bet of 100$ for car: "Bentley"
    And user retrieves bet for car: "Hummer"
    And user retrieves bet for car: "Bentley"
    And user retrieves all bets
    Then creation results should be got
      | A bet of 100$ has been added to the Audi car bets. Current bet sum:100$                                                        |
      | A bet of 100$ has been added to the Audi car bets. Current bet sum:200$                                                        |
      | A bet of 100$ has been added to the Hummer car bets. Current bet sum:100$                                                      |
      | A bet of 1000$ has been added to the Hummer car bets. Current bet sum:1100$                                                    |
      | A bet of 1000$ has been added to the BMW car bets. Current bet sum:1000$                                                       |
      | Bet amount value must be positive integer\n                                                                                    |
      | Car is absent in the request. Please add a car to request body\n                                                               |
      | Unfortunately there is no car: Bentley in our list.\nPlease choose one of the following: [Hummer, Ferrari, BMW, Audi, Honda]\n |
    And should be got by names
      | car                                                                                                                            | bet  |
      | Hummer                                                                                                                         | 1100 |
      | Unfortunately there is no car: Bentley in our list.\nPlease choose one of the following: [Hummer, Ferrari, BMW, Audi, Honda]\n |      |
    And all bets should be got
      | car     | bet  |
      | Hummer  | 1100 |
      | Ferrari | 0    |
      | BMW     | 1000 |
      | Audi    | 200  |
      | Honda   | 0    |
