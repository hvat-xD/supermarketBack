package com.supermarket.back;

public class Product {
    private int id_product, category_number;
    private String product_name, characteristics;

    public Product(int id_product, int category_number, String product_name, String characteristics) {
        this.id_product = id_product;
        this.category_number = category_number;
        this.product_name = product_name;
        this.characteristics = characteristics;
    }

    public int getId_product() {
        return id_product;
    }

    public void setId_product(int id_product) {
        this.id_product = id_product;
    }

    public int getCategory_number() {
        return category_number;
    }

    public void setCategory_number(int category_number) {
        this.category_number = category_number;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id_product=" + id_product +
                ", category_number=" + category_number +
                ", product_name='" + product_name + '\'' +
                ", characteristics='" + characteristics + '\'' +
                '}';
    }
}
