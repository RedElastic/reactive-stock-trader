/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import com.redelastic.stocktrader.portfolio.impl.PortfolioRepository;
import com.redelastic.stocktrader.portfolio.impl.PortfolioRepositoryImpl;
import com.redelastic.stocktrader.portfolio.impl.PortfolioServiceImpl;
import com.redelastic.stocktrader.wiretransfer.api.WireTransferService;
import com.lightbend.lagom.javadsl.api.ServiceLocator;
import com.lightbend.lagom.javadsl.client.ConfigurationServiceLocator;
import com.typesafe.config.Config;
import play.Environment;


@SuppressWarnings("WeakerAccess")
public class Module extends AbstractModule implements ServiceGuiceSupport {
    private final Environment environment;
    private final Config config;

    public Module(Environment environment, Config config) {
        this.environment = environment;
        this.config = config;
    }

    @Override
    protected void configure() {
        bindService(PortfolioService.class, PortfolioServiceImpl.class);
        bind(PortfolioRepository.class).to(PortfolioRepositoryImpl.class);
        bindClient(BrokerService.class);
        bindClient(WireTransferService.class);
    }
}