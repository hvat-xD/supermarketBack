package com.supermarket.back;

import java.util.ArrayList;

public class CheckTransferObject {
    private Check check;
    private ArrayList<Pair<String, Integer>> products;

    public CheckTransferObject(Check check, ArrayList<Pair<String, Integer>> products) {
        this.check = check;
        this.products = products;
    }

    public Check getCheck() {
        return check;
    }

    public void setCheck(Check check) {
        this.check = check;
    }

    public ArrayList<Pair<String, Integer>> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Pair<String, Integer>> products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return "CheckTransferObject{" +
                "check=" + check +
                ", products=" + products +
                '}';
    }
}
