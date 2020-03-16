import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.pollinterval.FibonacciPollInterval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.helpers.ClientBuilder;
import test.helpers.PropsHelper;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class TestClass {
    private Faker faker;
    private String firstName;
    private ClientBuilder requestClientBuilder;
    private ClientBuilder responseClientBuilder;
    private Response getResponse;
    private PropsHelper propsHelper;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/";
        requestClientBuilder = new ClientBuilder().build();
    }

    //
    private Response postRequestHandler(String endpoint) {

        return RestAssured.given().log().all()
                .header("Content-Type", "application/json")
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
                .header("Content-Type", "application/json")
                .get(endpoint);
        return getResponse.statusCode() == 200;
    }

//    @Test
    public void createClientTest() {
        String endpoint = "clients";
        Response response = postRequestHandler(endpoint);

        assertThat(response.getStatusCode(), is(201));

        assertThat(response.getHeaders().hasHeaderWithName("Location"), is(true));
        requestClientBuilder.setId(StringUtils.right(response.getHeader("Location"), 36));

        // async
        retryInvocation(() -> getHandler(response.getHeader("Location")));

        responseClientBuilder.setId(getResponse.getBody().path("id"));
        responseClientBuilder.setName(getResponse.getBody().path("name"));
        responseClientBuilder.setEmail(getResponse.getBody().path("email"));

        assertThat(requestClientBuilder, equalTo(responseClientBuilder));
    }

    @Test
    public void propertiesTest() {

        System.out.println(propsHelper.propertyFile("test.properties").getProperty("app.base.uri"));
    }
}
