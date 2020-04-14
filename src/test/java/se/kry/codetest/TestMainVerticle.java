package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.kry.codetest.util.Constants.NAME_PARAM;
import static se.kry.codetest.util.Constants.OK_STATUS;
import static se.kry.codetest.util.Constants.SERVICE_PATH;
import static se.kry.codetest.util.Constants.URL_PARAM;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  @DisplayName("Start a web server on localhost responding to path /service on port 8080")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void start_http_server(Vertx vertx, VertxTestContext testContext) {
    WebClient.create(vertx)
        .get(8080, "::1", "/service")
        .send(response -> testContext.verify(() -> {
          assertEquals(200, response.result().statusCode());
          JsonArray body = response.result().bodyAsJsonArray();
          assertEquals(1, body.size());
          testContext.completeNow();
        }));
  }

    @Test
    @DisplayName("Add new service and check it")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void testAddServiceGetService(Vertx vertx, VertxTestContext testContext) {

        WebClient.create(vertx)
                .post(8080, "::1", SERVICE_PATH)
                .sendJsonObject(new JsonObject()
                                .put(URL_PARAM, "http://www.google.com")
                                .put(NAME_PARAM, "Google"),
                        response -> testContext.verify(() -> {
                    assertEquals(OK_STATUS, response.result().bodyAsString());

                    WebClient.create(vertx)
                            .get(8080, "::1", SERVICE_PATH)
                            .send(r -> testContext.verify(() -> {
                                assertEquals(OK_STATUS, response.result().bodyAsString());
                                testContext.completeNow();
                            }));
                }));
    }

}
