package controllers.forms.portfolio;

import com.redelastic.stocktrader.portfolio.api.OpenPortfolioDetails;
import lombok.Data;

@SuppressWarnings("WeakerAccess")
@Data
public class OpenPortfolioForm {
    String name;

    public OpenPortfolioDetails toRequest() {
        return new OpenPortfolioDetails(getName());
    }
}
