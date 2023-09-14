package com.supermarket.back;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;
import java.util.zip.DataFormatException;


@RestController
@CrossOrigin
@RequestMapping("/api")
@SpringBootApplication
public class BackApplication implements CommandLineRunner {


	public static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {

		SpringApplication.run(BackApplication.class, args);
	}

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	UserDetailsManager users;

	@Autowired
	PasswordEncoder passwordEncoder;
	public void initializeDB(){
		jdbcTemplate.execute("DROP TABLE IF EXISTS \"Store_Product\" CASCADE");
		jdbcTemplate.execute("DROP TABLE IF EXISTS \"Category\" CASCADE");
		jdbcTemplate.execute("DROP TABLE IF EXISTS \"Product\" CASCADE");
		jdbcTemplate.execute("DROP TABLE IF EXISTS \"Sale\" CASCADE");
		jdbcTemplate.execute("DROP TABLE IF EXISTS \"Customer_Card\" CASCADE");
		jdbcTemplate.execute("DROP TABLE IF EXISTS \"Check\" CASCADE");
		jdbcTemplate.execute("DROP TABLE IF EXISTS \"Employee\" CASCADE");
		jdbcTemplate.execute("CREATE SEQUENCE if not exists store_product_seq START 1");
		jdbcTemplate.execute("CREATE SEQUENCE if not exists employee_seq START 1");
		jdbcTemplate.execute("CREATE SEQUENCE if not exists customer_card_seq START 1");
		jdbcTemplate.execute("CREATE SEQUENCE if not exists check_number_seq START 1");


		jdbcTemplate.execute("CREATE TABLE if not exists \"Category\"(" +
				"category_number SERIAL NOT NULL, " +
				"category_name VARCHAR(50) NOT NULL, " +
				"PRIMARY KEY (category_number))"
		);
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS \"Product\"(" +
				"id_product SERIAL NOT NULL, " +
				"category_number INT NOT NULL, " +
				"product_name VARCHAR(50) NOT NULL, " +
				"characteristics VARCHAR(100) NOT NULL," +
				"PRIMARY KEY(id_product), " +
				"FOREIGN KEY (category_number) " +
				"REFERENCES \"Category\"(category_number) " +
				"ON UPDATE CASCADE ON DELETE NO ACTION" +
				")"
		);
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS \"Store_Product\"(" +
				"UPC VARCHAR(12) NOT NULL, " +
				"UPC_prom VARCHAR(12), " +
				"id_product INT NOT NULL, " +
				"selling_price DECIMAL(13,4) NOT NULL," +
				"products_number INT NOT NULL, " +
				"promotional_product BOOLEAN DEFAULT false NOT NULL," +
				"PRIMARY KEY(UPC), " +
				"FOREIGN KEY(id_product) " +
				"REFERENCES \"Product\"(id_product) " +
				"ON UPDATE CASCADE ON DELETE NO ACTION, " +
				"FOREIGN KEY(UPC_prom) " +
				"REFERENCES \"Store_Product\"(UPC) " +
				"ON UPDATE CASCADE ON DELETE SET NULL" +
				")"
		);
		jdbcTemplate.execute("CREATE  TABLE IF NOT EXISTS \"Employee\"(" +
				"id_employee VARCHAR(10) DEFAULT CONCAT('em_', nextval('employee_seq')) NOT NULL, " +
				"empl_surname VARCHAR(50) NOT NULL, " +
				"empl_name VARCHAR(50) NOT NULL, " +
				"empl_patronymic VARCHAR(50), " +
				"empl_role VARCHAR(10) NOT NULL, " +
				"salary DECIMAL(13,4) NOT NULL, " +
				"date_of_birth DATE NOT NULL, " +
				"date_of_start DATE NOT NULL, " +
				"phone_number VARCHAR(13) NOT NULL, " +
				"city VARCHAR(50) NOT NULL, " +
				"street VARCHAR(50) NOT NULL, " +
				"zip_code VARCHAR(9) NOT NULL, " +
				"PRIMARY KEY(id_employee)" +
				")"
		);
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS \"Customer_Card\"(" +
				"card_number VARCHAR(13) NOT NULL DEFAULT CONCAT('cc_', nextval('customer_card_seq')), " +
				"cust_surname VARCHAR(50) NOT NULL, " +
				"cust_name VARCHAR(50) NOT NULL, " +
				"cust_patronymic VARCHAR(50), " +
				"phone_number VARCHAR(13) NOT NULL, " +
				"city VARCHAR(50), " +
				"street VARCHAR(50), " +
				"zip_code VARCHAR(9), " +
				"percent INT NOT NULL, " +
				"PRIMARY KEY(card_number)" +
				")"
		);
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS \"Check\"(" +
				"check_number VARCHAR(10) NOT NULL DEFAULT CONCAT('ch_', nextval('check_number_seq')), " +
				"id_employee VARCHAR(10) NOT NULL, " +
				"card_number VARCHAR(13), " +
				"print_date timestamp NOT NULL, " +
				"sum_total DECIMAL(13, 4) NOT NULL, " +
				"vat DECIMAL(13, 4) NOT NULL, " +
				"PRIMARY KEY(check_number), " +
				"FOREIGN KEY (id_employee) " +
				"REFERENCES \"Employee\"(id_employee) " +
				"ON UPDATE CASCADE ON DELETE NO ACTION, " +
				"FOREIGN KEY (card_number) " +
				"REFERENCES \"Customer_Card\"(card_number) " +
				"ON UPDATE CASCADE ON DELETE NO ACTION" +
				")"
		);
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS \"Sale\"(" +
				"UPC VARCHAR(12) NOT NULL, " +
				"check_number VARCHAR(10) NOT NULL, " +
				"product_number INT NOT NULL, " +
				"selling_price DECIMAL(13,4) NOT NULL, " +
				"PRIMARY KEY(UPC, check_number), " +
				"FOREIGN KEY(UPC) " +
				"REFERENCES \"Store_Product\"(UPC) " +
				"ON UPDATE CASCADE ON DELETE NO ACTION, " +
				"FOREIGN KEY (check_number) " +
				"REFERENCES \"Check\"(check_number) " +
				"ON UPDATE CASCADE ON DELETE CASCADE" +

				")"
		);
	}
	@Override
	public void run(String... args)throws Exception{
		//initializeDB();

//		users.deleteUser("admin");
//		users.deleteUser("user");
//		users.createUser(User.builder().username("admin").password(passwordEncoder.encode("password")).authorities("USER", "ADMIN").build());

	}



	@GetMapping("/public/login/{username}/{password}")
	public ResponseEntity<String> login(@PathVariable String username, @PathVariable String password){
		password = new String(Base64.getDecoder().decode(password));
		logger.info("login {} {}", username, password);

		UserDetails real = users.loadUserByUsername(username);
		String realpass = real.getPassword();
		if (passwordEncoder.matches(password, realpass)){
			logger.info("password is correct real:{} provided:{}", realpass, passwordEncoder.encode(password));
			try {
				return ResponseEntity.ok(jdbcTemplate.queryForObject("select id_employee " +
						"from empl_users " +
						"where username = ?", String.class, username)
				);
			}catch (EmptyResultDataAccessException e){
				if (real.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))){
					return ResponseEntity.ok("-1");
				}else {
					return ResponseEntity.ok("-2");
				}

			}
		}else {
			logger.info("incorrect password real:{} provided:{}", realpass, passwordEncoder.encode(password));
			return ResponseEntity.notFound().build();
		}
	}






	@PostMapping("/user/checks")
	public ResponseEntity<String> createCheck(@RequestBody CheckTransferObject transferObject){
		logger.info("post check: {}", transferObject);
		Check check = transferObject.getCheck();
		List<Pair<String, Integer>> products = transferObject.getProducts();
		String checkNumber = null;
		for (Pair<String,Integer> p : products){
			if (p.getSecond() <= 0)return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input quantity");
			try{
				Integer numberExist = jdbcTemplate.queryForObject(
						"select products_number from \"Store_Product\" where UPC = ?",
						Integer.class, p.getFirst()
				);
				if (numberExist < p.getSecond())return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Input is more than existing number");
			}catch (EmptyResultDataAccessException e){
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such product");
			}


		}

		if (check.getCard_number().equals("")){
			String checkQuery = "INSERT INTO \"Check\"( id_employee," +
					"sum_total, vat) " +
					"VALUES ( ?, ?, ?)";
			jdbcTemplate.update(checkQuery, check.getId_employee(),
					  check.getSum_total(), check.getVat());
			checkNumber = jdbcTemplate.queryForObject("SELECT last_value FROM check_number_seq", String.class);
		}else {
			String checkQuery = "INSERT INTO \"Check\"( id_employee, card_number, " +
					"sum_total, vat) " +
					"VALUES (?, ?, ?, ?)";
			jdbcTemplate.update(checkQuery, check.getId_employee(),
					check.getCard_number(), check.getSum_total(), check.getVat());
			checkNumber = jdbcTemplate.queryForObject("SELECT last_value FROM check_number_seq", String.class);
		}
		checkNumber = "ch_" + checkNumber;
		for (Pair<String, Integer> p : products){
			String UPC = p.getFirst();
			Integer product_number = p.getSecond();
			String updatePNumber = "UPDATE \"Store_Product\" SET products_number = products_number - ? " +
					"WHERE UPC = ?";
			jdbcTemplate.update(updatePNumber, product_number, UPC);
			BigDecimal cost = jdbcTemplate.queryForObject("SELECT selling_price " +
					"FROM \"Store_Product\" WHERE UPC = ?", BigDecimal.class, UPC);

			String saleQuery = "INSERT INTO \"Sale\" (UPC, check_number, product_number, selling_price) " +
					"VALUES (?, ?, ?, ?)";
			jdbcTemplate.update(saleQuery, UPC,checkNumber,product_number, cost);
		}
		return ResponseEntity.ok("Check created " + check);
	}






	@DeleteMapping("/admin/checks/{id}")
	public ResponseEntity<String> deleteCheck(@PathVariable String id){
		logger.info("delete check: {}", id);
		String deleteQuery = "DELETE FROM \"Check\" WHERE check_number = ?";
		int rowsAffected = jdbcTemplate.update(deleteQuery, id);
		if (rowsAffected > 0) {
			return ResponseEntity.ok("Check deleted successfully");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Check not found");
		}
	}
	@DeleteMapping("/admin/checks/with_employee/{id_employee}")
	public ResponseEntity<String> deleteChecksOfEmployee(@PathVariable String id_employee){
		logger.info("delete all checks of employee {}", id_employee);
		int rowsAffected = jdbcTemplate.update("delete from \"Check\" where id_employee = ?", id_employee);
		if (rowsAffected > 0) {
			return ResponseEntity.ok("Checks deleted successfully");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Checks not found");
		}
	}








	@GetMapping("/user/checks/{id_employee}/{left_date}/{right_date}")
	public ResponseEntity<List<Map<String,Object>>> getChecksWithTimeStamp(@PathVariable String id_employee, @PathVariable Date left_date, @PathVariable Date right_date){
		logger.info("get checks of {} from {} to {}", id_employee,left_date.toString(),right_date.toString());

		String query = "SELECT * FROM \"Check\" " +
				"WHERE id_employee = ? AND print_date::date >= ? AND print_date::date <= ?;";
		List<Map<String, Object>>result = jdbcTemplate.queryForList(query, id_employee,left_date,right_date);
		return ResponseEntity.ok(result);
	}
	@GetMapping("/user/sales/{check_number}")
	public ResponseEntity<List<Map<String, Object>>> getSalesOfCheck(@PathVariable String check_number){
		logger.info("get sales from check: {}", check_number);
		String query = "SELECT s.UPC, p.product_name, sp.products_number, s.selling_price, s.selling_price * s.product_number AS total " +
				"FROM \"Sale\" s " +
				"INNER JOIN \"Store_Product\" sp ON s.UPC = sp.UPC " +
				"INNER JOIN \"Product\" p ON sp.id_product = p.id_product " +
				"WHERE s.check_number = ?;";
		List<Map<String, Object>> result = jdbcTemplate.queryForList(query, check_number);
		return ResponseEntity.ok(result);
	}
	@GetMapping("/admin/checks/all/{left_date}/{right_date}")
	public ResponseEntity<List<Map<String,Object>>> getAllChecksWithTimeStamp(@PathVariable Date left_date, @PathVariable Date right_date){
		logger.info("get checks from {} to {}", left_date, right_date);
		String query = "SELECT * FROM \"Check\" " +
				"WHERE print_date::date >= ? AND print_date::date <= ?;";
		List<Map<String, Object>>result = jdbcTemplate.queryForList(query, left_date,right_date);
		return ResponseEntity.ok(result);
	}
	@GetMapping("/admin/checks/price/{id_employee}/{left_date}/{right_date}")
	public ResponseEntity<BigDecimal> getSumOfChecksOfEmployee(@PathVariable String id_employee,@PathVariable Date left_date, @PathVariable Date right_date){
		logger.info("get sum of checks of {} from {} to {}", id_employee, left_date.toString(), right_date.toString());
		String query = "SELECT SUM(sum_total) " +
				"FROM \"Check\" " +
				"WHERE id_employee = ? AND print_date::date >= ? AND print_date::date <= ?;";
		BigDecimal result = jdbcTemplate.queryForObject(query, BigDecimal.class, id_employee, left_date, right_date);
		if (result == null){
			return ResponseEntity.ok(BigDecimal.ZERO);
		}
		return ResponseEntity.ok(result);
	}
	@GetMapping("/admin/checks/price/all/{left_date}/{right_date}")
	public ResponseEntity<BigDecimal> getSumOfChecksOfEveryone(@PathVariable Date left_date, @PathVariable Date right_date){
		logger.info("get price of checks from {} to {}", left_date.toString(), right_date.toString());
		String query = "SELECT SUM(sum_total) " +
				"FROM \"Check\" " +
				"WHERE print_date::date >= ? AND print_date::date <= ?;";
		BigDecimal result = jdbcTemplate.queryForObject(query, BigDecimal.class, left_date, right_date);
		if (result == null){
			return ResponseEntity.ok(BigDecimal.ZERO);
		}
		return ResponseEntity.ok(result);
	}
	@GetMapping("/admin/checks")
	public ResponseEntity<List<Map<String, Object>>> getAllChecks(){
		logger.info("get all checks");
		return ResponseEntity.ok(
				jdbcTemplate.queryForList("SELECT * FROM \"Check\" ORDER BY print_date")
		);
	}
	@GetMapping("/user/checks/{check_number}")
	public ResponseEntity<Check> getCheck(@PathVariable String check_number){
		logger.info("get check {}", check_number);
		try{
			return ResponseEntity.ok(jdbcTemplate.queryForObject(
					"SELECT check_number, id_employee, card_number, print_date, sum_total, vat FROM \"Check\" WHERE check_number = ?",
					(rs, rowNum) -> new Check(
							rs.getString("check_number"),
							rs.getString("id_employee"),
							rs.getString("card_number"),
							rs.getTimestamp("print_date"),
							rs.getBigDecimal("sum_total"),
							rs.getInt("vat")
					),
					check_number
			));
		}catch (EmptyResultDataAccessException e){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Check());
		}

	}



}
