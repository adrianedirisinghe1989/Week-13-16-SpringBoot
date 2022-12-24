package com.promineotech.jeep.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.promineotech.jeep.controller.support.FetchJeepTestSupport;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;

import lombok.Getter;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = {
		"claspath:flyway/migrations/v1.0_Jeep_Schema.sql",
		"classpath:flyway/migrations/v1.1_Jeep_Date.sql"},
		config= @SqlConfig(encoding = "utf-8"))

class FetchJeepTest extends FetchJeepTestSupport {
	

	@Test
	void testThatJeepsAreReturnedWhenAvaidModelAndTrimAreSupplied() {
	// Given: a valid model, trim and URI
		JeepModel model = JeepModel.WRANGLER;
		String trim = "Sport"; 
		String uri = String.format("%s?model=%s&trim=%s",getBaseUri(),model,trim);	
		
	//When: a connection is made to the URI
	ResponseEntity<List<Jeep>>response=
	restTemplate.exchange(uri,HttpMethod.GET,null,new ParameterizedTypeReference<>() {});
		
	//Then: a success( OK- 200) status code is returned 	
	assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	
	// And: the actual list returned is the same as the expected list
	List<Jeep> expected = buildExpected();
	assertThat(response.getBody()).isEqualTo(expected);
	}
	
	@Autowired
	@Getter
	private TestRestTemplate restTemplate;
	
	@LocalServerPort
	private int serverPort;
	
}
