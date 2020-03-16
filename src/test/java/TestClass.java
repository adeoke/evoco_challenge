import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.awaitility.pollinterval.FibonacciPollInterval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.helpers.UrlHelper;
import test.helpers.domain.ClientBuilder;
import test.helpers.PropsHelper;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class TestClass {
    private ClientBuilder requestClientBuilder;
    private ClientBuilder responseClientBuilder;
    private Response getResponse;
    private UrlHelper urlHelper;
    private PropsHelper propsHelper;
    private final String appJsonHeader = "application/json";

    @BeforeEach
    public void setUp() throws IOException {
        propsHelper = new PropsHelper();
        RestAssured.baseURI = propsHelper.parsePropFile("test.properties").getProperty("app.base.uri");
        RestAssured.port = Integer.parseInt(propsHelper.parsePropFile("test.properties").getProperty("app.port"));
        RestAssured.basePath = propsHelper.parsePropFile("test.properties").getProperty("app.base.path");
        requestClientBuilder = new ClientBuilder().build();
        responseClientBuilder = new ClientBuilder();
    }

    private Response postRequestHandler(String endpoint) {
        return RestAssured.given().log().all()
                .contentType(appJsonHeader)
                .body(requestClientBuilder.toString())
                .post(endpoint);
    }

    private void retryInvocation(Callable<Boolean> callableMethod) {
        await().atMost(10, TimeUnit.SECONDS).
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
    public void createClientTest() throws IOException {
        String endpoint = propsHelper.parsePropFile("test.properties").getProperty("clients.endpoint");
        Response response = postRequestHandler(endpoint);

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

//    @Test
    public void createAccountTest() {

    }

//    @Test
//    public void depositTest() {
//
//    }
//
//    @Test
//    public void checkBalanceTest() {
//
//    }
}
