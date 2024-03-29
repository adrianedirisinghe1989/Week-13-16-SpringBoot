package com.promineotech.jeep.entity;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Order {
private Long orderPk;
private Customer customer;
private Jeep model;
private Color color;
private Engine engine;
private Tire tire;
private List<Option>options;
private BigDecimal price;

public Long getOrderPk() {
	return orderPk;
}

}
