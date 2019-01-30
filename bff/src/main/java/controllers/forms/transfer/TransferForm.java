package controllers.forms;

import lombok.Data;

import java.math.BigDecimal;

@SuppressWarnings("WeakerAccess")
@Data
public class TransferForm {
    public enum AccountType {
        Portfolio,
        Savings
    }

    BigDecimal amount;
    AccountType sourceType;
    String sourceId;
    AccountType destinationType;
    String destinationId;
}
