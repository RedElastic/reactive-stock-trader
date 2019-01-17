# Issues

- It'd be nice if the portfolio ID was a custom type (`PortfolioId`) rather than a String, however this necessitates adding custom JSON handling.

- Seems like if Lagom services are internally facing only the confusion between path parameters and request data is unnecessary. It'd be easier to use zero parameter service calls, and `call` to bind them, and access them through LagomClient library.

    - `autoAcl` with gateway does not namespace the methods with a service name   
    
-  Better way to handle rendering of BigDecimal as money on API than setting

- Clear conventions around path parameters versus request body? Seems reasonable to include entity ID as part of path. Is there a reason to favour between "/api/broker/buyStock/IBM?shares=10" vs. "/api/broker/buyStock" vs. "/api/broker/buyStock?symbol=IBM&shares=10"    

- REST doesn't really fit with a DDD philosophy in general. We do want to talk about entities, but avoid thinking about them purely in terms of CRUD. `restCall` should really be thought of as HTTP call, not REST.

- Can we proxy the HTML error responses from the Lagom services through the BFF for development? 
    
# Design

We'll treat Lagom as purely internally facing, and we won't care too much about the HTTP interface it is exposing, relying on Play as a service client to moderate external requests to the Lagom space. Thus we'll treat all service calls as parameterless, pure request-response.

# Notes

Caution: be careful not to reference the initial state in a behaviour in a command, for example, but instead use the state()
method. It may be best to split all command/event handlers out as separate methods from the behaviour defining method
to ensure that the initial state passed to the behaviour method (used in the newBehaviorBuilder) is not referenced.     

# Java issues
- Choice between all arguments constructor or builder. Without named parameters the former can be error prone when there are multiple parameters with the same type (e.g. String). The builder option suffers from incomplete initialization, which can only resolved at runtime through @NonNull annotations. In short we need to either consign ourselves to being open to transposition errors (e.g. swapping the order of orderId and portfolioId), or incomplete initialization. Wrappers for these could help, but may be very cumbersome in Java and with serde.