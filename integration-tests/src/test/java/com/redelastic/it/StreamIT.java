package com.redelastic.it;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.client.integration.LagomClientFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.redelastic.stream.api.StreamService;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class StreamIT {

    private static final String SERVICE_LOCATOR_URI = "http://localhost:9008";

    private static LagomClientFactory clientFactory;
    private static ActorSystem system;
    private static Materializer mat;

    @BeforeClass
    public static void setup() {
        clientFactory = LagomClientFactory.create("integration-test", StreamIT.class.getClassLoader());
        // One of the clients can use the service locator, the other can use the service gateway, to test them both.

        system = ActorSystem.create();
        mat = ActorMaterializer.create(system);
    }


    private <T> T await(CompletionStage<T> future) throws Exception {
        return future.toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    @AfterClass
    public static void tearDown() {
        if (clientFactory != null) {
            clientFactory.close();
        }
        if (system != null) {
            system.terminate();
        }
    }




}
