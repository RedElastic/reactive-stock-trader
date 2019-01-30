package controllers.forms.transfer;

import lombok.Data;

import java.math.BigDecimal;

@SuppressWarnings("WeakerAccess")
@Data
public class TransferForm {
    public enum AccountType {
        portfolio,
        savings
    }

    BigDecimal amount;
    AccountType sourceType;
    String sourceId;
    AccountType destinationType;
    String destinationId;
}
