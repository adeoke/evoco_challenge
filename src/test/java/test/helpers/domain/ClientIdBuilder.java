package test.helpers.domain;

public class ClientIdBuilder {
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public ClientIdBuilder build() {
        return this;
    }

    @Override
    public String toString() {
        return "{\"clientId\":\"" + clientId + "\"}";
    }
}
