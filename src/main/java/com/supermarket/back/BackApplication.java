package com.supermarket.back;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.math.BigDecimal;
import java.math.BigInteger;
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
	@GetMapping("/user/users/{username}")
	public ResponseEntity<UserDetails> getUser(@PathVariable String username){
		logger.info("get user {}", username);
		return ResponseEntity.ok(users.loadUserByUsername(username));
	}
	@PostMapping("/admin/users")
	public ResponseEntity<MyUser> createUser(@RequestBody MyUser user){

		logger.info("create user {}", user);
		UserDetails userDetails = User.builder()
				.username(user.getUsername())
				.password(passwordEncoder.encode(user.getPassword()))
				.authorities(user.getAuthorities())
				.build();
		users.createUser(userDetails);
		return ResponseEntity.ok(user);
	}
	@PutMapping("/public/users/{username}/{oldpassword}/{password}")
	public ResponseEntity<MyUser> changeUser(@PathVariable String username, @PathVariable String password, @PathVariable String oldpassword){
		logger.info("change user {}", username);
		password = new String(Base64.getDecoder().decode(password));
		oldpassword = new String(Base64.getDecoder().decode(oldpassword));
		logger.info("change from {} to {}", oldpassword, password);

		String realpass = users.loadUserByUsername(username).getPassword();
		Collection<? extends GrantedAuthority> authorities = users.loadUserByUsername(username).getAuthorities();
		if (passwordEncoder.matches(oldpassword, realpass)){
			UserDetails userDetails = User.builder()
					.username(username)
					.password(passwordEncoder.encode(password))
					.authorities(authorities)
					.build();
			users.updateUser(userDetails);
			return ResponseEntity.ok(new MyUser());
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MyUser());
		}


	}
	@DeleteMapping("/admin/users/{username}")
	public ResponseEntity<String> deleteUser(@PathVariable String username){
		logger.info("delete user {}", username);
		users.deleteUser(username);
		return ResponseEntity.ok(username);
	}
	@PostMapping("/admin/categories")
	public ResponseEntity<Category> createCategory(@RequestBody Category category){
		logger.info("post category: {}", category);
		String insertQuery = "INSERT INTO \"Category\" (category_name) VALUES (?)";
		jdbcTemplate.update(insertQuery, category.getCategory_name());
		return ResponseEntity.status(HttpStatus.CREATED).body(category);
	}
	@PostMapping("/admin/employees")
	public ResponseEntity<String> createEmployee(@RequestBody EmployeeAndUser employeeAndUser){
		Employee employee = employeeAndUser.getEmployee();
		MyUser user = employeeAndUser.getUser();

		logger.info("post employee: {}\n{}", employee,user);
		try{
			String[] authorities;
			if (employee.getEmpl_role().equalsIgnoreCase("касирка")){
				authorities = new String[1];
				authorities[0] = "USER";
			}else if(employee.getEmpl_role().equalsIgnoreCase("менеджерка")){
				authorities = new String[2];
				authorities[0] = "USER";
				authorities[1] = "ADMIN";
			}
			else throw new DataFormatException("role is incorrect");


			users.createUser(User.builder()
					.username(user.getUsername())
					.password(passwordEncoder.encode(new String(Base64.getDecoder().decode(user.getPassword()))))
					.authorities(authorities)
					.build());
			String insertQuery = "INSERT INTO \"Employee\" (empl_surname, empl_name, empl_patronymic, empl_role, salary, date_of_birth, date_of_start, phone_number, city, street, zip_code ) " +
					"VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			jdbcTemplate.update(insertQuery, employee.getEmpl_surname(), employee.getEmpl_name(),
					employee.getEmpl_patronymic(), employee.getEmpl_role(), employee.getSalary(),
					employee.getDate_of_birth(), employee.getDate_of_start(), employee.getPhone_number(), employee.getCity(),
					employee.getStreet(), employee.getZip_code());
			String id_employee = jdbcTemplate.queryForObject(
					"SELECT last_value from employee_seq",String.class
			);
			insertQuery = "INSERT INTO empl_users (id_employee, username) values " +
					"(?,?)";
			jdbcTemplate.update(insertQuery,"em_"+id_employee, user.getUsername());

		} catch (DataFormatException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad role");
		}catch (DuplicateKeyException e){
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Username is taken");
		}catch (DataIntegrityViolationException e){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("dunno some mistake i just have");
		}
//		catch (PSQLException e){
//			return ResponseEntity.status(HttpStatus.CONFLICT).body(employee);
//		}


		return ResponseEntity.status(HttpStatus.CREATED).body("Employee created successfully " + employee);
	}
	@PostMapping("/user/customer_cards")
	public ResponseEntity<String> createCustomer_Card(@RequestBody Customer_Card customerCard){
		logger.info("post customer card: {}", customerCard);
		String insertQuery = "INSERT INTO \"Customer_Card\" ( cust_surname, cust_name, cust_patronymic, phone_number, city, street, zip_code, percent) " +
				"VALUES ( ?, ?, ?, ?, ?, ?, ?, ?)";

		jdbcTemplate.update(insertQuery, customerCard.getCust_surname(), customerCard.getCust_name(),
				customerCard.getCust_patronymic(), customerCard.getPhone_number(), customerCard.getCity(),
				customerCard.getStreet(), customerCard.getZip_code(), customerCard.getPercent());

		return ResponseEntity.status(HttpStatus.CREATED).body("Customer created successfully " + customerCard);
	}
	@PostMapping("/admin/products")
	public ResponseEntity<String> createProduct(@RequestBody Product product){
		logger.info("post product: {}", product);
		String insertQuery = "INSERT INTO \"Product\" (category_number, product_name, characteristics) " +
				"VALUES (?, ?, ?)";
		try {
			jdbcTemplate.update(insertQuery, product.getCategory_number(), product.getProduct_name(), product.getCharacteristics());
			return ResponseEntity.status(HttpStatus.CREATED).body("Product created successfully " + product);
		}catch (DataIntegrityViolationException e){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role doesn't exist");
		}

	}
	@PostMapping("/admin/store_products")
	public ResponseEntity<String> createStore_Product(@RequestBody Store_Product storeProduct){
		logger.info("post store product: {}", storeProduct);
		List<Map<String,Object>> checking = jdbcTemplate.queryForList("select * from \"Store_Product\" where id_product = ?",
				storeProduct.getId_product());
		if (checking.size() >= 2 || (checking .size() == 0 && storeProduct.isPromotional_product())){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No place or non-promotional product" + storeProduct);
		}
		if (checking.size() == 1){
			if (storeProduct.isPromotional_product()){
				storeProduct.setSelling_price(((BigDecimal) checking.get(0).get("selling_price")).multiply(new BigDecimal(0.8)));
			}else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This product must be promotional " + storeProduct);
			}
		}
		try{
			if (storeProduct.getUPC_prom() == null || storeProduct.getUPC_prom().equals("") ){
				String insertQuery = "INSERT INTO \"Store_Product\" (UPC, id_product, selling_price, products_number, " +
						"promotional_product) " +
						"VALUES (?, ?, ?, ?, ?)";
				jdbcTemplate.update(insertQuery, storeProduct.getUPC(),
						storeProduct.getId_product(), storeProduct.getSelling_price(), storeProduct.getProducts_number(),
						storeProduct.isPromotional_product());
			}
			else {
				String insertQuery = "INSERT INTO \"Store_Product\" (UPC, UPC_prom, id_product, selling_price, products_number, " +
						"promotional_product) " +
						"VALUES (?, ?, ?, ?, ?, ?)";
				jdbcTemplate.update(insertQuery,storeProduct.getUPC(), storeProduct.getUPC_prom(),
						storeProduct.getId_product(), storeProduct.getSelling_price(), storeProduct.getProducts_number(),
						storeProduct.isPromotional_product());
			}
			if (storeProduct.isPromotional_product()){
				jdbcTemplate.update(
						"UPDATE \"Store_Product\" set UPC_prom = ? where id_product = ? AND UPC <> ? ",
						storeProduct.getUPC(), storeProduct.getId_product(), storeProduct.getUPC()

				);
			}

			return ResponseEntity.status(HttpStatus.CREATED).body("Created store product " + storeProduct);
		}catch (DuplicateKeyException e){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Such key already exists");
		}catch (DataIntegrityViolationException e){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such product");
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
	@DeleteMapping("/admin/categories/{id}")
	public ResponseEntity<String> deleteCategory(@PathVariable int id){
		logger.info("delete category: {}", id);
		String deleteQuery = "DELETE FROM \"Category\" WHERE category_number = ?;";
		//System.out.println(id);
		try {
			int rowsAffected = jdbcTemplate.update(deleteQuery, id);
			if (rowsAffected > 0) {
				//System.out.println("Deleted category");
				return ResponseEntity.ok("Category deleted successfully");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
			}
		}catch (DataIntegrityViolationException e){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category has products");
		}

	}
	@DeleteMapping("/admin/employees/{id}")
	public ResponseEntity<String> deleteEmployee(@PathVariable String id){
		logger.info("delete employee: {}", id);
		jdbcTemplate.update(
				"delete from authorities " +
						"where username IN (" +
						"SELECT empl_users.username from empl_users " +
						"WHERE id_employee = ?" +
						");",
				id
		);
		jdbcTemplate.update(
				"delete from users " +
						"where username IN (" +
							"SELECT empl_users.username from empl_users " +
							"WHERE id_employee = ?" +
						");",
				id
		);
		String deleteQuery = "DELETE FROM \"Employee\" WHERE id_employee = ?;";
		int rowsAffected = jdbcTemplate.update(deleteQuery, id);
		if (rowsAffected > 0) {
			return ResponseEntity.ok("Employee deleted successfully");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
		}
	}
	@DeleteMapping("/admin/products/{id}")
	public ResponseEntity<String> deleteProduct(@PathVariable int id){
		logger.info("delete product: {}", id);
		String deleteQuery = "DELETE FROM \"Product\" WHERE id_product = ?;";
		int rowsAffected = jdbcTemplate.update(deleteQuery, id);
		if (rowsAffected > 0) {
			return ResponseEntity.ok("Product deleted successfully");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
		}
	}

	@GetMapping("user/categories/is_empty/{category_number}")
	public ResponseEntity<Boolean> getEmpltyCategories(@PathVariable int category_number){
		return ResponseEntity.ok(
				jdbcTemplate.queryForList(
						"select * from \"Category\" c where category_number = ? and category_number " +
								"not in(select category_number from \"Product\" p " +
								"where p.category_number = c.category_number " +
								"and p.id_product not in(select pp.id_product from \"Store_Product\" sp " +
								"full join \"Product\" pp on pp.id_product = sp.id_product " +
								"group by pp.id_product " +
								"HAVING SUM(coalesce(products_number, 0)) = 0))",
						category_number
				).isEmpty()?false:true
		);
	}
	@GetMapping("user/categories/get_sold_today/{category_number}")
	public ResponseEntity<Integer> getNumberOfSoldProductsForCategory(@PathVariable int category_number){
		logger.info("get number of products sold today for category {}", category_number);
		return ResponseEntity.ok(jdbcTemplate.queryForObject(
				"SELECT SUM(s.product_number) AS total_sold " +
				"from \"Category\" c INNER JOIN \"Product\" p " +
						"ON p.category_number = c.category_number AND c.category_number = ? " +
						"INNER JOIN \"Store_Product\" sp " +
						"ON sp.id_product = p.id_product " +
						"INNER JOIN \"Sale\" s " +
						"ON s.UPC = sp.UPC " +
						"INNER JOIN \"Check\" ch " +
						"ON ch.print_date::date = CURRENT_DATE " +
						"AND ch.check_number = s.check_number " +
						"GROUP BY c.category_number, c.category_name " +
						"ORDER BY total_sold",Integer.class, category_number
		));
	}
	@DeleteMapping("/admin/store_products/{id}")
	public ResponseEntity<String> deleteStore_Product(@PathVariable String id){
		logger.info("delete store product: {}", id);
		String deleteQuery = "DELETE FROM \"Store_Product\" WHERE UPC = ?";
		try {
			int rowsAffected = jdbcTemplate.update(deleteQuery, id);
			if (rowsAffected > 0) {
				return ResponseEntity.ok("Store_Product deleted successfully");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Store_Product not found");
			}
		}catch (DataIntegrityViolationException e){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sales for this product exist");
		}


	}
	@DeleteMapping("/user/customer_cards/{id}")
	public ResponseEntity<String> deleteCustomer_Card(@PathVariable String id){
		logger.info("delete customer card: {}", id);
		String deleteQuery = "DELETE FROM \"Customer_Card\" WHERE card_number = ?;";
		int rowsAffected = jdbcTemplate.update(deleteQuery, id);
		if (rowsAffected > 0) {
			return ResponseEntity.ok("Customer_Card deleted successfully");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer_Card not found");
		}
	}
	@GetMapping("/admin/employees/get_sold_products/{minSum}")
	public ResponseEntity<List<Map<String , Object>>> getSoldProductsCount(@PathVariable double minSum){
		logger.info("get number of sold products for each employee with price");
		return ResponseEntity.ok(
				jdbcTemplate.queryForList(
						"SELECT subquery.id_employee, subquery.empl_surname, subquery.empl_name, " +
								"subquery.empl_role, subquery.sell_sum,( " +
								"    SELECT SUM(product_number)\n" +
								"    FROM \"Sale\" s\n" +
								"    INNER JOIN \"Check\" ch ON s.check_number = ch.check_number \n" +
								"      AND ch.id_employee = subquery.id_employee\n" +
								"    GROUP BY subquery.id_employee\n" +
								"  ) AS sold_products_amount\n" +
								"FROM\n" +
								"  (\n" +
								"    SELECT\n" +
								"      b.id_employee,\n" +
								"      b.empl_surname,\n" +
								"      b.empl_name,\n" +
								"      b.empl_role,\n" +
								"      (\n" +
								"        SELECT SUM(sum_total)\n" +
								"        FROM \"Check\"\n" +
								"        WHERE id_employee = b.id_employee\n" +
								"        GROUP BY b.id_employee\n" +
								"      ) AS sell_sum\n" +
								"    FROM\n" +
								"      \"Employee\" b\n" +
								"    WHERE\n" +
								"      id_employee IN (SELECT id_employee FROM \"Check\")\n" +
								"  ) AS subquery\n" +
								"WHERE\n" +
								"  subquery.sell_sum > ?;", minSum
				)
		);
	}
	@GetMapping("/user/categories/best_sellers")
	public ResponseEntity<List<Map<String, Object>>> getTodaySBestSellingCategories(){
		logger.info("get best selling categories");
		return ResponseEntity.ok(
				jdbcTemplate.queryForList(
						"select c.category_number, c.category_name, SUM(s.product_number) AS total_sold " +
								"from \"Category\" c INNER JOIN \"Product\" p " +
								"ON p.category_number = c.category_number " +
								"INNER JOIN \"Store_Product\" sp " +
								"ON sp.id_product = p.id_product " +
								"INNER JOIN \"Sale\" s " +
								"ON s.UPC = sp.UPC " +
								"INNER JOIN \"Check\" ch " +
								"ON ch.print_date::date = CURRENT_DATE " +
								"AND ch.check_number = s.check_number " +
								"GROUP BY c.category_number, c.category_name " +
								"ORDER BY total_sold DESC LIMIT 3;"

				)
		);
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
	@PutMapping("/admin/employees/{id_employee}")
	public ResponseEntity<String> updateEmployee(@PathVariable String id_employee, @RequestBody Employee employee){
		logger.info("update employee: {}\n{}",id_employee, employee );

		try{
			String username = jdbcTemplate.queryForObject("select username from empl_users where id_employee = ?",
					String.class ,id_employee);

			String[] roles;
			if (employee.getEmpl_role().equalsIgnoreCase("менеджерка")){
				roles = new String[2];
				roles[0] = "USER";
				roles[1] = "ADMIN";
			}else if (employee.getEmpl_role().equalsIgnoreCase("касирка")){
				roles = new String[1];
				roles[0] = "USER";
			}else throw new DataFormatException();

			String updateQuery = "UPDATE \"Employee\" SET empl_surname = ?, empl_name = ?, empl_patronymic = ?," +
					" empl_role = ?, phone_number = ?, city = ?, street =?, zip_code = ?, salary = ?, " +
					"date_of_birth = ?, date_of_start = ? " +
					"WHERE id_employee = ?;";
			jdbcTemplate.update(updateQuery, employee.getEmpl_surname(), employee.getEmpl_name(), employee.getEmpl_patronymic(),
					employee.getEmpl_role(), employee.getPhone_number(), employee.getCity(), employee.getStreet(),
					employee.getZip_code(), employee.getSalary(),employee.getDate_of_birth(),employee.getDate_of_start(), id_employee);

			users.updateUser(User.builder()
					.username(username)
					.authorities(roles)
					.password(users.loadUserByUsername(username).getPassword())
					.build());
		}catch (EmptyResultDataAccessException e){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user for such employee " + employee);
		} catch (DataFormatException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid role " + employee);
		}


		return ResponseEntity.ok("Changed employee " + employee);

	}
	@PutMapping("/user/customer_cards/{card_number}")
	public ResponseEntity<String> updateCard(@PathVariable String card_number, @RequestBody Customer_Card customerCard){
		logger.info("update customer card: {}\n{}", card_number, customerCard);
		if(customerCard.getPercent()<0)return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Negative percent");
		String updateQuery = "UPDATE \"Customer_Card\" SET cust_surname = ?, cust_name = ?, cust_patronymic = ?, " +
				"phone_number = ?, city = ?, street = ?, zip_code = ?, percent = ? " +
				"WHERE card_number = ?;";
		int aff = jdbcTemplate.update(updateQuery, customerCard.getCust_surname(), customerCard.getCust_name(),
				customerCard.getCust_patronymic(), customerCard.getPhone_number(), customerCard.getCity(),
				customerCard.getStreet(), customerCard.getZip_code(), customerCard.getPercent(), card_number);
		if (aff > 0){
			return ResponseEntity.ok("Changed employee " + customerCard);
		}else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such employee");

	}
	@PutMapping("/admin/categories/{category_number}")
	public ResponseEntity<String> changeCategory(@PathVariable int category_number, @RequestBody Category category){
		logger.info("update category: {}\n{}", category_number, category);
		String updateQuery = "UPDATE \"Category\" SET category_name = ? " +
				"WHERE category_number = ?;";
		int aff = jdbcTemplate.update(updateQuery, category.getCategory_name(), category_number);
		if (aff> 0)return ResponseEntity.ok("Changed category " + category);
		else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such category");
	}
	@PutMapping("/admin/products/{id_product}")
	public ResponseEntity<String> changeProduct(@PathVariable int id_product, @RequestBody Product product){
		logger.info("update product: {}\n{}", id_product, product);
		String updateQuery = "UPDATE \"Product\" SET category_number = ?, product_name = ?, " +
				"characteristics = ? WHERE id_product = ?;";
		try
		{
			int aff = jdbcTemplate.update(updateQuery, product.getCategory_number(), product.getProduct_name(),
					product.getCharacteristics(), id_product);
			if (aff > 0)return ResponseEntity.ok("Changed product " + product);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such product");
		}catch (DataIntegrityViolationException e){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad product format");
		}

	}
	@PutMapping("/admin/store_products/{UPC}")
	public ResponseEntity<String> changeStore_Product(@PathVariable String UPC, @RequestBody Store_Product storeProduct){
		logger.info("update store product: {}\n{}", UPC, storeProduct);
		String updateQuery = "UPDATE \"Store_Product\" SET UPC_prom = ?, id_product = ?, " +
				"products_number = ?, selling_price = ?, promotional_product = ? " +
				"WHERE UPC = ?;";
		try {
			int aff = jdbcTemplate.update(updateQuery, storeProduct.getUPC_prom(), storeProduct.getId_product(),
					storeProduct.getProducts_number(), storeProduct.getSelling_price(),
					storeProduct.isPromotional_product(), UPC);
			if (aff > 0)return ResponseEntity.ok("Changed sp " + storeProduct);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such sp");
		}catch (DataIntegrityViolationException e){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such prom sp");
		}


	}
	@GetMapping("/admin/employees/served_all_customers")
	public ResponseEntity<List<Map<String, Object>>> getEmployeesWhoServedAllCustomers(){
		logger.info("get very active employees");
		return ResponseEntity.ok(jdbcTemplate.queryForList(
				"select *,( SELECT COUNT(*) FROM \"Sale\" s WHERE s.check_number IN ( SELECT check_number " +
						"FROM \"Check\" WHERE id_employee = e.id_employee)) " +
						"from \"Employee\" e " +
						"where not exists(" +
						"select card_number from \"Check\" ch " +
						"where card_number is not null and not exists(" +
						"select * from \"Check\" " +
						"where card_number is not null and card_number = ch.card_number and id_employee = e.id_employee)) and " +
						"e.id_employee in (select id_employee from \"Check\");")
		);
	}

	@GetMapping("/admin/customer_cards/tried_everything/{city}")
	public ResponseEntity<List<Map<String,Object>>> getCustomer_CardsWhoTriedEveryCategory(@PathVariable String city){
//		jdbcTemplate.execute("PREPARE cityPlan (text) AS " +
//				"SELECT * FROM \"Customer_Card\" cc WHERE " +
//				"cc.city = $1 AND NOT EXISTS (" +
//				"SELECT * FROM \"Category\" c WHERE " +
//				"NOT EXISTS (" +
//				"SELECT * FROM \"Product\" p WHERE " +
//				"p.category_number = c.category_number " +
//				"AND EXISTS (" +
//				"SELECT * FROM \"Store_Product\" sp WHERE " +
//				"sp.id_product = p.id_product " +
//				"AND EXISTS (" +
//				"SELECT * FROM \"Sale\" s WHERE " +
//				"s.UPC = sp.UPC AND s.check_number IN (" +
//				"SELECT ch.check_number FROM \"Check\" ch WHERE " +
//				"ch.card_number = cc.card_number))))); ");
//		return ResponseEntity.ok(jdbcTemplate.queryForList(
//						"EXECUTE cityPlan(?);",city
//		));
		logger.info("get customers who tried everything");
		return ResponseEntity.ok(
				jdbcTemplate.queryForList(
						"SELECT * FROM \"Customer_Card\" cc WHERE " +
								"cc.city = ? AND NOT EXISTS (" +
								"SELECT * FROM \"Category\" c WHERE " +
								"NOT EXISTS (" +
								"SELECT * FROM \"Product\" p WHERE " +
								"p.category_number = c.category_number " +
								"AND EXISTS (" +
								"SELECT * FROM \"Store_Product\" sp WHERE " +
								"sp.id_product = p.id_product " +
								"AND EXISTS (" +
								"SELECT * FROM \"Sale\" s WHERE " +
								"s.UPC = sp.UPC AND s.check_number IN (" +
								"SELECT ch.check_number FROM \"Check\" ch WHERE " +
								"ch.card_number = cc.card_number))))); ", city)
				);
	}

	@GetMapping("/admin/store_products/strange_statistic")
	public ResponseEntity<List<Map<String,Object>>> getStrangeStatistic(){
		logger.info("get statistics");
		return ResponseEntity.ok(
				jdbcTemplate.queryForList(
						"SELECT c.category_number, c.category_name, res.price_range, res.total_sold\n" +
								"FROM \"Category\" c\n" +
								"INNER JOIN (\n" +
								"  SELECT c.category_number,\n" +
								"    CASE\n" +
								"      WHEN sp.selling_price < (0.25 * max_price.max_price) THEN 'Third'\n" +
								"      WHEN sp.selling_price > (0.75 * max_price.max_price) THEN 'First'\n" +
								"      ELSE 'Second'\n" +
								"    END AS price_range,\n" +
								"    SUM(product_number) AS total_sold\n" +
								"  FROM \"Category\" c\n" +
								"  INNER JOIN \"Product\" p ON c.category_number = p.category_number\n" +
								"  INNER JOIN \"Store_Product\" sp ON p.id_product = sp.id_product\n" +
								"  INNER JOIN \"Sale\" s ON sp.UPC = s.UPC\n" +
								"  INNER JOIN (\n" +
								"    SELECT c.category_number, MAX(sp.selling_price) AS max_price\n" +
								"    FROM \"Category\" c\n" +
								"    INNER JOIN \"Product\" p ON c.category_number = p.category_number\n" +
								"    INNER JOIN \"Store_Product\" sp ON p.id_product = sp.id_product\n" +
								"    GROUP BY c.category_number\n" +
								"  ) AS max_price ON c.category_number = max_price.category_number\n" +
								"  GROUP BY c.category_number, price_range\n" +
								") AS res ON c.category_number = res.category_number\n" +
								"ORDER BY c.category_number, res.price_range;"
				)
		);
	}
	@GetMapping("/admin/employees")
	public ResponseEntity<List<Map<String,Object>>> getEmployees(){
		logger.info("get employees");
		List<Map<String, Object>> employees = jdbcTemplate.queryForList("SELECT * " +
				"FROM \"Employee\" " +
				"ORDER BY empl_surname;");
		return ResponseEntity.ok(employees);
	}
	@GetMapping("/admin/employees/cashiers")
	public ResponseEntity<List<Map<String,Object>>> getCashiers(){
		logger.info("get cashiers");
		List<Map<String,Object>> cashiers = jdbcTemplate.queryForList("SELECT * \n" +
				"FROM \"Employee\" " +
				"WHERE empl_role ilike \'касирка\' " +
				"ORDER BY empl_surname;");
		return ResponseEntity.ok(cashiers);
	}
	@GetMapping("/user/customer_cards")
	public ResponseEntity<List<Map<String,Object>>> getCustomer_Cards(){
		logger.info("get customer cards");
		List<Map<String,Object>> cards = jdbcTemplate.queryForList("SELECT * " +
				"FROM \"Customer_Card\" " +
				"ORDER BY cust_surname;");
		return ResponseEntity.ok(cards);
	}
	@GetMapping("/user/categories")
	public ResponseEntity<List<Map<String,Object>>> getCategories(){
		logger.info("get categories");
		List<Map<String,Object>> categories = jdbcTemplate.queryForList("SELECT * " +
				"FROM \"Category\" " +
				"ORDER BY category_name;");

		return ResponseEntity.ok(categories);
	}
	@GetMapping("/user/products")
	public ResponseEntity<List<Map<String,Object>>> getProducts(){
		logger.info("get products");
		List<Map<String,Object>> products = jdbcTemplate.queryForList("SELECT * " +
				"FROM \"Product\" p INNER JOIN \"Category\" c ON c.category_number = p.category_number " +
				"ORDER BY product_name;");
		return ResponseEntity.ok(products);
	}
	@GetMapping("/admin/employees/with_surname/{surname}")
	public ResponseEntity<List<Map<String, Object>>> getEmployeesBySurname(@PathVariable String surname) {
		logger.info("get employees with surname: {}", surname);
		String query = "SELECT * FROM \"Employee\" WHERE empl_surname = ?";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, surname);
		return ResponseEntity.ok(rows);
	}
	@GetMapping("/admin/customer_cards/{rate}")
	public ResponseEntity<List<Map<String,Object>>> getCustomersWithRate(@PathVariable int rate){
		logger.info("get customer cards with rate: {}", rate);
		String query = "SELECT * FROM \"Customer_Card\" WHERE percent = ? ORDER BY cust_surname";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, rate);
		return ResponseEntity.ok(rows);
	}
	@GetMapping("/user/products/{id}")
	public ResponseEntity<List<Map<String,Object>>> getProduct(@PathVariable int id){
		logger.info("get product: {}", id);
		String query = "SELECT * FROM \"Product\" p INNER JOIN \"Category\" c ON c.category_number = p.category_number" +
				" WHERE id_product = ?;";
		List<Map<String , Object>> result = jdbcTemplate.queryForList(query, id);
		return ResponseEntity.ok(result);
	}
	@GetMapping("/user/products/by_category/{category_number}")
	public ResponseEntity<List<Map<String, Object>>> getProductsByCategory(@PathVariable int category_number) {
		logger.info("get products with category: {}", category_number);
		String query = "SELECT * FROM \"Product\" p inner join \"Category\" c on c.category_number = p.category_number " +
				"and p.category_number = ? " +
				"ORDER BY product_name";

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, category_number);
		return ResponseEntity.ok(rows);
	}
	@GetMapping("/user/store_products/by_products_number")
	public ResponseEntity<List<Map<String, Object>>> getProductsAndStoreNumbers() {
		logger.info("get store products ordered by number");
		String query = "SELECT p.product_name, sp.* " +
				"FROM \"Store_Product\" sp " +
				"INNER JOIN \"Product\" p ON sp.id_product = p.id_product " +
				"INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
				"ORDER BY sp.products_number";

		List<Map<String, Object>> result = jdbcTemplate.queryForList(query);

		return ResponseEntity.ok(result);
	}
	@GetMapping("/user/store_products")
	public ResponseEntity<List<Map<String , Object>>> getAllStore_Products(){
		logger.info("get store products");
		return ResponseEntity.ok(jdbcTemplate.queryForList("SELECT p.product_name, sp.* " +
				"FROM \"Store_Product\" sp " +
				"INNER JOIN \"Product\" p ON sp.id_product = p.id_product " +
				"INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
				"ORDER BY sp.products_number"));
	}

	@GetMapping("/user/store_products/by_product_name")
	public ResponseEntity<List<Map<String, Object>>> getProductsAndStoreNumbersBuName() {
		logger.info("get store products ordered by name");
		String query = "SELECT p.product_name, sp.*" +
				"FROM \"Store_Product\" sp " +
				"INNER JOIN \"Product\" p ON sp.id_product = p.id_product " +
				"INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
				"ORDER BY p.product_name";

		List<Map<String, Object>> result = jdbcTemplate.queryForList(query);

		return ResponseEntity.ok(result);
	}


	@GetMapping("/user/store_products/promo")
	public ResponseEntity<List<Map<String,Object>>> getPromoProducts(){
		logger.info("get promo products");
		String query = "SELECT * FROM \"Product\" p " +
				"INNER JOIN \"Store_Product\" sp " +
				"ON p.id_product = sp.id_product AND sp.promotional_product = true " +
				"INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
				"ORDER BY sp.products_number, p.product_name;";
		List<Map<String,Object>> result = jdbcTemplate.queryForList(query);
		return ResponseEntity.ok(result);
	}
	@GetMapping("/user/store_products/promo/by_products_number")
	public ResponseEntity<List<Map<String,Object>>> getPromoProductsByProductsNumber(){
		logger.info("get promo products");
		String query = "SELECT * FROM \"Product\" p " +
				"INNER JOIN \"Store_Product\" sp " +
				"ON p.id_product = sp.id_product AND sp.promotional_product = true " +
				"INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
				"ORDER BY sp.products_number";
		List<Map<String,Object>> result = jdbcTemplate.queryForList(query);
		return ResponseEntity.ok(result);
	}
	@GetMapping("/user/store_products/promo/by_product_name")
	public ResponseEntity<List<Map<String,Object>>> getPromoProductsByProductName(){
		logger.info("get promo products");
		String query = "SELECT * FROM \"Product\" p " +
				"INNER JOIN \"Store_Product\" sp " +
				"ON p.id_product = sp.id_product AND sp.promotional_product = true " +
				"INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
				"ORDER BY p.product_name;";
		List<Map<String,Object>> result = jdbcTemplate.queryForList(query);
		return ResponseEntity.ok(result);
	}
	@GetMapping("/user/store_products/not_promo")
	public ResponseEntity<List<Map<String,Object>>> getNonPromoProducts(){
		logger.info("get non-promo products");
		String query = "SELECT * FROM \"Product\" p " +
				"INNER JOIN \"Store_Product\" sp " +
				"ON p.id_product = sp.id_product AND sp.promotional_product = false " +
				"INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
				"ORDER BY sp.products_number, p.product_name;";
		List<Map<String,Object>> result = jdbcTemplate.queryForList(query);
		return ResponseEntity.ok(result);
	}
	@GetMapping("/user/store_products/not_promo/by_products_number")
	public ResponseEntity<List<Map<String,Object>>> getNonPromoProductsByProductsNumber(){
		logger.info("get non-promo products");
		String query = "SELECT * FROM \"Product\" p " +
				"INNER JOIN \"Store_Product\" sp " +
				"ON p.id_product = sp.id_product AND sp.promotional_product = false " +
				"INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
				"ORDER BY sp.products_number, p.product_name;";
		List<Map<String,Object>> result = jdbcTemplate.queryForList(query);
		return ResponseEntity.ok(result);
	}
	@GetMapping("/user/store_products/not_promo/by_product_name")
	public ResponseEntity<List<Map<String,Object>>> getNonPromoProductsByProductName(){
		logger.info("get non-promo products");
		String query = "SELECT * FROM \"Product\" p " +
				"INNER JOIN \"Store_Product\" sp " +
				"ON p.id_product = sp.id_product AND sp.promotional_product = false " +
				"INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
				"ORDER BY p.product_name;";
		List<Map<String,Object>> result = jdbcTemplate.queryForList(query);
		return ResponseEntity.ok(result);
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
	@GetMapping("/user/products/count/{id_product}/{left_date}/{right_date}")
	public ResponseEntity<Integer> getCountOfProductSold(@PathVariable int id_product, @PathVariable Date left_date, @PathVariable Date right_date){
		logger.info("get number of product {} sold from {} to {}", id_product, left_date.toString(), right_date.toString());
		String query = "SELECT SUM(s.product_number) " +
				"FROM \"Product\" p INNER JOIN \"Store_Product\" sp ON p.id_product = ? AND p.id_product = sp.id_product " +
				"INNER JOIN \"Sale\" s ON s.UPC = sp.UPC " +
				"INNER JOIN \"Check\" ch ON s.check_number = ch.check_number AND ch.print_date::date >= ? AND ch.print_date::date <= ?;";
		try{
			Integer result = jdbcTemplate.queryForObject(query, Integer.class, id_product, left_date,right_date);
			if (result == null)result =0;
			return ResponseEntity.ok(result);
		}catch (EmptyResultDataAccessException e){
			return ResponseEntity.ok(0);
		}

	}
	@GetMapping("/user/products/with_name/{name}")
	public ResponseEntity<List<Map<String, Object>>> getProductsByName(@PathVariable String name){
		logger.info("get products with name {}", name);
		String query = "SELECT * FROM \"Product\" p " +
				"INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
				"WHERE product_name = ?;";
		List<Map<String,Object>> result = jdbcTemplate.queryForList(query,name);
		return ResponseEntity.ok(result);
	}
	@GetMapping("/user/customer_cards/with_surname/{surname}")
	public ResponseEntity<List<Map<String, Object>>> getCustomersBySurname(@PathVariable String surname){
		logger.info("get customer cards with surname {}", surname);
		String query = "SELECT * FROM \"Customer_Card\" WHERE cust_surname = ?;";
		List<Map<String, Object>> result = jdbcTemplate.queryForList(query, surname);
		return ResponseEntity.ok(result);
	}
	@GetMapping("/user/employees/{id_employee}")
	public ResponseEntity<Employee> getEmployeeById(@PathVariable String id_employee){
		logger.info("get employee {}", id_employee);
		String query = "SELECT * FROM \"Employee\" WHERE id_employee = ?;";
		try {
			Employee employee = jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(Employee.class), id_employee);
			return ResponseEntity.ok(employee);
		} catch (EmptyResultDataAccessException e) {
			return ResponseEntity.notFound().build();
		}
	}
	@GetMapping("/user/store_products/{UPC}")
	public ResponseEntity<Map<String, Object>> getByUPC(@PathVariable String UPC){
		logger.info("get store product {}", UPC);
		String query = "SELECT * " +
				"FROM \"Product\" p JOIN \"Store_Product\" sp ON p.id_product = sp.id_product " +
				"WHERE sp.UPC = ?;";
		try{
			Map<String,Object> result = jdbcTemplate.queryForMap(query, UPC);
			return ResponseEntity.ok(result);
		}catch (EmptyResultDataAccessException e){
			return ResponseEntity.notFound().build();
		}

	}
}
