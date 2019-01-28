package com.redelastic.stocktrader.portfolio.impl;

import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.japi.Pair;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.order.OrderId;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.ConcurrentHashMap;

// TODO: Replace with actor
public class ExpirationScheduler {

    private final ActorSystem actorSystem;
    private final Provider<PortfolioRepository> portfolioRepositoryProvider;

    private final ConcurrentHashMap<Pair<PortfolioId, OrderId>, Cancellable> timers;

    @Inject
    public ExpirationScheduler(ActorSystem actorSystem,
                               Provider<PortfolioRepository> portfolioRepositoryProvider) {
        this.actorSystem = actorSystem;
        this.portfolioRepositoryProvider = portfolioRepositoryProvider;
        this.timers = new ConcurrentHashMap<>();
    }

    void schedule(PortfolioId portfolioId, OrderId orderId, FiniteDuration duration) {
        Pair<PortfolioId, OrderId> timerKey = Pair.create(portfolioId, orderId);
        Cancellable existingTimer = timers.get(timerKey);
        if (existingTimer != null) existingTimer.cancel();
        Cancellable timer = actorSystem.scheduler().scheduleOnce(
                duration,
                () -> portfolioRepositoryProvider.get().get(portfolioId).expireOrder(orderId),
                actorSystem.getDispatcher());
        timers.put(timerKey, timer);
    }
}
