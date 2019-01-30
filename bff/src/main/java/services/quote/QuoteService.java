package services.quote;

import com.redelastic.stocktrader.portfolio.api.Holding;
import org.pcollections.PSequence;

import java.util.concurrent.CompletionStage;

public interface QuoteService {
    CompletionStage<PSequence<ValuedHolding>> priceHoldings(PSequence<Holding>holdings);
}
