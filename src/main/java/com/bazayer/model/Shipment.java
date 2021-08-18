package com.bazayer.model;

import com.opencsv.bean.CsvBindByName;
import java.util.Objects;
import lombok.Data;

@Data
public class Shipment {

    @CsvBindByName(column = "recipientFirstName")
    private String firstName;

    @CsvBindByName(column = "recipientLastName")
    private String lastName;

    @CsvBindByName(column = "recipientFullName")
    private String recipientFullName;

    @CsvBindByName(column = "recipientPhoneNumber")
    private String recipientPhoneNumber;

    @CsvBindByName(column = "address")
    private String address;

    @CsvBindByName(column = "addressCity")
    private String addressCity;

    @CsvBindByName(column = "addressState")
    private String addressState;

    @CsvBindByName(column = "addressZip")
    private String addressZip;

    @CsvBindByName(column = "trackingNumber")
    private String trackingNumber;

    @CsvBindByName(column = "deliveryCompany")
    private String deliveryCompany;

    private String trackingUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shipment)) return false;
        Shipment that = (Shipment) o;
        return Objects.equals(trackingNumber, that.trackingNumber) && Objects.equals(recipientPhoneNumber, that.recipientPhoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackingNumber, recipientPhoneNumber);
    }
}
