package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

import java.util.List;
import java.util.Map;

import static se.kry.codetest.util.Constants.FAIL_STATUS;
import static se.kry.codetest.util.Constants.OK_STATUS;

public class BackgroundPoller {


    private final WebClient webClient;

    public BackgroundPoller(Vertx vertx) {
        this.webClient = WebClient.create(vertx);
    }

    public Future<List<String>> pollServices(Map<String, String> services) {
        services.forEach((url, status) ->
            webClient.getAbs(url).send(result -> {
                if (result == null || result.result() == null
                        || result.result().statusCode()%100 != 2) {//if status code is 2XX
                    services.put(url, FAIL_STATUS);
                } else {
                    services.put(url, OK_STATUS);
                }
            }));
        return Future.succeededFuture();
    }

}