package test.helpers.domain;

public class AccountBuilder {
    private String id;
    private double balance = 0.00;
    private String clientId;

    public AccountBuilder build() {
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "{\"clientId\":\"" + clientId + "\", \"id\":\"" + id + "\", \"balance\":\"" + balance + "\"}";
    }
}
