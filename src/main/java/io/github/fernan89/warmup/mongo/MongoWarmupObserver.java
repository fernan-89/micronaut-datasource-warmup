package io.github.fernan89.warmup.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClient;
import io.github.fernan89.warmup.WarmupRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Forces MongoDB's Server Discovery and Monitoring (SDAM) to resolve cluster topology at startup,
 * before an orchestrator's readiness probe polls the health endpoint - a deterministic {@code ping}
 * against the {@code admin} database, then against the application database resolved from
 * {@code mongodb.uri}, eliminates the transient "UNKNOWN" topology state and cold-start latency a
 * readiness probe would otherwise race.
 */
@Singleton
@Requires(notEnv = "test")
@Requires(beans = MongoClient.class)
@Slf4j
public class MongoWarmupObserver implements ApplicationEventListener<StartupEvent> {

    private static final BsonDocument PING = new BsonDocument("ping", new BsonInt32(1));

    private final MongoClient mongoClient;
    private final String applicationDatabase;
    private final MongoWarmupProperties properties;
    private final WarmupRunner runner;
    private final ApplicationContext applicationContext;

    @Inject
    public MongoWarmupObserver(MongoClient mongoClient, @Property(name = "mongodb.uri") String mongoUri,
            MongoWarmupProperties properties, WarmupRunner runner, ApplicationContext applicationContext) {
        this.mongoClient = Objects.requireNonNull(mongoClient, "mongoClient must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.runner = Objects.requireNonNull(runner, "runner must not be null");
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext must not be null");
        this.applicationDatabase = resolveDatabase(mongoUri);
    }

    private static String resolveDatabase(String mongoUri) {
        try {
            String database = new ConnectionString(mongoUri).getDatabase();
            return database != null ? database : "admin";
        } catch (IllegalArgumentException e) {
            log.warn("[WARMUP] mongo: could not parse mongodb.uri, defaulting to the admin database. Reason: {}", e.getMessage());
            return "admin";
        }
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        // flatMap, not then(Mono.from(...)): the application-database ping must stay lazy, evaluated only
        // once the admin ping actually succeeds - a direct then() argument is built eagerly, calling
        // runCommand() on both databases up front regardless of the admin ping's outcome.
        runner.run("mongo", properties, applicationContext, () ->
                Mono.from(mongoClient.getDatabase("admin").runCommand(PING))
                        .flatMap(adminResult -> Mono.from(mongoClient.getDatabase(applicationDatabase).runCommand(PING)))
                        .then());
    }
}
