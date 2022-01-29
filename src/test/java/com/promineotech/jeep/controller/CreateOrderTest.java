package com.promineotech.jeep.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.promineotech.jeep.entity.JeepModel;
import com.promineotech.jeep.entity.Order;
import org.apache.catalina.loader.ResourceEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.jdbc.JdbcTestUtils;
import com.promineotech.jeep.controller.support.CreateOrderTeestSupport;
import com.promineotech.jeep.entity.OrderRequest;
import lombok.Getter;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = {"classpath:flyway/migrations/V1.0__Jeep_Schema.sql", 
    "classpath:flyway/migrations/V1.1__Jeep_Data.sql"}, config = @SqlConfig(encoding="UTF-8"))
public class CreateOrderTest extends CreateOrderTeestSupport {
  
    @LocalServerPort
    private int serverPort;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    @Getter
    private TestRestTemplate restTemplate;
    
    protected String getBaseUriForOrders() {
      return String.format("http://localhost:%d/orders", serverPort);
    }
    
    private String createOrderBody() {
     return "{\n" + "  \"customer\":\"MORISON_LINA\",\n" + "  \"model\":\"WRANGLER\",\n"
          + "  \"trim\":\"Sport Altitude\",\n" + "  \"doors\":4,\n"
          + "  \"color\":\"EXT_NACHO\",\n" + "  \"engine\":\"2_0_TURBO\",\n"
          + "  \"tire\":\"35_TOYO\",\n" + "  \"options\":[\n" + "    \"DOOR_QUAD_4\",\n"
          + "    \"EXT_AEV_LIFT\",\n" + "    \"EXT_WARN_WINCH\",\n"
          + "    \"EXT_WARN_BUMPER_FRONT\",\n" + "    \"EXT_WARN_BUMPER_REAR\",\n"
          + "    \"EXT_ARB_COMPRESSOR\"\n" + "  ]\n" + "}";
          //JSON goes in the return, use a converter/validator for it
    }
  
  @Test
  void testCreateOrderReturnsSuccess201(){
    String body = createOrderBody();
    String uri = getBaseUriForOrders();
    
    int numRowsOrders = JdbcTestUtils.countRowsInTable(jdbcTemplate, "orders");
    int numRowsOptions = JdbcTestUtils.countRowsInTable(jdbcTemplate, "order_options");
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    
    HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers);
    ResponseEntity<Order> response = restTemplate.exchange(uri, HttpMethod.POST, httpEntity, Order.class);
    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    
    Order order = response.getBody();
    assertThat(order.getCustomer().getCustomerId()).isEqualTo("MORISON_LINA");
    assertThat(order.getModel().getModelID()).isEqualTo(JeepModel.WRANGLER);
    assertThat(order.getModel().getTrimLevel()).isEqualTo("Sport Altitude");
    assertThat(order.getModel().getNumDoors()).isEqualTo(4);
    assertThat(order.getColor().getColorId()).isEqualTo("EXT_NACHO");
    assertThat(order.getEngine().getEngineId()).isEqualTo("2_0_TURBO");
    assertThat(order.getTire().getTireId()).isEqualTo("35_TOYO");
    assertThat(order.getOptions()).hasSize(6);
    
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "orders")).isEqualTo(numRowsOrders + 1);
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "order_options")).isEqualTo(numRowsOptions + 6);
  }
}

