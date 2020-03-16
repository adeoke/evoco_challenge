import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.awaitility.pollinterval.FibonacciPollInterval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.helpers.UrlHelper;
import test.helpers.domain.AccountBuilder;
import test.helpers.domain.ClientBuilder;
import test.helpers.PropsHelper;
import test.helpers.domain.ClientIdBuilder;
import test.helpers.domain.MoneyBuilder;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class TestClass {
    private static final int TIMEOUT = 10;
    private static final int DEPOSIT_AMOUNT = 1000000;
    private ClientBuilder requestClientBuilder;
    private ClientBuilder responseClientBuilder;
    private ClientIdBuilder clientIdBuilder;
    private AccountBuilder accountBuilder;
    private MoneyBuilder moneyBuilder;
    private Response getResponse;
    private UrlHelper urlHelper;
    private final String appJsonHeader = "application/json";
    private String clientsEndpoint;
    private String accountsEndpoint;

    @BeforeEach
    public void setUp() throws IOException {
        PropsHelper propsHelper = new PropsHelper();

        RestAssured.defaultParser = Parser.JSON;
        RestAssured.baseURI = propsHelper.parsePropFile("test.properties").getProperty("app.base.uri");
        RestAssured.port = Integer.parseInt(propsHelper.parsePropFile("test.properties").getProperty("app.port"));
        RestAssured.basePath = propsHelper.parsePropFile("test.properties").getProperty("app.base.path");

        accountsEndpoint = propsHelper.parsePropFile("test.properties").getProperty("accounts.endpoint");
        clientsEndpoint = propsHelper.parsePropFile("test.properties").getProperty("clients.endpoint");

        requestClientBuilder = new ClientBuilder().build();
        responseClientBuilder = new ClientBuilder();
        clientIdBuilder = new ClientIdBuilder();
        accountBuilder = new AccountBuilder();
        moneyBuilder = new MoneyBuilder();
    }

    private Response postRequestHandler(String endpoint, String payload) {
        return RestAssured.given().log().all()
                .contentType(appJsonHeader)
                .body(payload)
                .post(endpoint);
    }

    private void retryInvocation(Callable<Boolean> callableMethod) {
        await().atMost(TIMEOUT, TimeUnit.SECONDS).
                pollInterval(FibonacciPollInterval.fibonacci(TimeUnit.SECONDS)).
                ignoreExceptions().
                until(callableMethod);
    }

    private Boolean getHandler(String endpoint) {
        getResponse = RestAssured.given().log().all()
                .contentType(appJsonHeader)
                .get(endpoint);
        return getResponse.statusCode() == HttpStatus.SC_OK;
    }

    @Test
    public void createClientTest() {
        Response response = postRequestHandler(clientsEndpoint, requestClientBuilder.toString());

        assertThat(response.getStatusCode(), is(HttpStatus.SC_CREATED));

        assertThat(response.getHeaders().hasHeaderWithName("Location"), is(true));
        urlHelper = new UrlHelper(response.getHeader("Location"));
        requestClientBuilder.setId(urlHelper.getPath());

        // async
        retryInvocation(() -> getHandler(response.getHeader("Location")));

        responseClientBuilder.setId(getResponse.getBody().path("id"));
        responseClientBuilder.setName(getResponse.getBody().path("name"));
        responseClientBuilder.setEmail(getResponse.getBody().path("email"));

        assertThat(requestClientBuilder.toString(), equalTo(responseClientBuilder.toString()));
    }

    @Test
    public void createAccountTest() {
        Response response = postRequestHandler(clientsEndpoint, requestClientBuilder.toString());

        assertThat(response.getStatusCode(), is(HttpStatus.SC_CREATED));

        assertThat(response.getHeaders().hasHeaderWithName("Location"), is(true));
        urlHelper = new UrlHelper(response.getHeader("Location"));
        requestClientBuilder.setId(urlHelper.getPath());

        // async
        retryInvocation(() -> getHandler(response.getHeader("Location")));
        clientIdBuilder.setClientId(getResponse.getBody().path("id").toString());
        clientIdBuilder.build();

        Response accountsResponse = postRequestHandler(accountsEndpoint, clientIdBuilder.toString());
        assertThat(accountsResponse.getStatusCode(), is(HttpStatus.SC_CREATED));
        urlHelper = new UrlHelper(accountsResponse.getHeader("Location"));
        String accountId = urlHelper.getPath();

        retryInvocation(() -> getHandler(accountsResponse.getHeader("Location")));

        accountBuilder.setClientId(clientIdBuilder.getClientId());
        accountBuilder.setId(accountId);
        accountBuilder.build();

        assertThat(getResponse.path("clientId"), equalTo(accountBuilder.getClientId()));
        assertThat(getResponse.path("balance").toString(), equalTo(accountBuilder.getBalance().toString()));
        assertThat(getResponse.path("id"), equalTo(accountBuilder.getId()));
    }

    @Test
    public void depositTest() {
        Response response = postRequestHandler(clientsEndpoint, requestClientBuilder.toString());

        assertThat(response.getStatusCode(), is(HttpStatus.SC_CREATED));

        assertThat(response.getHeaders().hasHeaderWithName("Location"), is(true));
        urlHelper = new UrlHelper(response.getHeader("Location"));
        requestClientBuilder.setId(urlHelper.getPath());

        // async
        retryInvocation(() -> getHandler(response.getHeader("Location")));
        clientIdBuilder.setClientId(getResponse.getBody().path("id").toString());
        clientIdBuilder.build();

        Response accountsResponse = postRequestHandler(accountsEndpoint, clientIdBuilder.toString());
        assertThat(accountsResponse.getStatusCode(), is(HttpStatus.SC_CREATED));
        urlHelper = new UrlHelper(accountsResponse.getHeader("Location"));
        String accountId = urlHelper.getPath();

        retryInvocation(() -> getHandler(accountsResponse.getHeader("Location")));

        String depositsEndpoint = accountsEndpoint + "/" + accountId + "/" + "deposits";
        moneyBuilder.setAmount(DEPOSIT_AMOUNT);
        moneyBuilder.build();

        accountBuilder.setClientId(clientIdBuilder.getClientId());
        accountBuilder.setId(accountId);
        accountBuilder.setBalance(moneyBuilder.getAmount());
        accountBuilder.build();

        Response depositResponse = postRequestHandler(depositsEndpoint, moneyBuilder.toString());
        assertThat(depositResponse.getStatusCode(), is(HttpStatus.SC_NO_CONTENT));

        String accountsUri = accountsEndpoint + "/" + accountId;
        retryInvocation(() -> getHandler(accountsUri));

        assertThat(getResponse.statusCode(), equalTo(HttpStatus.SC_OK));
        assertThat(getResponse.path("id"), equalTo(accountBuilder.getId()));
        assertThat(getResponse.path("balance").toString(), equalTo(accountBuilder.getBalance().toString()));
        assertThat(getResponse.path("clientId"), equalTo(accountBuilder.getClientId()));
    }
}
