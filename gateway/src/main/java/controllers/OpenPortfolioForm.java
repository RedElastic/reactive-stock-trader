package controllers;

import com.redelastic.stocktrader.portfolio.api.OpenPortfolioRequest;
import lombok.Data;

@Data
public class OpenPortfolioForm {
    String name;

    OpenPortfolioRequest toRequest() {
        return OpenPortfolioRequest.builder()
                .name(getName())
                .build();
    }
}
