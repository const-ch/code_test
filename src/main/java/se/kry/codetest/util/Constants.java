package se.kry.codetest.util;

public class Constants {

    public static final String URL_PARAM = "url";
    public static final String NAME_PARAM = "name";

    public static final String SERVICE_PATH = "/service";

    public static final String OK_STATUS = "OK";
    public static final String FAIL_STATUS = "FAIL";
    public static final String PENDING_STATUS = "Pending..";


    public static final String CREATE_SERVICES_SQL = "CREATE TABLE IF NOT EXISTS" +
            " service (" +
            "url VARCHAR(128) NOT NULL PRIMARY KEY, " +
            "name VARCHAR(128), " +
            "createDate VARCHAR(128))";
    public static final String SELECT_FROM_SERVICE_SQL = "SELECT * FROM service";
    public static final String INSERT_INTO_SERVICE_SQL = "INSERT INTO service VALUES (?, ?, ?)";
    public static final String DELETE_FROM_SERVICE_SQL = "DELETE FROM service WHERE url = ?";

    public static final String START_MESSAGE = "KRY code test service started";

}
