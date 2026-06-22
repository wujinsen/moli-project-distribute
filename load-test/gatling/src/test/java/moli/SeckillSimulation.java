package moli;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Gatling seckill simulation for high-throughput JVM-based load generation.
 *
 * Run:
 *   cd load-test/gatling
 *   mvn gatling:test -Dgatling.simulationClass=moli.SeckillSimulation \
 *     -DbaseUrl=http://localhost:21000 -DtargetRps=50000
 */
public class SeckillSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:21000");
    private static final int TARGET_RPS = Integer.getInteger("targetRps", 10000);
    private static final int DURATION_SEC = Integer.getInteger("durationSec", 300);

    HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .shareConnections()
            .disableWarmUp();

    ScenarioBuilder seckill = scenario("Seckill Order")
            .exec(session -> session.set("uid", "gatling-" + session.userId() + "-" + System.nanoTime()))
            .exec(
                    http("place order")
                            .post("/OrderServer/seckill/order")
                            .body(StringBody("{\"activityId\":1,\"userId\":\"#{uid}\",\"requestId\":\"#{uid}\"}"))
                            .check(status().in(200, 409, 429))
            );

    {
        setUp(
                seckill.injectOpen(
                        constantUsersPerSec(TARGET_RPS).during(DURATION_SEC)
                )
        ).protocols(httpProtocol)
                .throttle(
                        reachRps(TARGET_RPS).in(30),
                        holdFor(DURATION_SEC)
                )
                .maxDuration(DURATION_SEC + 60)
                .assertions(
                        global().successfulRequests().percent().gt(95.0),
                        global().responseTime().percentile3().lt(2000)
                );
    }
}
