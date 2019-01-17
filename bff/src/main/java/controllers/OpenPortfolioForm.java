package controllers;

import com.redelastic.stocktrader.portfolio.api.OpenPortfolioDetails;
import lombok.Data;

@SuppressWarnings("WeakerAccess")
@Data
public class OpenPortfolioForm {
    String name;

    OpenPortfolioDetails toRequest() {
        return OpenPortfolioDetails.builder()
                .name(getName())
                .build();
    }
}
