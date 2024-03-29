package com.promineotech.jeep.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import com.promineotech.jeep.entity.Order;
import com.promineotech.jeep.entity.OrderRequest;
import com.promineotech.jeep.entity.Tire;
import io.swagger.v3.core.jackson.TypeNameResolver.Options;
import com.promineotech.jeep.entity.Color;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.Customer;
import com.promineotech.jeep.entity.Engine;
import com.promineotech.jeep.entity.JeepModel;
import com.promineotech.jeep.entity.Option;

public interface JeepOrderDao{
	

Optional<Customer> fetchCustomer(String customerId);


Optional<Jeep> fetchModel(JeepModel model, String trim, int doors);
	
Optional<Color> fetchColor(String colorID);
	
Optional<Engine> fetchEngine(String engineId);
	
Optional<Tire> fetchTire(String tireId);


Order saveOrder(Customer customer, Jeep jeep, Color color, Engine engine, Tire tire, BigDecimal price, List<Option> options);


List<Option> fetchOptions(List<String> optionIds);

	
}
	