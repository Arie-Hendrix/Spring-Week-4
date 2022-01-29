package com.promineotech.jeep.controller;

import java.util.List;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.promineotech.jeep.Constants;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;

@Validated
@RequestMapping("/jeeps")
// Possibly needed?
@OpenAPIDefinition(info = @Info(title = "Jeep Sales Service"), 
servers = {@Server(url = "http://localhost:8080", description = "Local Server")})
public interface JeepSalesController {

  // @formatter:off
  @Operation(
      summary = "Returns the Jeep list",
      description = "Returns the list with specified model and trim",
      responses = {
          @ApiResponse(responseCode = "200", 
              description = "The Jeep list was returned sucessfully", 
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = Jeep.class))),
          @ApiResponse(responseCode = "400", 
              description = "The request parameters are invalid", 
               content = @Content(mediaType = "application/json")),
          @ApiResponse(responseCode = "404", 
              description = "No Jeeps found with input criteria", 
              content = @Content(mediaType = "application/json")),
          @ApiResponse(responseCode = "500", 
              description = "An unexpected error has occurred",
              content = @Content(mediaType = "application/json"))
      },
      parameters = {
          @Parameter(name = "model", allowEmptyValue = false,
              required = false, description = "Name of the Jeep model"),
          @Parameter(name = "trim", allowEmptyValue = false,
              required = false, description = "Trim package of the Jeep")
      }
     )
 
  @GetMapping
  @ResponseStatus(code = HttpStatus.OK)
  List<Jeep> fetchJeeps(
      @RequestParam(required = false) JeepModel model,
      
      @Length(max = Constants.TRIM_MAX_LENGTH)
      @Pattern(regexp = "[\\w\\s]*")
      @RequestParam(required = false) String trim);

  // @formatter:on
}
