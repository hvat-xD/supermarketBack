package com.supermarket.back;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Check {
    private String check_number, id_employee, card_number;
    private Timestamp print_date;
    private BigDecimal sum_total;
    private Integer vat;
    public Check(){}

    public Check(String check_number, String id_employee, String card_number, Timestamp print_date, BigDecimal sum_total, Integer vat) {
        this.check_number = check_number;
        this.id_employee = id_employee;
        this.card_number = card_number;
        this.print_date = print_date;
        this.sum_total = sum_total;
        this.vat = vat;
    }

    public Check(String check_number, String id_employee, String card_number, BigDecimal sum_total, Integer vat) {
        this.check_number = check_number;
        this.id_employee = id_employee;
        this.card_number = card_number;
        this.sum_total = sum_total;
        this.vat = vat;
    }

    public void setVat(Integer vat) {
        this.vat = vat;
    }

    public Integer getVat() {
        return vat;
    }

    public String getCheck_number() {
        return check_number;
    }

    public void setCheck_number(String check_number) {
        this.check_number = check_number;
    }

    public String getId_employee() {
        return id_employee;
    }

    public void setId_employee(String id_employee) {
        this.id_employee = id_employee;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public Timestamp getPrint_date() {
        return print_date;
    }

    public void setPrint_date(Timestamp print_date) {
        this.print_date = print_date;
    }

    public BigDecimal getSum_total() {
        return sum_total;
    }

    public void setSum_total(BigDecimal sum_total) {
        this.sum_total = sum_total;
    }





    @Override
    public String toString() {
        return "Check{" +
                "check_number='" + check_number + '\'' +
                ", id_employee='" + id_employee + '\'' +
                ", card_number='" + card_number + '\'' +
                ", print_date=" + print_date +
                ", sum_total=" + sum_total +
                ", vat=" + vat +
                '}';
    }
}
