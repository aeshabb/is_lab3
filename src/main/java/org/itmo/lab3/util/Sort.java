package org.itmo.lab3.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Sort implements Iterable<Sort.Order> {
    
    private final List<Order> orders;
    
    public Sort(Order... orders) {
        this.orders = Arrays.asList(orders);
    }
    
    public Sort(List<Order> orders) {
        this.orders = new ArrayList<>(orders);
    }
    
    public static Sort by(String... properties) {
        List<Order> orders = new ArrayList<>();
        for (String property : properties) {
            orders.add(Order.asc(property));
        }
        return new Sort(orders);
    }
    
    public static Sort by(Order... orders) {
        return new Sort(orders);
    }
    
    public boolean isSorted() {
        return !orders.isEmpty();
    }
    
    @Override
    public Iterator<Order> iterator() {
        return orders.iterator();
    }
    
    public static class Order {
        private final String property;
        private final Direction direction;
        
        private Order(String property, Direction direction) {
            this.property = property;
            this.direction = direction;
        }
        
        public static Order asc(String property) {
            return new Order(property, Direction.ASC);
        }
        
        public static Order desc(String property) {
            return new Order(property, Direction.DESC);
        }
        
        public String getProperty() {
            return property;
        }
        
        public Direction getDirection() {
            return direction;
        }
    }
    
    public enum Direction {
        ASC, DESC
    }
}
