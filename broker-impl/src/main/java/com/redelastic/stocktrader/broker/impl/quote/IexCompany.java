package com.redelastic.stocktrader.broker.impl.quote;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.*;

@JsonDeserialize(builder = IexCompany.IexCompanyBuilder.class)
@Builder
@Data
class IexCompany {
    String symbol;
    String companyName;
    String exchange;
    String industry;
    String website;
    String description;
    String CEO;
    String securityName;
    String issueType;
    String sector;
    String primarySicCode;
    String employees;
    String address;
    String address2;
    String state;
    String city;
    String zip;
    String country;
    String phone;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class IexCompanyBuilder {
    }
}