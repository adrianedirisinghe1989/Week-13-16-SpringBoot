package com.promineotech.jeep.dao;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import com.promineotech.jeep.entity.Color;
import com.promineotech.jeep.entity.Customer;
import com.promineotech.jeep.entity.Engine;
import com.promineotech.jeep.entity.FuelType;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;
import com.promineotech.jeep.entity.Model;
import com.promineotech.jeep.entity.Option;
import com.promineotech.jeep.entity.OptionType;
import com.promineotech.jeep.entity.Order;
import com.promineotech.jeep.entity.OrderRequest;
import com.promineotech.jeep.entity.Tire;
import lombok.extern.slf4j.Slf4j;
import com.promineotech.jeep.dao.DefaultJeepOrderDao.SqlParams;

@Component
public class DefaultJeepOrderDao implements JeepOrderDao {
	@Autowired
	private NamedParameterJdbcTemplate jbdcTemplate;

	@Override
	public Order saveOrder(Customer customer, Jeep jeep, Color color, Engine engine, Tire tire, BigDecimal price,
			List<Option> options) {
		SqlParams params = generateInsertSql(customer, jeep, color, engine, tire, price);

		KeyHolder keyHolder = new GeneratedKeyHolder();
		jbdcTemplate.update(params.sql, params.source, keyHolder);

		Long orderPk = keyHolder.getKey().longValue();
		saveOptions(options, orderPk);

	//@formatter:off
	return Order.builder()
			.orderPk(orderPk)
			.customer(customer)
			.model(jeep)
			.color(color)
			.engine(engine)
			.tire(tire)
			.options(options)
			.price(price)
			.build();
	//@formatter:on
	}

	/**
	 * 
	 * @param options
	 * @param orderPk
	 */
	private void saveOptions(List<Option> options, Long orderPk) {
		for (Option option : options) {
			SqlParams params = generateInsertSql(option, orderPk);
			jbdcTemplate.update(params.sql, params.source);
		}
	}
	private SqlParams generateInsertSql(Option option, Long orderPk) {
		SqlParams params = new SqlParams();

	//@formatter:off
	params. sql =" "
		+ "INSERT INTO order_options ("
		+ "option_fk, order_fk"
		+ ") VALUES ("
		+ ":option_fk, :order_fk"
		+ ")";
	//@formatter:on

		params.source.addValue("option_fk", option.getOptionPk());
		params.source.addValue("order_fk", orderPk);

		return params;
	}

	/**
	 * 
	 * @param customer
	 * @param jeep
	 * @param color
	 * @param engine
	 * @param tire
	 * @param price
	 * @return
	 */
	private SqlParams generateInsertSql(Customer customer, Jeep jeep, Color color, Engine engine, Tire tire,
			BigDecimal price) {
	//@formatter:off
	String sql = ""
	 + "INSERT INTO orders ("
	 + "customer_fk, color_fk, engine_fk, tire_fk, model_fk, price"
	 + ") VALUES ("
	 + ":customer_fk, :color_fk, :engine_fk, :tire_fk, :model_fk, :price"
	 + ")";
	//@formatter:on

		SqlParams params = new SqlParams();

		params.sql = sql;
		params.source.addValue("customer_fk", customer.getCustomerPK());
		params.source.addValue("color_fk", color.getColorPk());
		params.source.addValue("engine_fk", engine.getEnginePk());
		params.source.addValue("model_fk", jeep.getModelPk());
		params.source.addValue("tire_fk", tire.getTirePk());
		params.source.addValue("price", price);

		return params;
	}

	@Override
	public List<Option> fetchOptions(List<String> optionIds) {
		if (optionIds.isEmpty()) {
			return new LinkedList<>();
		}

		Map<String, Object> params = new HashMap<>();
	//@formatter:off
	String sql = " "
		+ "SELECT * "
		+ "FROM options "
		+ "WHERE option_id IN(";
	// @formatter:on

		for (int index = 0; index < optionIds.size(); index++) {
			String key = "option_" + index;
			sql += ":" + key + ", ";
			params.put(key, optionIds.get(index));
		}

		sql = sql.substring(0, sql.length() - 2);
		sql += ")";

		return jbdcTemplate.query(sql, params, new RowMapper<Option>() {

			@Override
			public Option mapRow(ResultSet rs, int rowNum) throws SQLException {
				// @formatter :off
				return Option.builder().category(OptionType.valueOf(rs.getString("category")))
						.manufacturer(rs.getString("manufacturer")).name(rs.getString("name"))
						.optionId(rs.getString("option_id")).optionPk(rs.getLong("option_pk"))
						.price(rs.getBigDecimal("price")).build();
				// @formatter: on
			}
		});

	}

	/**
	 * 
	 */
	@Override
	public Optional<Customer> fetchCustomer(String customerId) {
		String sql = "" 
	+ "SELECT * " 
	+ "FROM customers " 
	+ "WHERE customer_id = :customer_id";

		Map<String, Object> params = new HashMap<>();
		params.put("customer_id", customerId);

		return Optional.ofNullable(jbdcTemplate.query(sql, params, new CustomerResultSetExtractor()));
	}

	/**
	 *
	 */
	@Override
	public Optional<Jeep> fetchModel(JeepModel model, String trim, int doors) {
	//@formatter:off
	String sql =""
	  + "SELECT * "
	  + "FROM models "
	  + "WHERE model_id = :model_id "
	  + "AND trim_level = :trim_level "
	  + "AND num_doors = :num_doors";
	//@formatter:on

		Map<String, Object> params = new HashMap<>();
		params.put("model_id", model.toString());
		params.put("trim_level", trim);
		params.put("num_doors", doors);

		return Optional.ofNullable(jbdcTemplate.query(sql, params, new ModelResultSetExtractor()));
	}

	/**
	*
	*/
	@Override
	public Optional<Color> fetchColor(String colorId) {
	//@formatter:off
	String sql =""
	 + "SELECT * "
	 + "FROM colors "
	 + "WHERE color_id = :color_id ";

	//@formatter:on

		Map<String, Object> params = new HashMap<>();
		params.put("color_id", colorId);

		return Optional.ofNullable(jbdcTemplate.query(sql, params, new ColorResultSetExtractor()));
	}

	/**
	*
	*/
	@Override
	public Optional<Engine> fetchEngine(String engineId) {
//@formatter:off
String sql =""
 + "SELECT * "
 + "FROM engines "
 + "WHERE engine_id = :engine_id ";

//@formatter:on

		Map<String, Object> params = new HashMap<>();
		params.put("engine_id", engineId);

		return Optional.ofNullable(jbdcTemplate.query(sql, params, new EngineResultSetExtractor()));
	}

/**
*
*/
@Override
public Optional<Tire> fetchTire( String tireId) {
//@formatter:off
String sql =""
 + "SELECT * "
 + "FROM tires "
 + "WHERE tire_id = :tire_id ";

//@formatter:on

Map<String, Object> params = new HashMap<>();
params.put("tire_id",tireId);


return Optional.ofNullable(jbdcTemplate.query(sql, params,new TireResultSetExtractor()));


}
class CustomerResultSetExtractor implements ResultSetExtractor<Customer>{
	@Override
	public Customer extractData(ResultSet rs) throws SQLException, DataAccessException {
	rs.next();
	
	// @formatter:off
	return Customer.builder()
		.customerId(rs.getString("customer_id"))
		.customerPK(rs.getLong("customer_pk"))
		.firstName(rs.getString("first_name"))
		.lastName(rs.getString("last_name"))
		.phone(rs.getString("phone"))
		.build();
			// @formatter:on
	}		
}
class ColorResultSetExtractor implements ResultSetExtractor<Color>{
	@Override
	public Color extractData(ResultSet rs) throws SQLException, DataAccessException {
	rs.next();
	
	// @formatter:off
	return Color.builder()
		.colorId(rs.getString("color_id"))
		.colorPk(rs.getLong("color_pk"))
		.color(rs.getString("color"))
		.price(rs.getBigDecimal("price"))
		.isExterior(rs.getBoolean("is_exterior"))
		.build();
			// @formatter:on
	}
}
class EngineResultSetExtractor implements ResultSetExtractor<Engine>{
	@Override
	public Engine extractData(ResultSet rs) throws SQLException, DataAccessException {
	rs.next();
	
	// @formatter:off
	return Engine.builder()
		.engineId(rs.getString("engine_id"))
		.enginePk(rs.getLong("engine_pk"))
		.sizeInLiters(rs.getFloat("size_In_liters"))
		.name(rs.getString("name"))
		.fuelType(FuelType.valueOf(rs.getString("fuel_type")))
		.mpgHwy(rs.getFloat("mpg_hwy"))
		.hasStartStop(rs.getBoolean("has_start_stop"))
		.description(rs.getString("description"))
		.price(rs.getBigDecimal("price"))
		.build();
			// @formatter:on
	}
	
	}
class ModelResultSetExtractor implements ResultSetExtractor<Jeep>{
	@Override
	public Jeep extractData(ResultSet rs) throws SQLException, DataAccessException {
	rs.next();
	
	// @formatter:off
	return Jeep.builder()
			.basePrice(new BigDecimal(rs.getString("base_price")))
			.modelId(JeepModel.valueOf(rs.getString("model_id")))
			.modelPk(rs.getLong("model_pk"))
			.numDoors(rs.getInt("num_doors"))
			.trimLevel(rs.getString("trim_level"))
			.wheelSize(rs.getInt("wheel_size"))
			.build();	
			// @formatter:on
	}
	}
class TireResultSetExtractor implements ResultSetExtractor<Tire>{
	@Override
	public Tire extractData(ResultSet rs) throws SQLException, DataAccessException {
	rs.next();
	
	// @formatter:off
	return Tire.builder()
		.tireId(rs.getString("tire_id"))
		.tirePk(rs.getLong("tire_pk"))
		.tireSize(rs.getString("tire_size"))
		.manufacturer(rs.getString("manufacturer"))
		.price(rs.getBigDecimal("price"))
		.warrantyMiles(rs.getInt("warranty_miles"))
		.build();
			// @formatter:on
	}
}
class SqlParams {
	String sql;
	MapSqlParameterSource source = new MapSqlParameterSource();
}
}