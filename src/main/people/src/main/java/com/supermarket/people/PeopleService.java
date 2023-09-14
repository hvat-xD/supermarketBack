package com.supermarket.people;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.zip.DataFormatException;

@RestController
@CrossOrigin
@RequestMapping("/api")
@EnableAutoConfiguration
public class PeopleService {
   // public static final Logger logger = LoggerFactory.getLogger(PeopleService.class);
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    UserDetailsManager users;
    @Autowired
    PasswordEncoder passwordEncoder;


    @GetMapping("/admin/employees")
    public ResponseEntity<List<Map<String,Object>>> getEmployees(){
        //logger.info("get employees");
        List<Map<String, Object>> employees = jdbcTemplate.queryForList("SELECT * " +
                "FROM \"Employee\" " +
                "ORDER BY empl_surname;");
        return ResponseEntity.ok(employees);
    }
    @GetMapping("/admin/employees/cashiers")
    public ResponseEntity<List<Map<String,Object>>> getCashiers(){
        //logger.info("get cashiers");
        List<Map<String,Object>> cashiers = jdbcTemplate.queryForList("SELECT * \n" +
                "FROM \"Employee\" " +
                "WHERE empl_role ilike \'касирка\' " +
                "ORDER BY empl_surname;");
        return ResponseEntity.ok(cashiers);
    }
    @GetMapping("/user/products/count/{id_product}/{left_date}/{right_date}")
    public ResponseEntity<Integer> getCountOfProductSold(@PathVariable int id_product, @PathVariable Date left_date, @PathVariable Date right_date){
        //logger.info("get number of product {} sold from {} to {}", id_product, left_date.toString(), right_date.toString());
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
    @GetMapping("/user/customer_cards/with_surname/{surname}")
    public ResponseEntity<List<Map<String, Object>>> getCustomersBySurname(@PathVariable String surname){
        //logger.info("get customer cards with surname {}", surname);
        String query = "SELECT * FROM \"Customer_Card\" WHERE cust_surname = ?;";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(query, surname);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/user/employees/{id_employee}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id_employee){
        //logger.info("get employee {}", id_employee);
        String query = "SELECT * FROM \"Employee\" WHERE id_employee = ?;";
        try {
            Employee employee = jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(Employee.class), id_employee);
            return ResponseEntity.ok(employee);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/user/products/with_name/{name}")
    public ResponseEntity<List<Map<String, Object>>> getProductsByName(@PathVariable String name){
        //logger.info("get products with name {}", name);
        String query = "SELECT * FROM \"Product\" p " +
                "INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
                "WHERE product_name = ?;";
        List<Map<String,Object>> result = jdbcTemplate.queryForList(query,name);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/admin/employees/with_surname/{surname}")
    public ResponseEntity<List<Map<String, Object>>> getEmployeesBySurname(@PathVariable String surname) {
        //logger.info("get employees with surname: {}", surname);
        String query = "SELECT * FROM \"Employee\" WHERE empl_surname = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, surname);
        return ResponseEntity.ok(rows);
    }
    @GetMapping("/admin/customer_cards/{rate}")
    public ResponseEntity<List<Map<String,Object>>> getCustomersWithRate(@PathVariable int rate){
        //logger.info("get customer cards with rate: {}", rate);
        String query = "SELECT * FROM \"Customer_Card\" WHERE percent = ? ORDER BY cust_surname";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, rate);
        return ResponseEntity.ok(rows);
    }
    @GetMapping("/user/customer_cards")
    public ResponseEntity<List<Map<String,Object>>> getCustomer_Cards(){
        //logger.info("get customer cards");
        List<Map<String,Object>> cards = jdbcTemplate.queryForList("SELECT * " +
                "FROM \"Customer_Card\" " +
                "ORDER BY cust_surname;");
        return ResponseEntity.ok(cards);
    }
    @GetMapping("/admin/employees/served_all_customers")
    public ResponseEntity<List<Map<String, Object>>> getEmployeesWhoServedAllCustomers(){
        //logger.info("get very active employees");
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
        //logger.info("get customers who tried everything");
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

    @PutMapping("/admin/employees/{id_employee}")
    public ResponseEntity<String> updateEmployee(@PathVariable String id_employee, @RequestBody Employee employee){
        //logger.info("update employee: {}\n{}",id_employee, employee );

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
        //logger.info("update customer card: {}\n{}", card_number, customerCard);
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
    @DeleteMapping("/user/customer_cards/{id}")
    public ResponseEntity<String> deleteCustomer_Card(@PathVariable String id){
        //logger.info("delete customer card: {}", id);
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
        //logger.info("get number of sold products for each employee with price");
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
    @DeleteMapping("/admin/employees/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable String id){
        //logger.info("delete employee: {}", id);
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
    @GetMapping("/user/users/{username}")
    public ResponseEntity<UserDetails> getUser(@PathVariable String username){
        //logger.info("get user {}", username);
        return ResponseEntity.ok(users.loadUserByUsername(username));
    }
    @PostMapping("/admin/users")
    public ResponseEntity<MyUser> createUser(@RequestBody MyUser user){

        //logger.info("create user {}", user);
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
        //logger.info("change user {}", username);
        password = new String(Base64.getDecoder().decode(password));
        oldpassword = new String(Base64.getDecoder().decode(oldpassword));
        //logger.info("change from {} to {}", oldpassword, password);

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
        //logger.info("delete user {}", username);
        users.deleteUser(username);
        return ResponseEntity.ok(username);
    }
    @PostMapping("/user/customer_cards")
    public ResponseEntity<String> createCustomer_Card(@RequestBody Customer_Card customerCard){
        //logger.info("post customer card: {}", customerCard);
        String insertQuery = "INSERT INTO \"Customer_Card\" ( cust_surname, cust_name, cust_patronymic, phone_number, city, street, zip_code, percent) " +
                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(insertQuery, customerCard.getCust_surname(), customerCard.getCust_name(),
                customerCard.getCust_patronymic(), customerCard.getPhone_number(), customerCard.getCity(),
                customerCard.getStreet(), customerCard.getZip_code(), customerCard.getPercent());

        return ResponseEntity.status(HttpStatus.CREATED).body("Customer created successfully " + customerCard);
    }
    @PostMapping("/admin/employees")
    public ResponseEntity<String> createEmployee(@RequestBody EmployeeAndUser employeeAndUser){
        Employee employee = employeeAndUser.getEmployee();
        MyUser user = employeeAndUser.getUser();

        //logger.info("post employee: {}\n{}", employee,user);
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
}
