package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import java.util.UUID;

public class HomeController extends Controller {

    private static String instanceId = UUID.randomUUID().toString();

    public Result instanceId() {
        return ok(instanceId);
    }

    public Result healthz() {
        // Add additional healthchecks here, e.g, test PubSub API, Message Broker API, etc
        return ok("OK");
    }

}