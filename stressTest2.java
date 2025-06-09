package devops;

import java.time.Duration;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class DevopsStressTestSimulation extends Simulation {

    private HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8888")
            .shareConnections()
            .disableFollowRedirect()
            .inferHtmlResources()
            .disableAutoReferer()
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .acceptEncodingHeader("gzip, deflate")
            .acceptLanguageHeader("en-US,en;q=0.5")
            .upgradeInsecureRequestsHeader("1")
            .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:138.0) Gecko/20100101 Firefox/138.0");

    private ScenarioBuilder scn = scenario("DevopsStressTest")
            .during(Duration.ofMinutes(4)).on(
                    exec(
                            http("Load index.jsp")
                                    .get("/devops-app/index.jsp"),
                            pause(3),
                            http("Submit form to result.jsp")
                                    .post("/devops-app/result.jsp")
                                    .formParam("username", "Tal"),
                            pause(1)
                    )
            );

    {
        setUp(
                scn.injectClosed(
                        rampConcurrentUsers(500).to(1540).during(Duration.ofSeconds(144)), // 2 min 24 sec
                        constantConcurrentUsers(1540).during(Duration.ofSeconds(96))       // 1 min 36 sec
                )
        )
                .protocols(httpProtocol)
                .assertions(
                        global().successfulRequests().percent().gt(90.0),
                        global().responseTime().percentile3().lt(3000)
                );
    }
}
