Feature: test


  Scenario: Posting pets
    Given Headers
      | accept       | application/json |
      | Content-Type | application/json |
    And Log filter with errorStatus
      | 400 |
      | 404 |
      | 503 |
    When Send get request 'https://petstore.swagger.io/v2/pet/findByStatus?status=available'
    Then Verify that the status code is 200
    Then Get '200' and '201' from scenario store and then compare, Are they not equals ?

  Scenario Outline: Post  a pets and check with get
    Given base url '<Pet Store Base Url>'
    And endpoint '/v2/pet'
    And payload as file 'payloads/petPost.json' from resource
    And relaxedHTTPSValidation
    And Headers
      | accept        | application/json |
      | Content-Type  | application/json |
      | Cache-Control | max-age=0        |
    And Log filter with errorStatus
      | 500 |
      | 400 |
      | 405 |
    When Send post requests
    Then Verify that the status code is 200
    Then Get response time as seconds and compare it, is it less then 200 ?
    And Get 'tags' from response and store it with 'tags' during scenario
    And Get 'id' from 'tags' json list which one equals 'name'='kuçukuçu', and store it during Scenario with 'tagId'
    And Get 'id' from response and store it with 'patId' during scenario
    Given Define new request
    And base url '<Pet Store Base Url>'
    And endpoint '/v2/pet/{id}'
    And Path parameter 'id' = 'patId'.
    When Send get request
    Then Verify that the status code is 200
    And Get 'id' from 'tags' json list which one equals 'name'='kuçukuçu', and store it during Scenario with 'tagId2'
    Then Get "tags" from the body then convert it to list and store it with "tag_list" during the scenario
    And Get "tag_list" list from response and then compare list count with 4, are they equals?


    Examples:
      | Pet Store Base Url          |
      | https://petstore.swagger.io |
