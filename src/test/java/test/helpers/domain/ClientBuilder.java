package test.helpers.domain;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;

import java.util.Locale;

public class ClientBuilder {
    private String id;
    private String name = new Faker().name().firstName() + " " + new Faker().name().lastName();
    private String email = new FakeValuesService(new Locale("en-GB"), new RandomService()).bothify("????##@gmail.com");

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ClientBuilder build() {
        return this;
    }

    @Override
    public String toString() {
        return "{\"name\":\"" + name + "\", \"email\":\"" + email + "\"}";
    }
}
