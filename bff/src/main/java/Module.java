import com.lightbend.lagom.javadsl.api.ServiceAcl;
import com.lightbend.lagom.javadsl.api.ServiceInfo;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.client.ServiceClientGuiceSupport;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;

@SuppressWarnings("WeakerAccess")
public class Module extends AbstractModule implements ServiceClientGuiceSupport {
    @Override
    protected void configure() {
        // route all paths to through this Play BFF
        bindServiceInfo(ServiceInfo.of("web-gateway-module", ServiceAcl.path(".*")));
        bindClient(PortfolioService.class);
        bindClient(BrokerService.class);

        bind(JavaJsonCustomObjectMapper.class).asEagerSingleton();
    }
}