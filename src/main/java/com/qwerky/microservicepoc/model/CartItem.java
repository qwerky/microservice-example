package com.qwerky.microservicepoc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

public class CartItem {

    private String id;
    private String name;
    private int qty;
    @JsonProperty("unit_cost")
    private int unitCost;

    @Id
    public String getId() {
        return id;
    }

    @JsonProperty("total_cost")
    public int getCost() {
        return qty*unitCost;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(int unitCost) {
        this.unitCost = unitCost;
    }
}
