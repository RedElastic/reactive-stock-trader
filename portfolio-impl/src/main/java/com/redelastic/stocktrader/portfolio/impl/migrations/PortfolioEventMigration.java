package com.redelastic.stocktrader.portfolio.impl.migrations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.lightbend.lagom.serialization.JacksonJsonMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PortfolioEventMigration extends JacksonJsonMigration {

    private final Logger log = LoggerFactory.getLogger(PortfolioEventMigration.class);

    @Override
    public int currentVersion() {
        return 2;
    }

    @Override
    public JsonNode transform(int fromVersion, JsonNode json) {
        ObjectNode root = (ObjectNode) json;
        fixNullPortfolioName(fromVersion, root);
        return root;
    }

    private void fixNullPortfolioName(int fromVersion, ObjectNode root) {
        if (fromVersion <= 1 && !root.hasNonNull("name")) {
            log.error("migrating");
            root.set("name", TextNode.valueOf(""));
        }
    }
}
