package se.kry.codetest.data;

public class ServiceDTO {
    private String url;
    private String name;
    private String createDate;
    private String serverStatus;

    public ServiceDTO(String url, String name, String createDate, String serverStatus) {
        this.url = url;
        this.name = name;
        this.createDate = createDate;
        this.serverStatus = serverStatus;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(String serverStatus) {
        this.serverStatus = serverStatus;
    }
}
