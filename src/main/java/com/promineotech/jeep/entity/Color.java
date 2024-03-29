package com.promineotech.jeep.entity;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Color {
private Long  colorPk;
private String colorId;
private String color;
private BigDecimal price;
private boolean isExterior;
}
