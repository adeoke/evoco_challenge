package test.helpers.domain;

public class MoneyBuilder {
    private double amount;

    public MoneyBuilder build() {
        return this;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "{\"amount\":\"" + amount + "\"}";
    }
}
