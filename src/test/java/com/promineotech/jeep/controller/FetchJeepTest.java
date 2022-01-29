package com.promineotech.jeep.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doThrow;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import com.promineotech.jeep.Constants;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;
import com.promineotech.jeep.service.JeepSalesService;
import lombok.Getter;


class FetchJeepTest {
  
  @LocalServerPort
  private int serverPort;
  
  @Autowired
  @Getter
  private TestRestTemplate restTemplate;
  
  protected String getBaseUriForOrders(){
    return String.format("http://localhost:%d/orders", serverPort);
  }
  
  @Nested
  @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
  @ActiveProfiles("test")
  @Sql(scripts = {"classpath:flyway/migrations/V1.0__Jeep_Schema.sql", 
      "classpath:flyway/migrations/V1.1__Jeep_Data.sql"}, config = @SqlConfig(encoding="UTF-8"))
  class TestsThatDoNotPolluteTheApplicationContext{
       
    @LocalServerPort
    private int serverPort;
    
    @Autowired
    @Getter
    private TestRestTemplate restTemplate;
    
    @Test
    void testThatJeepsAreReturnedWhenAValidModelAndTrimAreSupplied() {
     JeepModel model = JeepModel.WRANGLER;
     String trim = "Sport";
     String uri = String.format("http://localhost:%d/jeeps?model=%s&trim=%s",
         serverPort, model, trim);
    // System.out.println(uri);
     
     // From the video
      //ResponseEntity<Jeep> response = getRestTemplate().getForEntity(uri, Jeep.class);
     
     // From the homework
    ResponseEntity<List<Jeep>> response = restTemplate.exchange(uri, HttpMethod.GET, null, 
         new ParameterizedTypeReference<>() {});
     
     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
     
     List<Jeep> actual = response.getBody();
     List<Jeep> expected = buildExpected();
     
     assertThat(actual).isEqualTo(expected);
    }
    
    @Test
    void testThatAnErrorMessageIsReturnedWhenAnUnknownTrimIsSupplied() {
     JeepModel model = JeepModel.WRANGLER;
     String trim = "Unknown Value";
     String uri = String.format("http://localhost:%d/jeeps?model=%s&trim=%s",
         serverPort, model, trim);
    // System.out.println(uri);
     
     // From the video
      //ResponseEntity<Jeep> response = getRestTemplate().getForEntity(uri, Jeep.class);
     
     // From the homework
    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(uri, HttpMethod.GET, null, 
         new ParameterizedTypeReference<>() {});
     
     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
     
     Map<String, Object> error = response.getBody();
     
     assertErrorMessageValid(error, HttpStatus.NOT_FOUND);
    }
    
    @ParameterizedTest
    @MethodSource("com.promineotech.jeep.controller.FetchJeepTest#parametersForInvalidInput")
    void testThatAnErrorMessageIsReturnedWhenAnInvalidValueIsSupplied(String model, String trim, String reason) {
      String uri = String.format("http://localhost:%d/jeeps?model=%s&trim=%s",
          serverPort, model, trim);
     // System.out.println(uri);
      
      // From the video
       //ResponseEntity<Jeep> response = getRestTemplate().getForEntity(uri, Jeep.class);
      
      // From the homework
     ResponseEntity<Map<String, Object>> response = restTemplate.exchange(uri, HttpMethod.GET, null, 
          new ParameterizedTypeReference<>() {});
      
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      
      Map<String, Object> error = response.getBody();
      
      assertErrorMessageValid(error, HttpStatus.BAD_REQUEST);
     }
  }
  
  static Stream<Arguments> parametersForInvalidInput(){
    
    return Stream.of(
        arguments("WRANGLER", "d2@e^s", "Trim contains non-alpha-numeric characters"),
        arguments("WRANGLER", "C".repeat(Constants.TRIM_MAX_LENGTH + 1), "Trim length exceeds maximum"),
        arguments("CHALLENGER", "Sport", "Invalid model")
        );
  }

  protected void assertErrorMessageValid(Map<String, Object> error, HttpStatus status) {
    assertThat(error).containsKey("message")
      .containsEntry("status code", status.value())
      .containsEntry("uri", "/jeeps")
      .containsKey("timestamp")
      .containsEntry("reason", status.getReasonPhrase());
  }

  protected List<Jeep> buildExpected() {
    List<Jeep> list = new LinkedList<>();
    
    list.add(Jeep.builder().modelID(JeepModel.WRANGLER).trimLevel("Sport").numDoors(2).wheelSize(17).basePrice(new BigDecimal("28475.00")).build());
    list.add(Jeep.builder().modelID(JeepModel.WRANGLER).trimLevel("Sport").numDoors(4).wheelSize(17).basePrice(new BigDecimal("31975.00")).build());
   
    Collections.sort(list);
    return list;
  }

  
  @Nested
  @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
  @ActiveProfiles("test")
  @Sql(scripts = {"classpath:flyway/migrations/V1.0__Jeep_Schema.sql", 
      "classpath:flyway/migrations/V1.1__Jeep_Data.sql"}, config = @SqlConfig(encoding="UTF-8"))
  class TestsThatPolluteTheApplicationContext{
    
    @LocalServerPort
    private int serverPort;
    
    @Autowired
    @Getter
    private TestRestTemplate restTemplate;
    
    @MockBean
    private JeepSalesService jeepSalesService;
    @Test
    void testThatAnUnplannedErrorResultsInA500Status() {
     JeepModel model = JeepModel.WRANGLER;
     String trim = "Invalid";
     String uri = String.format("http://localhost:%d/jeeps?model=%s&trim=%s",
         serverPort, model, trim);
     
     doThrow(new RuntimeException("D'oh!")).when(jeepSalesService).fetchJeeps(model, trim);
    // System.out.println(uri);
     
     // From the video
      //ResponseEntity<Jeep> response = getRestTemplate().getForEntity(uri, Jeep.class);
     
     // From the homework
    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(uri, HttpMethod.GET, null, 
         new ParameterizedTypeReference<>() {});
     
     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
     
     Map<String, Object> error = response.getBody();
     
     assertErrorMessageValid(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }  
}
