package googleadstest.domain.model;

public class GoogleIpBlockResponse {

    private long id;
    private String ipAddress;

    public GoogleIpBlockResponse(long id, String ipAddress) {
        this.id = id;
        this.ipAddress = ipAddress;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
