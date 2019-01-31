package com.redelastic.stocktrader.portfolio.impl;

import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.portfolio.api.OpenPortfolioDetails;
import com.redelastic.stocktrader.portfolio.api.OrderPlaced;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PortfolioRepositoryImpl implements PortfolioRepository {

    private final Logger log = LoggerFactory.getLogger(PortfolioRepositoryImpl.class);

    private final BrokerService brokerService;

    private final PersistentEntityRegistry persistentEntities;

    @Inject
    public PortfolioRepositoryImpl(BrokerService brokerService,
                                   PersistentEntityRegistry persistentEntities) {
        this.brokerService = brokerService;
        this.persistentEntities = persistentEntities;
        persistentEntities.register(PortfolioEntity.class);
    }

    /**
     * Initialize a new portfolio. We first generate a new ID for it and send it a setup message. In the very unlikely
     * circumstance that the ID is already in use we'll get an exception when we send the initialize command, we should
     * retry with a new UUID.
     * @param request
     * @return The PortfolioModel ID assigned.
     */
    // TODO: Implement retry logic. Theoretically the chance of a collision is astronomically low *given* everything else works.
    @Override
    public CompletionStage<PortfolioId> open(OpenPortfolioDetails request) {
        val portfolioId = PortfolioId.newId();
        PersistentEntityRef<PortfolioCommand> ref = persistentEntities.refFor(PortfolioEntity.class, portfolioId.getId());
        return ref.ask(new PortfolioCommand.Open(request.getName()))
                .thenApply(done -> portfolioId);
    }

    @Override
    public PortfolioModel get(PortfolioId portfolioId) {
        return new PortfolioModel(brokerService, persistentEntities, portfolioId);
    }

    @Override
    public PersistentEntityRef<PortfolioCommand> getRef(PortfolioId portfolioId) {
        return persistentEntities.refFor(PortfolioEntity.class, portfolioId.getId());
    }

    public Source<Pair<OrderPlaced, Offset>, ?> ordersStream(AggregateEventTag<PortfolioEvent> tag, Offset offset) {
        return persistentEntities.eventStream(tag, offset)
            .filter(eventOffset ->
                    eventOffset.first() instanceof PortfolioEvent.OrderPlaced
            ).mapAsync(1, eventOffset -> {
                    PortfolioEvent.OrderPlaced orderPlaced = (PortfolioEvent.OrderPlaced)eventOffset.first();
                    log.info(String.format("Publishing order %s", orderPlaced.getOrderId()));
                    return CompletableFuture.completedFuture(Pair.create(
                            orderPlaced.asDomainEvent(),
                            eventOffset.second()
                    ));
            });
    }

}