package com.redelastic.stocktrader.broker.api;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import org.pcollections.PSequence;

@Value
@Builder
public class DetailedQuotesResponse {

    @NonNull PSequence<DetailedQuote> detailedQuotes;

    @Value
    class DetailedQuote {
        @NonNull String symbol;
        @NonNull Integer shares;
        @NonNull CompanyData company;

        @Value
        class CompanyData {
            @NonNull String companyName;
            @NonNull String exchange;
            @NonNull String industry;
            @NonNull String website;
            @NonNull String description;
            @NonNull String CEO;
            @NonNull String employees;
            @NonNull String address;
            @NonNull String address2;
            @NonNull String state;
            @NonNull String city;
            @NonNull String zip;
            @NonNull String country;
            @NonNull String phone;
        }
    }
}
