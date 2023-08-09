package com.supermarket.back;

import java.math.BigDecimal;

public class Sale {
    private String UPC, check_number;
    private int product_number;
    private BigDecimal selling_price;

    public Sale(String UPC, String check_number, int product_number, BigDecimal selling_price) {
        this.UPC = UPC;
        this.check_number = check_number;
        this.product_number = product_number;
        this.selling_price = selling_price;
    }

    public String getUPC() {
        return UPC;
    }

    public void setUPC(String UPC) {
        this.UPC = UPC;
    }

    public String getCheck_number() {
        return check_number;
    }

    public void setCheck_number(String check_number) {
        this.check_number = check_number;
    }

    public int getProduct_number() {
        return product_number;
    }

    public void setProduct_number(int product_number) {
        this.product_number = product_number;
    }

    public BigDecimal getSelling_price() {
        return selling_price;
    }

    public void setSelling_price(BigDecimal selling_price) {
        this.selling_price = selling_price;
    }

    @Override
    public String toString() {
        return "Sale{" +
                "UPC='" + UPC + '\'' +
                ", check_number='" + check_number + '\'' +
                ", product_number=" + product_number +
                ", selling_price=" + selling_price +
                '}';
    }
}
