package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferCommand;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferEntity;

import javax.inject.Inject;

public class TransferRepository {

    private final PersistentEntityRegistry entityRegistry;

    @Inject
    public TransferRepository(PersistentEntityRegistry entityRegistry) {
        entityRegistry.register(TransferEntity.class);
        this.entityRegistry = entityRegistry;
    }

    public PersistentEntityRef<TransferCommand> get(String transferId) {
        return entityRegistry.refFor(TransferEntity.class, transferId);
    }
}
