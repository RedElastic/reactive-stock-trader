/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.api.ServiceAcl;
import com.lightbend.lagom.javadsl.api.ServiceInfo;
import com.lightbend.lagom.javadsl.client.ServiceClientGuiceSupport;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import com.redelastic.stocktrader.wiretransfer.api.WireTransferService;
import services.quote.QuoteService;
import services.quote.QuoteServiceImpl;

@SuppressWarnings("WeakerAccess")
public class Module extends AbstractModule implements ServiceClientGuiceSupport {
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