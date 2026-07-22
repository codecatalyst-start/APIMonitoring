package com.automate.apimonitor.service;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public String findOrder(Integer id) {

        if(id == 1)
            throw new NullPointerException("Customer object is null");

        if(id == 2)
            throw new IllegalArgumentException("Invalid Order Id");

        if(id == 3)
            throw new RuntimeException("Database Connection Failed");

        if(id == 4)
            throw new ArithmeticException("Divide by zero");

        return "Order Found";
    }

}
