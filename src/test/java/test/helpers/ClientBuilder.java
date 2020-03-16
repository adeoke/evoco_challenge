package test.helpers;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Locale;

public class ClientBuilder {
    private String id;
    private String name = new Faker().name().firstName() + " " + new Faker().name().lastName();
    private String email = new FakeValuesService(new Locale("en-GB"), new RandomService()).bothify("????##@gmail.com");

    public ClientBuilder name(String name) {
        this.name = name;
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public ClientBuilder email(String email) {
        this.email = email;
        return this;
    }

    public ClientBuilder build() {
        return this;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "{\"name\":\"" + name + "\", \"email\":\"" + email + "\"}";
    }
}
