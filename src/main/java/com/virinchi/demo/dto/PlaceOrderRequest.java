package com.virinchi.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public class PlaceOrderRequest {
    private String userName;
    private String userEmail;
    private List<Item> items;

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        private String id;      // product SKU or local id
        private String name;
        private BigDecimal price;
        private Integer qty;
        private String image;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getQty() { return qty; }
        public void setQty(Integer qty) { this.qty = qty; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
    }
}

