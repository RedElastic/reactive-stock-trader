package models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CompletedOrder {
    @NonNull String orderId;
}
