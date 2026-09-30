# micronaut-datasource-warmup

Startup readiness warm-up for Micronaut services backed by MongoDB or any relational engine reachable
over R2DBC - PostgreSQL, MySQL, Oracle, Microsoft SQL Server, MariaDB and IBM Db2 out of the box.

A Kubernetes (or any other) readiness probe that starts polling `/health/readiness` the instant the HTTP
port opens can race the database driver: MongoDB's Server Discovery and Monitoring (SDAM) hasn't resolved
cluster topology yet, or the R2DBC connection pool hasn't opened its first connection. The result is a pod
that looks "ready" for a few seconds while every request against it 500s.

This library adds one `ApplicationEventListener<StartupEvent>` per datasource that:

1. Runs a real probe against it at startup (`ping` for MongoDB, `SELECT 1` for MySQL/PostgreSQL via R2DBC).
2. Retries with exponential backoff (capped) on failure, up to a configurable number of attempts.
3. On exhaustion, stops the `ApplicationContext` by default (fail-fast) - so a readiness probe never
   reports ready against a datasource that never came up - or just logs and continues, if you'd rather
   degrade than crash-loop.

Each observer is auto-detected by classpath and bean presence - add the dependency, add your usual
MongoDB/R2DBC configuration, and it activates on its own. Nothing to wire by hand.

## Using it

```groovy
repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri('https://maven.pkg.github.com/fernan-89/micronaut-datasource-warmup')
        credentials { username = System.getenv('GITHUB_ACTOR'); password = System.getenv('GITHUB_TOKEN') }
    }
}
dependencies { implementation 'io.github.fernan89:micronaut-datasource-warmup:0.1.0' }
```

GitHub Packages requires a token even for public packages (`read:packages` scope) - export
`GITHUB_ACTOR`/`GITHUB_TOKEN` before building, or run `./gradlew publishToMavenLocal` against a local
checkout of this repo and drop the GitHub Packages block entirely.

## Configuring it

Nothing is required beyond having a `MongoClient` or R2DBC `ConnectionFactory` bean in context - every
observer defaults to `enabled: true`, 10 attempts, a 5s initial backoff doubling up to a 60s cap, a 5s
per-attempt timeout, and fail-fast on exhaustion. Override only what you need:

```yaml
warmup:
  mongo:
    enabled: true
  mysql:
    enabled: true
    max-attempts: 10
  postgres:
    enabled: false
```

| Property (under `warmup.<engine>.*`) | Default | Meaning |
|---|---|---|
| `enabled` | `true` | Whether this datasource's observer runs at all. |
| `max-attempts` | `10` | Probe attempts before giving up. |
| `initial-backoff` | `5s` | Delay before the second attempt; doubles on each subsequent retry. |
| `max-backoff` | `60s` | Upper bound on the backoff delay. |
| `timeout` | `5s` | Per-attempt timeout. |
| `fail-fast` | `true` | Stop the `ApplicationContext` on exhaustion instead of just logging. |

`<engine>` is one of `mongo`, `mysql`, `postgres`, `oracle`, `sqlserver`, `mariadb` or `db2`.

Every relational engine shares a single observer implementation: it inspects each R2DBC
`ConnectionFactory` bean's own driver metadata (`ConnectionFactoryMetadata.getName()`) to decide which
`warmup.<engine>.*` block applies, and runs one instance per datasource if you configure more than one
(see `VendorMapping` for the exact name-matching rules). A `ConnectionFactory` whose vendor isn't
recognised is left alone - no error, just skipped, so adding a datasource this library doesn't yet know
about never breaks startup.

### Not yet covered

Redis, Elasticsearch and Cassandra round out most "top 10 production datastores" lists alongside the
relational engines and MongoDB above, but each speaks a genuinely different client API (Lettuce/Jedis,
the Elasticsearch Java client, the DataStax driver) rather than R2DBC or the MongoDB reactive streams
driver - supporting them means a dedicated observer per client, not just another `VendorMapping` entry.
Out of scope for this first version; contributions welcome.

## What's inside

| Package | Class | Purpose |
|---|---|---|
| `io.github.fernan89.warmup` | `WarmupProperties` | Configuration contract every datasource properties class implements |
| `io.github.fernan89.warmup` | `WarmupRunner` | The shared retry/backoff/fail-fast engine |
| `io.github.fernan89.warmup.mongo` | `MongoWarmupProperties` / `MongoWarmupObserver` | `warmup.mongo.*`, active when a reactive `MongoClient` bean is present |
| `io.github.fernan89.warmup.r2dbc` | `VendorMapping` | Maps an R2DBC driver's metadata name to its `@ConfigurationProperties` class |
| `io.github.fernan89.warmup.r2dbc` | `R2dbcWarmupObserver` | One instance per R2DBC `ConnectionFactory` bean, vendor-detected at runtime |
| `io.github.fernan89.warmup.r2dbc` | `MySqlWarmupProperties` / `PostgresWarmupProperties` / `OracleWarmupProperties` / `SqlServerWarmupProperties` / `MariaDbWarmupProperties` / `Db2WarmupProperties` | `warmup.mysql.*` / `warmup.postgres.*` / `warmup.oracle.*` / `warmup.sqlserver.*` / `warmup.mariadb.*` / `warmup.db2.*` |

Every dependency on a specific driver (`micronaut-mongo-reactive`, `mongodb-driver-reactivestreams`,
`r2dbc-spi`) is declared `compileOnly`: a consumer with only Mongo on the classpath never pulls in R2DBC,
and vice versa, and the matching observer simply never activates when its own driver is absent
(`@Requires(beans = ...)` / `@EachBean`).

## Build and test

```bash
./gradlew clean check    # unit tests + 100% line/branch coverage gate
```

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
