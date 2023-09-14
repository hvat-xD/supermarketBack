package com.supermarket.sale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@CrossOrigin
@RequestMapping("/api")
@EnableAutoConfiguration
public class SaleService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @PostMapping("/user/checks")
    public ResponseEntity<String> createCheck(@RequestBody CheckTransferObject transferObject){
        //logger.info("post check: {}", transferObject);
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
        //logger.info("delete check: {}", id);
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
        //logger.info("delete all checks of employee {}", id_employee);
        int rowsAffected = jdbcTemplate.update("delete from \"Check\" where id_employee = ?", id_employee);
        if (rowsAffected > 0) {
            return ResponseEntity.ok("Checks deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Checks not found");
        }
    }








    @GetMapping("/user/checks/{id_employee}/{left_date}/{right_date}")
    public ResponseEntity<List<Map<String,Object>>> getChecksWithTimeStamp(@PathVariable String id_employee, @PathVariable Date left_date, @PathVariable Date right_date){
        //logger.info("get checks of {} from {} to {}", id_employee,left_date.toString(),right_date.toString());

        String query = "SELECT * FROM \"Check\" " +
                "WHERE id_employee = ? AND print_date::date >= ? AND print_date::date <= ?;";
        List<Map<String, Object>>result = jdbcTemplate.queryForList(query, id_employee,left_date,right_date);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/user/sales/{check_number}")
    public ResponseEntity<List<Map<String, Object>>> getSalesOfCheck(@PathVariable String check_number){
        //logger.info("get sales from check: {}", check_number);
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
        //logger.info("get checks from {} to {}", left_date, right_date);
        String query = "SELECT * FROM \"Check\" " +
                "WHERE print_date::date >= ? AND print_date::date <= ?;";
        List<Map<String, Object>>result = jdbcTemplate.queryForList(query, left_date,right_date);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/admin/checks/price/{id_employee}/{left_date}/{right_date}")
    public ResponseEntity<BigDecimal> getSumOfChecksOfEmployee(@PathVariable String id_employee,@PathVariable Date left_date, @PathVariable Date right_date){
        //logger.info("get sum of checks of {} from {} to {}", id_employee, left_date.toString(), right_date.toString());
        String query = "SELECT SUM(sum_total) " +
                "FROM \"Check\" " +
                "WHERE id_employee = ? AND print_date::date >= ? AND print_date::date <= ?;";
        BigDecimal result = jdbcTemplate.queryForObject(query, BigDecimal.class, id_employee, left_date, right_date);
        return ResponseEntity.ok(Objects.requireNonNullElse(result, BigDecimal.ZERO));
    }
    @GetMapping("/admin/checks/price/all/{left_date}/{right_date}")
    public ResponseEntity<BigDecimal> getSumOfChecksOfEveryone(@PathVariable Date left_date, @PathVariable Date right_date){
        //logger.info("get price of checks from {} to {}", left_date.toString(), right_date.toString());
        String query = "SELECT SUM(sum_total) " +
                "FROM \"Check\" " +
                "WHERE print_date::date >= ? AND print_date::date <= ?;";
        BigDecimal result = jdbcTemplate.queryForObject(query, BigDecimal.class, left_date, right_date);
        return ResponseEntity.ok(Objects.requireNonNullElse(result, BigDecimal.ZERO));
    }
    @GetMapping("/admin/checks")
    public ResponseEntity<List<Map<String, Object>>> getAllChecks(){
        //logger.info("get all checks");
        return ResponseEntity.ok(
                jdbcTemplate.queryForList("SELECT * FROM \"Check\" ORDER BY print_date")
        );
    }
    @GetMapping("/user/checks/{check_number}")
    public ResponseEntity<Check> getCheck(@PathVariable String check_number){
        //logger.info("get check {}", check_number);
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
