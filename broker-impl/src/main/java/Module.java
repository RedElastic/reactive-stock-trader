import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.impl.BrokerServiceImpl;
import com.redelastic.stocktrader.broker.impl.order.OrderRepository;
import com.redelastic.stocktrader.broker.impl.order.OrderRepositoryImpl;
import com.redelastic.stocktrader.broker.impl.quote.IexQuoteServiceImpl;
import com.redelastic.stocktrader.broker.impl.quote.QuoteService;
import com.redelastic.stocktrader.broker.impl.trade.TradeService;
import com.redelastic.stocktrader.broker.impl.trade.TradeServiceImpl;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;

@SuppressWarnings("WeakerAccess")
public class Module extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(BrokerService.class, BrokerServiceImpl.class);
        bindClient(PortfolioService.class);
        bind(QuoteService.class).to(IexQuoteServiceImpl.class);
        bind(TradeService.class).to(TradeServiceImpl.class);
        bind(OrderRepository.class).to(OrderRepositoryImpl.class);
    }
}