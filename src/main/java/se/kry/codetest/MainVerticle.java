package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import se.kry.codetest.data.ServiceDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static se.kry.codetest.util.Constants.NAME_PARAM;
import static se.kry.codetest.util.Constants.OK_STATUS;
import static se.kry.codetest.util.Constants.PENDING_STATUS;
import static se.kry.codetest.util.Constants.SERVICE_PATH;
import static se.kry.codetest.util.Constants.START_MESSAGE;
import static se.kry.codetest.util.Constants.URL_PARAM;

public class MainVerticle extends AbstractVerticle {

    public static final int PORT = 8080;
    public static final int POLLER_INTERVAL_MS = 6000;
    private Map<String, String> services = new HashMap<>();

    private DBConnector connector;
    private BackgroundPoller poller;
    private ServiceRepository serviceDAO;
    private Router router;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.poller = new BackgroundPoller(vertx);
        this.connector = new DBConnector(vertx);
        this.serviceDAO = new ServiceRepository(connector);
        router = Router.router(vertx);
    }

    @Override
    public void start(Future<Void> startFuture) {
        loadServicesInitData();
        setupPollerHandler();
        setupRoutes(router);
        setupServerListener(startFuture, router);
    }

    private void setupPollerHandler() {
        vertx.setPeriodic(POLLER_INTERVAL_MS, timerId ->
                serviceDAO.retrieveServiceList().setHandler(servicesResult -> {
                    if (servicesResult.succeeded()) {
                        servicesResult.result().forEach(
                                service -> this.services.putIfAbsent(service.getUrl(), PENDING_STATUS));
                        poller.pollServices(this.services);
                    }
                }));
    }

    private void loadServicesInitData() {
        serviceDAO.retrieveServiceList().setHandler(servicesResult -> {
            if (servicesResult.succeeded()) {
                this.services = servicesResult.result().stream()
                        .collect(Collectors.toMap(
                                service -> service.getUrl(),
                                status -> PENDING_STATUS));
            }
        });
    }

    private void setupRoutes(Router router) {

        router.route()
                .handler(BodyHandler.create())
                .handler(StaticHandler.create());

        setupPostServiceHandler(router);
        setupServiceGetHandler(router);
        setupDeleteServiceHandler(router);
    }

    private void setupServiceGetHandler(Router router) {
        router.get(SERVICE_PATH).handler(req ->
                serviceDAO.retrieveServiceList().setHandler(servicesResult -> {
                    if (servicesResult.succeeded()) {
                        List<ServiceDTO> result = servicesResult.result().stream()
                                .map(service -> {
                                    service.setServerStatus(this.services.getOrDefault(service.getUrl(), PENDING_STATUS));
                                    return service;
                                }).collect(Collectors.toList());
                        req.response()
                                .putHeader("content-type", "application/json")
                                .end(Json.encode(result));
                    } else {
                        req.fail(servicesResult.cause());
                    }
                }));
    }

    private void setupDeleteServiceHandler(Router router) {
        router.delete(SERVICE_PATH).handler(req -> {
            String url = req.getBodyAsJson().getString(URL_PARAM);
            serviceDAO.deleteURL(url);
            services.remove(url);
            responseSuccess(req);
        });
    }

    private void setupPostServiceHandler(Router router) {
        router.post(SERVICE_PATH).handler(req -> {
            String url = req.getBodyAsJson().getString(URL_PARAM);
            String name = req.getBodyAsJson().getString(NAME_PARAM);
            name = name.isEmpty() ? url : name;
            serviceDAO.saveURL(url, name);
            poller.pollServices(this.services);
            responseSuccess(req);
        });
    }

    private void responseSuccess(RoutingContext req) {
        req.response()
                .putHeader("content-type", "text/plain")
                .end(OK_STATUS);
    }

    private void setupServerListener(Future<Void> startFuture, Router router) {
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(PORT, result -> {
                    if (result.succeeded()) {
                        System.out.println(START_MESSAGE);
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }
}