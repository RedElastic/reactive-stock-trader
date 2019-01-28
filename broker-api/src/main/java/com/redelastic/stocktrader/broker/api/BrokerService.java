package com.redelastic.stocktrader.broker.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.broker.kafka.KafkaProperties;
import com.lightbend.lagom.javadsl.api.transport.Method;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.*;

public interface BrokerService extends Service {

  /**
   * Get the most recent share price for a stock.
   * @param symbol Stock ticker symbol.
   * @return
   */
  ServiceCall<NotUsed, Quote> getQuote(String symbol);

  /**
   * Get the current status of an order.
   * @param orderId ID for the order.
   * @return Status of the order, if it exists, empty if no such order ID is known.
   */
  ServiceCall<NotUsed, Optional<OrderStatus>> getOrderStatus(String orderId);

  /**
   * Completion events for orders, either successfully as a trade, or unsuccessfully (due to expiration of timeout
   * or otherwise).
   */
  Topic<OrderResult> orderResult();

  String ORDER_RESULTS_TOPIC_ID = "Broker-OrderResults";

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("broker").withCalls(
            restCall(Method.GET, "/api/quote/:symbol", this::getQuote),
            restCall(Method.GET, "/api/order/:orderId", this::getOrderStatus)
    ).withTopics(
            topic(ORDER_RESULTS_TOPIC_ID, this::orderResult)
              .withProperty(KafkaProperties.partitionKeyStrategy(), OrderResult::getPortfolioId)
    );
    // @formatter:on
  }
}
