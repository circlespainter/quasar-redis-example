# Quasar Redis Example

## Getting Started

Redis and `make` must be installed to run the tests. `make test` will run a get/set example and a pub-sub one.

## Quasar and Redis integration

[Redis](http://redis.io) is an in-memory data structure store, used as a database, cache and message broker.

The `comsat-redis` Comsat module is compatible with JDK 8 and higher; it uses [Lettuce's async API](https://github.com/mp911de/lettuce/wiki/Asynchronous-API-%284.0%29), which is based on [JDK8's CompletableStage](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletionStage.html), to offer a fiber-blocking [Jedis](https://github.com/xetorthio/jedis/wiki/Getting-started) API. At present it supports most basic commands and Redis pub-sub.

The `co.paralleluniverse.fibers.redis.BinaryJedis`, `Jedis`, `BinaryJedisPubSub` and `JedisPubSub` classes extend the corresponding Jedis' ones so in most cases using the integration is as simple as, including the module as a compile-time dependency, [enabling Quasar instrumentation](http://docs.paralleluniverse.co/quasar/#instrumentation) and replacing references to the `redis.clients.jedis` package with `co.paralleluniverse.fibers.redis`.

There are some difference between `comsat-redis` and Jedis:

- When invoked from fibers `comsat-redis` will block fibers while from threads it will be block threads; this means it's generally better to use it inside fibers in order to get maximum concurrency capacity and efficiency but it can also be used as a traditional Jedis driver from Java threads.
- When using pub-sub, the callback handlers have to extend Comsat's `BinaryJedisPubSub` or `JedisPubSub` rather than Jedis' (usually it's just a matter of switching package). Also, each callback method will run in a fiber and will have to be annotated as `@Suspendable` if it calls fiber-blocking methods.
- Lettuce's driver is thread-safe for basic commands so there's no need for a driver pool: a single `Jedis` instance will suffice in most cases.

Here's a complete list of features that are currently supported:

- Pub-sub
- All basic commands and operations except geo commands, `getClient`, `zrevrangeByLex`, `geohash`, `rpushx`, `lpushx`, `spop` with count, `zincrby` with count, scan commands with parameters, bit operations with parameters, zstore operations with parameters and Jedis-deprecated calls.

Clustering, sharding, replication, transaction/multi, pipelining, scripting, monitoring, driver pooling and all other features not explicitly listed above are not yet supported.
