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









}
