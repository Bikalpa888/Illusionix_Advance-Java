package com.virinchi.demo.repository;

public interface OrderExportView {
    String getOrderNumber();
    String getUserName();
    String getUserEmail();
    String getTotalStr();
    String getStatus();
    String getCreatedAtStr();
}

