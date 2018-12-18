import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.impl.BrokerServiceImpl;

public class Module extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(BrokerService.class, BrokerServiceImpl.class);
    }
}