package com.supermarket.back;

public class EmployeeAndUser {
    private Employee employee;
    private MyUser user;

    public EmployeeAndUser() {
    }

    public EmployeeAndUser(Employee employee, MyUser user) {
        this.employee = employee;
        this.user = user;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "EmployeeAndUser{" +
                "employee=" + employee +
                ", user=" + user +
                '}';
    }
}
