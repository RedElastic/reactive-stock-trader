package com.redelastic.stocktrader.broker.impl.order;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;


public class OrderEntityTest {
    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    private PersistentEntityTestDriver<OrderCommand, OrderEvent, Optional<OrderState>> createOrderEntity(String id) {
        return new PersistentEntityTestDriver<>(system, new OrderEntity(), id);
    }

    @Test
    public void startOrder() {
        String orderId = "orderId";
        PersistentEntityTestDriver<OrderCommand, OrderEvent, Optional<OrderState>> driver = createOrderEntity(orderId);


    }
}
