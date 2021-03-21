import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.api.ServiceAcl;
import com.lightbend.lagom.javadsl.api.ServiceInfo;
import com.lightbend.lagom.javadsl.client.ServiceClientGuiceSupport;
import com.lightbend.lagom.javadsl.client.ConfigurationServiceLocator;
import com.lightbend.lagom.javadsl.api.ServiceLocator;
import play.Environment;
import com.typesafe.config.Config;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import com.redelastic.stocktrader.wiretransfer.api.WireTransferService;
import services.quote.QuoteService;
import services.quote.QuoteServiceImpl;

@SuppressWarnings("WeakerAccess")
public class Module extends AbstractModule implements ServiceClientGuiceSupport {
    private final Environment environment;
    private final Config config;

    public Module(Environment environment, Config config) {
        this.environment = environment;
        this.config = config;
    }

    @Override
    protected void configure() {
        // route all paths to through this Play BFF
        bindServiceInfo(ServiceInfo.of("web-gateway-module", ServiceAcl.path(".*")));
        bindClient(PortfolioService.class);
        bindClient(BrokerService.class);
        bindClient(WireTransferService.class);

        bind(JavaJsonCustomObjectMapper.class).asEagerSingleton();
        bind(QuoteService.class).to(QuoteServiceImpl.class);

        if (environment.isProd()) {
            bind(ServiceLocator.class).to(ConfigurationServiceLocator.class);
        }
    }
}
