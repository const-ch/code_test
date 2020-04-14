package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import se.kry.codetest.data.ServiceDTO;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static se.kry.codetest.util.Constants.DELETE_FROM_SERVICE_SQL;
import static se.kry.codetest.util.Constants.INSERT_INTO_SERVICE_SQL;
import static se.kry.codetest.util.Constants.PENDING_STATUS;
import static se.kry.codetest.util.Constants.SELECT_FROM_SERVICE_SQL;

public class ServiceRepository {

    private final DBConnector connector;

    public ServiceRepository(DBConnector connector) {
        this.connector = connector;
    }

    public Future<List<ServiceDTO>> retrieveServiceList() {
        Future<List<ServiceDTO>> future = Future.future();
        connector.query(SELECT_FROM_SERVICE_SQL).setHandler(servicesResult -> {
            if (servicesResult.succeeded()) {
                future.complete(
                        servicesResult.result()
                                .getResults()
                                .stream()
                                .map(json -> new ServiceDTO(
                                        json.getString(0),
                                        json.getString(1),
                                        json.getString(2),
                                        PENDING_STATUS
                                ))
                                .collect(Collectors.toList())
                );
            } else {
                future.fail(servicesResult.cause());
            }
        });
        return future;
    }

    public Future<Void> saveURL(String service, String name){
        Future<Void> future = Future.future();
        connector.query(INSERT_INTO_SERVICE_SQL, new JsonArray()
                .add(service)
                .add(name)
                .add(DateTimeFormatter.ISO_INSTANT.format(Instant.now())))
                .setHandler(results -> {
                    if (results.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(results.cause());
                    }
                });
        return future;
    }

    public Future<Void> deleteURL(String service){
        Future<Void> future = Future.future();
        connector.query(DELETE_FROM_SERVICE_SQL, new JsonArray().add(service))
                .setHandler(servicesResult -> {
                    if (servicesResult.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(servicesResult.cause());
                    }
                });
        return future;
    }
}