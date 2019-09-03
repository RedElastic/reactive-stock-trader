package services.quote;

import com.redelastic.CSHelper;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.api.Quote;
import com.redelastic.stocktrader.portfolio.api.Holding;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.stream.Collectors.toList;

public class QuoteServiceImpl implements QuoteService {

    private final BrokerService brokerService;

    @Inject
    QuoteServiceImpl(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    @Override
    public CompletionStage<PSequence<ValuedHolding>> priceHoldings(PSequence<Holding> holdings) {
        List<CompletableFuture<ValuedHolding>> requests = holdings.stream()
                .map(valuedHolding -> {
                    CompletionStage<BigDecimal> getSharePrice = brokerService
                            .getQuote(valuedHolding.getSymbol())
                            .invoke()
                            .thenApply(Quote::getSharePrice);

                    CompletionStage<BigDecimal> nullPriceOnFailure = CSHelper.recover(getSharePrice, RuntimeException.class, ex -> null);

                    return nullPriceOnFailure

                            .thenApply(sharePrice -> {
                                BigDecimal price = sharePrice == null ? null : sharePrice.multiply(BigDecimal.valueOf(valuedHolding.getShareCount()));
                                return new ValuedHolding(
                                        valuedHolding.getSymbol(),
                                        valuedHolding.getShareCount(),
                                        price);
                            })
                            .toCompletableFuture();
                })
                .collect(toList());

        return CSHelper.allOf(requests).thenApply(ConsPStack::from);
    }
}
