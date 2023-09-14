package com.supermarket.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api")
@EnableAutoConfiguration
public class ProductService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @PostMapping("/admin/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Category category){
        //logger.info("post category: {}", category);
        String insertQuery = "INSERT INTO \"Category\" (category_name) VALUES (?)";
        jdbcTemplate.update(insertQuery, category.getCategory_name());
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PostMapping("/admin/products")
    public ResponseEntity<String> createProduct(@RequestBody Product product){
        //logger.info("post product: {}", product);
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
        //logger.info("post store product: {}", storeProduct);
        List<Map<String,Object>> checking = jdbcTemplate.queryForList("select * from \"Store_Product\" where id_product = ?",
                storeProduct.getId_product());
        if (checking.size() >= 2 || (checking .size() == 0 && storeProduct.isPromotional_product())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No place or non-promotional product" + storeProduct);
        }
        if (checking.size() == 1){
            if (storeProduct.isPromotional_product()){
                storeProduct.setSelling_price(storeProduct.getSelling_price().multiply(new BigDecimal(0.8)));
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This product must be promotional " + storeProduct);
            }
        }
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

        return ResponseEntity.status(HttpStatus.CREATED).body("Created store product " + storeProduct);
    }
    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable int id){
        //logger.info("delete category: {}", id);
        String deleteQuery = "DELETE FROM \"Category\" WHERE category_number = ?;";
        //System.out.println(id);
        int rowsAffected = jdbcTemplate.update(deleteQuery, id);
        if (rowsAffected > 0) {
            //System.out.println("Deleted category");
            return ResponseEntity.ok("Category deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
        }
    }

    @DeleteMapping("/admin/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id){
        //logger.info("delete product: {}", id);
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
    @GetMapping("/admin/store_products/strange_statistic")
    public ResponseEntity<List<Map<String,Object>>> getStrangeStatistic(){
        //logger.info("get statistics");
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
    @GetMapping("/user/products/{id}")
    public ResponseEntity<List<Map<String,Object>>> getProduct(@PathVariable int id){
        //logger.info("get product: {}", id);
        String query = "SELECT * FROM \"Product\" p INNER JOIN \"Category\" c ON c.category_number = p.category_number" +
                " WHERE id_product = ?;";
        List<Map<String , Object>> result = jdbcTemplate.queryForList(query, id);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/user/products/by_category/{category_number}")
    public ResponseEntity<List<Map<String, Object>>> getProductsByCategory(@PathVariable int category_number) {
        //logger.info("get products with category: {}", category_number);
        String query = "SELECT * FROM \"Product\" p inner join \"Category\" c on c.category_number = p.category_number " +
                "and p.category_number = ? " +
                "ORDER BY product_name";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, category_number);
        return ResponseEntity.ok(rows);
    }
    @GetMapping("/user/store_products/by_products_number")
    public ResponseEntity<List<Map<String, Object>>> getProductsAndStoreNumbers() {
        //logger.info("get store products ordered by number");
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
        //logger.info("get store products");
        return ResponseEntity.ok(jdbcTemplate.queryForList("SELECT p.product_name, sp.* " +
                "FROM \"Store_Product\" sp " +
                "INNER JOIN \"Product\" p ON sp.id_product = p.id_product " +
                "INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
                "ORDER BY sp.products_number"));
    }

    @GetMapping("/user/store_products/by_products_name")
    public ResponseEntity<List<Map<String, Object>>> getProductsAndStoreNumbersBuName() {
        //logger.info("get store products ordered by name");
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
        //logger.info("get promo products");
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
        //logger.info("get promo products");
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
        //logger.info("get promo products");
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
        //logger.info("get non-promo products");
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
        //logger.info("get non-promo products");
        String query = "SELECT * FROM \"Product\" p " +
                "INNER JOIN \"Store_Product\" sp " +
                "ON p.id_product = sp.id_product AND sp.promotional_product = false " +
                "INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
                "ORDER BY sp.products_number, p.product_name;";
        List<Map<String,Object>> result = jdbcTemplate.queryForList(query);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/user/store_products/{UPC}")
    public ResponseEntity<Map<String, Object>> getByUPC(@PathVariable String UPC){
        //logger.info("get store product {}", UPC);
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
    @GetMapping("/user/store_products/not_promo/by_product_name")
    public ResponseEntity<List<Map<String,Object>>> getNonPromoProductsByProductName(){
        //logger.info("get non-promo products");
        String query = "SELECT * FROM \"Product\" p " +
                "INNER JOIN \"Store_Product\" sp " +
                "ON p.id_product = sp.id_product AND sp.promotional_product = false " +
                "INNER JOIN \"Category\" c ON p.category_number = c.category_number " +
                "ORDER BY p.product_name;";
        List<Map<String,Object>> result = jdbcTemplate.queryForList(query);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/user/categories")
    public ResponseEntity<List<Map<String,Object>>> getCategories(){
        //logger.info("get categories");
        List<Map<String,Object>> categories = jdbcTemplate.queryForList("SELECT * " +
                "FROM \"Category\" " +
                "ORDER BY category_name;");

        return ResponseEntity.ok(categories);
    }
    @GetMapping("/user/products")
    public ResponseEntity<List<Map<String,Object>>> getProducts(){
        //logger.info("get products");
        List<Map<String,Object>> products = jdbcTemplate.queryForList("SELECT * " +
                "FROM \"Product\" p INNER JOIN \"Category\" c ON c.category_number = p.category_number " +
                "ORDER BY product_name;");
        return ResponseEntity.ok(products);
    }
    @PutMapping("/admin/categories/{category_number}")
    public ResponseEntity<String> changeCategory(@PathVariable int category_number, @RequestBody Category category){
        //logger.info("update category: {}\n{}", category_number, category);
        String updateQuery = "UPDATE \"Category\" SET category_name = ? " +
                "WHERE category_number = ?;";
        int aff = jdbcTemplate.update(updateQuery, category.getCategory_name(), category_number);
        if (aff> 0)return ResponseEntity.ok("Changed category " + category);
        else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such category");
    }
    @PutMapping("/admin/products/{id_product}")
    public ResponseEntity<String> changeProduct(@PathVariable int id_product, @RequestBody Product product){
        //logger.info("update product: {}\n{}", id_product, product);
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
        //logger.info("update store product: {}\n{}", UPC, storeProduct);
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
    @GetMapping("/user/categories/best_sellers")
    public ResponseEntity<List<Map<String, Object>>> getTodaySBestSellingCategories(){
        //logger.info("get best selling categories");
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

    @GetMapping("user/categories/get_sold_today/{category_number}")
    public ResponseEntity<Integer> getNumberOfSoldProductsForCategory(@PathVariable int category_number){
        //logger.info("get number of products sold today for category {}", category_number);
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
        //logger.info("delete store product: {}", id);
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
}
