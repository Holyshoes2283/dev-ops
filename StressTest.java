public class RecordedSimulation extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
    .baseUrl("http://16.171.132.166:8080")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .contentTypeHeader("application/x-www-form-urlencoded")
    .originHeader("http://16.171.132.166:8080")
    .upgradeInsecureRequestsHeader("1")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:137.0) Gecko/20100101 Firefox/137.0");

  private Map<CharSequence, String> headers_0 = Map.of("Priority", "u=0, i");

  private ScenarioBuilder scn = scenario("StressTestSimulation")
    .exec(
      http("Login Request")
        .post("/devops-app/result.jsp")
        .headers(headers_0)
        .formParam("username", "user")
    );

  {
    setUp(
      scn.injectOpen(
        nothingFor(Duration.ofSeconds(5)),              // warmup
        rampUsers(20).during(Duration.ofMinutes(2)),  // ramp up 18 users over 2 minutes
        constantUsersPerSec(20).during(Duration.ofMinutes(5)) // sustain 15 users/sec לאורך חמש דקות
      )
    ).protocols(httpProtocol)
     .assertions(
        global.successfulRequests().percent().gt(95.0),
        global.responseTime().max().lt(2000) //מגבלת זמן תגובה 2000
     );
  }
}
