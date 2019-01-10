import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.impl.*;
import com.redelastic.stocktrader.broker.impl.quote.IexQuoteServiceImpl;
import com.redelastic.stocktrader.broker.impl.quote.QuoteService;
import com.redelastic.stocktrader.broker.impl.trade.TradeService;
import com.redelastic.stocktrader.broker.impl.trade.TradeServiceImpl;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;

public class Module extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(BrokerService.class, BrokerServiceImpl.class);
        bindClient(PortfolioService.class);
        bind(QuoteService.class).to(IexQuoteServiceImpl.class);
        bind(TradeService.class).to(TradeServiceImpl.class);
    }
}