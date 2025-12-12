## Contents

- [Distributed Key Value Store](#Distributed Key-Value Store (Spring Boot + MySQL))
- [Airline Seat Booking – Concurrency Demonstration](#Airline-Seat-Booking–Concurrency-Demonstration) 
# 🚀 Distributed Key-Value Store (Spring Boot + MySQL)

> Ignite-inspired distributed caching & routing model running locally using Spring Boot, Java 21, and MySQL.

---

## 🧠 Overview

This project implements a **mini distributed key-value store**, inspired by **Apache Ignite**, built using:

* **Java 21**
* **Spring Boot**
* **MySQL**
* **Affinity-based key routing**
* **Partitioning**
* **Replication**
* **Multi-node cluster simulation**

Even though everything runs locally, multiple Spring Boot instances behave like a **distributed cluster**.

---

## ✨ Features

✔ Get / Put / Delete
✔ Partition-based routing
✔ Replication to backups
✔ Node-to-node forwarding
✔ Data expiry (TTL)
✔ Supports BLOB values
✔ Sharding-ready architecture
✔ Ignite-style affinity function

---

# 🏗 Architecture

```
Client → Node (Primary) → DB
                \
                 → Backup nodes
```

### Primary Node

* owns a subset of partitions
* handles writes/reads
* forwards replication to backups

### Backup Node

* stores replicated data
* can serve reads if primary fails

### Database

* persists all values
* TTL stored in DB layer
* partitions stored for analysis

---

# 🔁 Routing logic (Affinity Function)

Every key maps to a logical partition:

```
partitionId = hash(key) % PARTITIONS
```

Then each partition is assigned a list of nodes:

```
owners = [primaryNode, backupNode...]
```

So requests are forwarded automatically to the
**correct owning node**.

---

# 🗂 Folder structure

```
src/
└── main/java/com/example/distributedkv
      ├── controller
      ├── service
      ├── repository
      ├── entity
      ├── affinity
      ├── internal
```

---

# 🔌 API Endpoints

### GET value

```sh
GET /kv/{key}
```

### PUT value

```sh
POST /kv/{key}?ttlSec=60
```

### DELETE value

```sh
DELETE /kv/{key}
```

---

# 🧩 Internal Endpoints (node-to-node)

Used only for replication and forwarding:

```
/internal/kv/{key}
```

---

# 👯 Run multiple nodes locally

### Node 1

```
mvn spring-boot:run -Dspring-boot.run.profiles=node1
```

### Node 2

```
mvn spring-boot:run -Dspring-boot.run.profiles=node2
```

Both will join cluster automatically.

---

# ⚙ Configuration example

### application-node1.properties

```
cluster.nodeId=node1
server.port=8081
```

### application-node2.properties

```
cluster.nodeId=node2
server.port=8082
```

---

# 🗃 MySQL Schema

```sql
CREATE TABLE kv_store (
  k            VARCHAR(255) PRIMARY KEY,
  v            BLOB NOT NULL,
  expire_at    DATETIME NULL,
  partition_id INT NOT NULL,
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_partition (partition_id),
  INDEX idx_expire_at (expire_at)
);
```

---

# 🔥 Why a partition count? (like Ignite)

We pick:

```
PARTITIONS = 32
```

NOT equal to number of nodes.

Reason:

* avoids full rehashing on node scaling
* allows incremental partition rebalancing
* matches Ignite/Cassandra design principles

---

# 🔄 Replication Model

When a write happens:

```
Primary writes locally
→ forwards to backup nodes
→ backup writes local copy (overwrite)
```

This provides:

* durability
* availability
* redundancy

Identical to Ignite “backup copies”.

---

# 🧠 Write semantics

```
repo.save(entity)
```

> is equivalent to a **PUT** in KV language
> (insert or update: UPSET)

Backups intentionally overwrite previous value.

---

# 🌟 What this project teaches

✔ distributed routing
✔ logical partitions
✔ affinity mapping
✔ backup replication
✔ consistency guarantees
✔ fault tolerance patterns
✔ local cluster simulation
✔ sharding-ready design

---

# 🧩 Next steps (already prepared by design)

Future extensions:

* DB sharding
* Consistent hashing
* Gossip membership
* Vector clocks
* CRDT merge
* Rebalancing on node join/leave
* Write-ahead log
* Eventual consistency mode
* Read-repair

---

# 🤝 Inspired by

* Apache Ignite
* DynamoDB
* Cassandra
* Redis Cluster
* Kafka partitions

---
# 🚀 Airline-Seat-Booking–Concurrency-Demonstration

This module demonstrates how 120 concurrent users attempt to book 120 airline seats and why, without proper concurrency control, seat allocation can result in:

* duplicate booking attempts,
* stale reads,
* race conditions,
* inconsistent updates,
* lost seats,
* OR (if retries are applied) **eventual consistent allocation** through optimistic concurrency.

The purpose is educational—not to build a commercial booking system—but to observe effects of database concurrency behavior similar to ticketing/airline systems.

---

## 🧠 What the example shows

### Initially:

* all 120 seats in DB are free (`user_id = NULL`)
* 120 users attempt booking in parallel (each thread = one user)


## 🧩 Key Observations

| Case                         | Result                                        |
| ---------------------------- | --------------------------------------------- |
| No locks + No retry          | lost seats, inconsistent allocation           |
| No locks + Retry             | eventual success, no double-booking           |
| SELECT … FOR UPDATE          | pessimistic locking + slow throughput         |
| SKIP LOCKED                  | more parallel allocation than FOR UPDATE only |
| Optimistic check (`IS NULL`) | prevents duplicates safely                    |

---

## Try to run two instances of 4th case simultaneously

Remove resetSeats from code and explicitly set seats to null in the DB to test this behaviour. Here is the o/p of two instances running the same code simultaneously.

```/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin/java -javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=56490 -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath /Users/neha/Documents/code/sample-projects/target/classes:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-starter-webmvc/4.0.0/spring-boot-starter-webmvc-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-starter/4.0.0/spring-boot-starter-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-starter-logging/4.0.0/spring-boot-starter-logging-4.0.0.jar:/Users/neha/.m2/repository/ch/qos/logback/logback-classic/1.5.21/logback-classic-1.5.21.jar:/Users/neha/.m2/repository/ch/qos/logback/logback-core/1.5.21/logback-core-1.5.21.jar:/Users/neha/.m2/repository/org/apache/logging/log4j/log4j-to-slf4j/2.25.2/log4j-to-slf4j-2.25.2.jar:/Users/neha/.m2/repository/org/apache/logging/log4j/log4j-api/2.25.2/log4j-api-2.25.2.jar:/Users/neha/.m2/repository/org/slf4j/jul-to-slf4j/2.0.17/jul-to-slf4j-2.0.17.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-autoconfigure/4.0.0/spring-boot-autoconfigure-4.0.0.jar:/Users/neha/.m2/repository/jakarta/annotation/jakarta.annotation-api/3.0.0/jakarta.annotation-api-3.0.0.jar:/Users/neha/.m2/repository/org/yaml/snakeyaml/2.5/snakeyaml-2.5.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-starter-jackson/4.0.0/spring-boot-starter-jackson-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-jackson/4.0.0/spring-boot-jackson-4.0.0.jar:/Users/neha/.m2/repository/tools/jackson/core/jackson-databind/3.0.2/jackson-databind-3.0.2.jar:/Users/neha/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.20/jackson-annotations-2.20.jar:/Users/neha/.m2/repository/tools/jackson/core/jackson-core/3.0.2/jackson-core-3.0.2.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-http-converter/4.0.0/spring-boot-http-converter-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/spring-web/7.0.1/spring-web-7.0.1.jar:/Users/neha/.m2/repository/io/micrometer/micrometer-observation/1.16.0/micrometer-observation-1.16.0.jar:/Users/neha/.m2/repository/io/micrometer/micrometer-commons/1.16.0/micrometer-commons-1.16.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-webmvc/4.0.0/spring-boot-webmvc-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-servlet/4.0.0/spring-boot-servlet-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/spring-webmvc/7.0.1/spring-webmvc-7.0.1.jar:/Users/neha/.m2/repository/org/springframework/spring-aop/7.0.1/spring-aop-7.0.1.jar:/Users/neha/.m2/repository/org/springframework/spring-expression/7.0.1/spring-expression-7.0.1.jar:/Users/neha/.m2/repository/org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar:/Users/neha/.m2/repository/jakarta/xml/bind/jakarta.xml.bind-api/4.0.4/jakarta.xml.bind-api-4.0.4.jar:/Users/neha/.m2/repository/jakarta/activation/jakarta.activation-api/2.1.4/jakarta.activation-api-2.1.4.jar:/Users/neha/.m2/repository/net/bytebuddy/byte-buddy/1.17.8/byte-buddy-1.17.8.jar:/Users/neha/.m2/repository/org/springframework/spring-core/7.0.1/spring-core-7.0.1.jar:/Users/neha/.m2/repository/commons-logging/commons-logging/1.3.5/commons-logging-1.3.5.jar:/Users/neha/.m2/repository/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-starter-data-jpa/4.0.0/spring-boot-starter-data-jpa-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-starter-jdbc/4.0.0/spring-boot-starter-jdbc-4.0.0.jar:/Users/neha/.m2/repository/com/zaxxer/HikariCP/7.0.2/HikariCP-7.0.2.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-data-jpa/4.0.0/spring-boot-data-jpa-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-data-commons/4.0.0/spring-boot-data-commons-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-persistence/4.0.0/spring-boot-persistence-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/data/spring-data-commons/4.0.0/spring-data-commons-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-hibernate/4.0.0/spring-boot-hibernate-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-jpa/4.0.0/spring-boot-jpa-4.0.0.jar:/Users/neha/.m2/repository/jakarta/persistence/jakarta.persistence-api/3.2.0/jakarta.persistence-api-3.2.0.jar:/Users/neha/.m2/repository/org/hibernate/orm/hibernate-core/7.1.8.Final/hibernate-core-7.1.8.Final.jar:/Users/neha/.m2/repository/jakarta/transaction/jakarta.transaction-api/2.0.1/jakarta.transaction-api-2.0.1.jar:/Users/neha/.m2/repository/org/jboss/logging/jboss-logging/3.6.1.Final/jboss-logging-3.6.1.Final.jar:/Users/neha/.m2/repository/org/hibernate/models/hibernate-models/1.0.1/hibernate-models-1.0.1.jar:/Users/neha/.m2/repository/com/fasterxml/classmate/1.7.1/classmate-1.7.1.jar:/Users/neha/.m2/repository/org/glassfish/jaxb/jaxb-runtime/4.0.6/jaxb-runtime-4.0.6.jar:/Users/neha/.m2/repository/org/glassfish/jaxb/jaxb-core/4.0.6/jaxb-core-4.0.6.jar:/Users/neha/.m2/repository/org/eclipse/angus/angus-activation/2.0.3/angus-activation-2.0.3.jar:/Users/neha/.m2/repository/org/glassfish/jaxb/txw2/4.0.6/txw2-4.0.6.jar:/Users/neha/.m2/repository/com/sun/istack/istack-commons-runtime/4.1.2/istack-commons-runtime-4.1.2.jar:/Users/neha/.m2/repository/jakarta/inject/jakarta.inject-api/2.0.1/jakarta.inject-api-2.0.1.jar:/Users/neha/.m2/repository/org/springframework/spring-orm/7.0.1/spring-orm-7.0.1.jar:/Users/neha/.m2/repository/org/springframework/data/spring-data-jpa/4.0.0/spring-data-jpa-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/spring-tx/7.0.1/spring-tx-7.0.1.jar:/Users/neha/.m2/repository/org/antlr/antlr4-runtime/4.13.2/antlr4-runtime-4.13.2.jar:/Users/neha/.m2/repository/org/springframework/spring-aspects/7.0.1/spring-aspects-7.0.1.jar:/Users/neha/.m2/repository/org/aspectj/aspectjweaver/1.9.25/aspectjweaver-1.9.25.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-jdbc/4.0.0/spring-boot-jdbc-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-sql/4.0.0/spring-boot-sql-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-transaction/4.0.0/spring-boot-transaction-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/spring-jdbc/7.0.1/spring-jdbc-7.0.1.jar:/Users/neha/.m2/repository/com/mysql/mysql-connector-j/9.5.0/mysql-connector-j-9.5.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-webclient/4.0.0/spring-boot-webclient-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot/4.0.0/spring-boot-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/spring-context/7.0.1/spring-context-7.0.1.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-http-client/4.0.0/spring-boot-http-client-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/spring-webflux/7.0.1/spring-webflux-7.0.1.jar:/Users/neha/.m2/repository/org/springframework/spring-beans/7.0.1/spring-beans-7.0.1.jar:/Users/neha/.m2/repository/io/projectreactor/reactor-core/3.8.0/reactor-core-3.8.0.jar:/Users/neha/.m2/repository/org/reactivestreams/reactive-streams/1.0.4/reactive-streams-1.0.4.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-http-codec/4.0.0/spring-boot-http-codec-4.0.0.jar com.example.airline_seat_booking.AirlineBookingDemo4
SUCCESS user 14 booked 10A
SUCCESS user 53 booked 11D
SUCCESS user 11 booked 10B
SUCCESS user 24 booked 10D
SUCCESS user 30 booked 10C
SUCCESS user 45 booked 9F
SUCCESS user 26 booked 11B
SUCCESS user 10 booked 9E
NO SEATS LEFT for user 55
NO SEATS LEFT for user 62
NO SEATS LEFT for user 56
SUCCESS user 59 booked 12A
SUCCESS user 23 booked 11A
SUCCESS user 27 booked 10E
NO SEATS LEFT for user 38
NO SEATS LEFT for user 39
SUCCESS user 58 booked 11F
NO SEATS LEFT for user 46
NO SEATS LEFT for user 2
NO SEATS LEFT for user 41
SUCCESS user 29 booked 11E
NO SEATS LEFT for user 33
NO SEATS LEFT for user 42
NO SEATS LEFT for user 36
NO SEATS LEFT for user 64
NO SEATS LEFT for user 40
NO SEATS LEFT for user 16
NO SEATS LEFT for user 44
NO SEATS LEFT for user 1
SUCCESS user 51 booked 12F
NO SEATS LEFT for user 31
SUCCESS user 47 booked 10F
SUCCESS user 57 booked 13B
SUCCESS user 52 booked 12D
NO SEATS LEFT for user 18
NO SEATS LEFT for user 6
SUCCESS user 61 booked 13A
NO SEATS LEFT for user 4
NO SEATS LEFT for user 15
NO SEATS LEFT for user 12
NO SEATS LEFT for user 7
NO SEATS LEFT for user 9
NO SEATS LEFT for user 48
NO SEATS LEFT for user 5
NO SEATS LEFT for user 68
NO SEATS LEFT for user 3
NO SEATS LEFT for user 70
NO SEATS LEFT for user 35
NO SEATS LEFT for user 25
NO SEATS LEFT for user 37
NO SEATS LEFT for user 32
NO SEATS LEFT for user 49
NO SEATS LEFT for user 8
NO SEATS LEFT for user 50
NO SEATS LEFT for user 34
NO SEATS LEFT for user 19
NO SEATS LEFT for user 63
NO SEATS LEFT for user 28
NO SEATS LEFT for user 13
SUCCESS user 60 booked 12B
SUCCESS user 54 booked 12E
All tasks completed in 417 ms

Process finished with exit code 0
```
Second Instance:
/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin/java -javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=56494 -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath /Users/neha/Documents/code/sample-projects/target/classes:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-starter-webmvc/4.0.0/spring-boot-starter-webmvc-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-starter/4.0.0/spring-boot-starter-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-starter-logging/4.0.0/spring-boot-starter-logging-4.0.0.jar:/Users/neha/.m2/repository/ch/qos/logback/logback-classic/1.5.21/logback-classic-1.5.21.jar:/Users/neha/.m2/repository/ch/qos/logback/logback-core/1.5.21/logback-core-1.5.21.jar:/Users/neha/.m2/repository/org/apache/logging/log4j/log4j-to-slf4j/2.25.2/log4j-to-slf4j-2.25.2.jar:/Users/neha/.m2/repository/org/apache/logging/log4j/log4j-api/2.25.2/log4j-api-2.25.2.jar:/Users/neha/.m2/repository/org/slf4j/jul-to-slf4j/2.0.17/jul-to-slf4j-2.0.17.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-autoconfigure/4.0.0/spring-boot-autoconfigure-4.0.0.jar:/Users/neha/.m2/repository/jakarta/annotation/jakarta.annotation-api/3.0.0/jakarta.annotation-api-3.0.0.jar:/Users/neha/.m2/repository/org/yaml/snakeyaml/2.5/snakeyaml-2.5.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-starter-jackson/4.0.0/spring-boot-starter-jackson-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-jackson/4.0.0/spring-boot-jackson-4.0.0.jar:/Users/neha/.m2/repository/tools/jackson/core/jackson-databind/3.0.2/jackson-databind-3.0.2.jar:/Users/neha/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.20/jackson-annotations-2.20.jar:/Users/neha/.m2/repository/tools/jackson/core/jackson-core/3.0.2/jackson-core-3.0.2.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-http-converter/4.0.0/spring-boot-http-converter-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/spring-web/7.0.1/spring-web-7.0.1.jar:/Users/neha/.m2/repository/io/micrometer/micrometer-observation/1.16.0/micrometer-observation-1.16.0.jar:/Users/neha/.m2/repository/io/micrometer/micrometer-commons/1.16.0/micrometer-commons-1.16.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-webmvc/4.0.0/spring-boot-webmvc-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-servlet/4.0.0/spring-boot-servlet-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/spring-webmvc/7.0.1/spring-webmvc-7.0.1.jar:/Users/neha/.m2/repository/org/springframework/spring-aop/7.0.1/spring-aop-7.0.1.jar:/Users/neha/.m2/repository/org/springframework/spring-expression/7.0.1/spring-expression-7.0.1.jar:/Users/neha/.m2/repository/org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar:/Users/neha/.m2/repository/jakarta/xml/bind/jakarta.xml.bind-api/4.0.4/jakarta.xml.bind-api-4.0.4.jar:/Users/neha/.m2/repository/jakarta/activation/jakarta.activation-api/2.1.4/jakarta.activation-api-2.1.4.jar:/Users/neha/.m2/repository/net/bytebuddy/byte-buddy/1.17.8/byte-buddy-1.17.8.jar:/Users/neha/.m2/repository/org/springframework/spring-core/7.0.1/spring-core-7.0.1.jar:/Users/neha/.m2/repository/commons-logging/commons-logging/1.3.5/commons-logging-1.3.5.jar:/Users/neha/.m2/repository/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-starter-data-jpa/4.0.0/spring-boot-starter-data-jpa-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-starter-jdbc/4.0.0/spring-boot-starter-jdbc-4.0.0.jar:/Users/neha/.m2/repository/com/zaxxer/HikariCP/7.0.2/HikariCP-7.0.2.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-data-jpa/4.0.0/spring-boot-data-jpa-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-data-commons/4.0.0/spring-boot-data-commons-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-persistence/4.0.0/spring-boot-persistence-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/data/spring-data-commons/4.0.0/spring-data-commons-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-hibernate/4.0.0/spring-boot-hibernate-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-jpa/4.0.0/spring-boot-jpa-4.0.0.jar:/Users/neha/.m2/repository/jakarta/persistence/jakarta.persistence-api/3.2.0/jakarta.persistence-api-3.2.0.jar:/Users/neha/.m2/repository/org/hibernate/orm/hibernate-core/7.1.8.Final/hibernate-core-7.1.8.Final.jar:/Users/neha/.m2/repository/jakarta/transaction/jakarta.transaction-api/2.0.1/jakarta.transaction-api-2.0.1.jar:/Users/neha/.m2/repository/org/jboss/logging/jboss-logging/3.6.1.Final/jboss-logging-3.6.1.Final.jar:/Users/neha/.m2/repository/org/hibernate/models/hibernate-models/1.0.1/hibernate-models-1.0.1.jar:/Users/neha/.m2/repository/com/fasterxml/classmate/1.7.1/classmate-1.7.1.jar:/Users/neha/.m2/repository/org/glassfish/jaxb/jaxb-runtime/4.0.6/jaxb-runtime-4.0.6.jar:/Users/neha/.m2/repository/org/glassfish/jaxb/jaxb-core/4.0.6/jaxb-core-4.0.6.jar:/Users/neha/.m2/repository/org/eclipse/angus/angus-activation/2.0.3/angus-activation-2.0.3.jar:/Users/neha/.m2/repository/org/glassfish/jaxb/txw2/4.0.6/txw2-4.0.6.jar:/Users/neha/.m2/repository/com/sun/istack/istack-commons-runtime/4.1.2/istack-commons-runtime-4.1.2.jar:/Users/neha/.m2/repository/jakarta/inject/jakarta.inject-api/2.0.1/jakarta.inject-api-2.0.1.jar:/Users/neha/.m2/repository/org/springframework/spring-orm/7.0.1/spring-orm-7.0.1.jar:/Users/neha/.m2/repository/org/springframework/data/spring-data-jpa/4.0.0/spring-data-jpa-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/spring-tx/7.0.1/spring-tx-7.0.1.jar:/Users/neha/.m2/repository/org/antlr/antlr4-runtime/4.13.2/antlr4-runtime-4.13.2.jar:/Users/neha/.m2/repository/org/springframework/spring-aspects/7.0.1/spring-aspects-7.0.1.jar:/Users/neha/.m2/repository/org/aspectj/aspectjweaver/1.9.25/aspectjweaver-1.9.25.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-jdbc/4.0.0/spring-boot-jdbc-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-sql/4.0.0/spring-boot-sql-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-transaction/4.0.0/spring-boot-transaction-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/spring-jdbc/7.0.1/spring-jdbc-7.0.1.jar:/Users/neha/.m2/repository/com/mysql/mysql-connector-j/9.5.0/mysql-connector-j-9.5.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-webclient/4.0.0/spring-boot-webclient-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot/4.0.0/spring-boot-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/spring-context/7.0.1/spring-context-7.0.1.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-http-client/4.0.0/spring-boot-http-client-4.0.0.jar:/Users/neha/.m2/repository/org/springframework/spring-webflux/7.0.1/spring-webflux-7.0.1.jar:/Users/neha/.m2/repository/org/springframework/spring-beans/7.0.1/spring-beans-7.0.1.jar:/Users/neha/.m2/repository/io/projectreactor/reactor-core/3.8.0/reactor-core-3.8.0.jar:/Users/neha/.m2/repository/org/reactivestreams/reactive-streams/1.0.4/reactive-streams-1.0.4.jar:/Users/neha/.m2/repository/org/springframework/boot/spring-boot-http-codec/4.0.0/spring-boot-http-codec-4.0.0.jar com.example.airline_seat_booking.AirlineBookingDemo4Copy
SUCCESS user 4 booked 1B
SUCCESS user 17 booked 1D
SUCCESS user 22 booked 1F
SUCCESS user 12 booked 1E
SUCCESS user 10 booked 2B
SUCCESS user 14 booked 2A
SUCCESS user 1 booked 1A
SUCCESS user 5 booked 1C
SUCCESS user 25 booked 2C
SUCCESS user 44 booked 2E
SUCCESS user 41 booked 6B
SUCCESS user 18 booked 7A
SUCCESS user 11 booked 5A
SUCCESS user 13 booked 8B
SUCCESS user 21 booked 7B
SUCCESS user 36 booked 3F
SUCCESS user 46 booked 8C
SUCCESS user 47 booked 8D
SUCCESS user 15 booked 6E
SUCCESS user 20 booked 7E
SUCCESS user 40 booked 7F
SUCCESS user 42 booked 7D
SUCCESS user 2 booked 2D
SUCCESS user 6 booked 4D
SUCCESS user 7 booked 3A
SUCCESS user 43 booked 8A
SUCCESS user 8 booked 4C
SUCCESS user 26 booked 3B
SUCCESS user 3 booked 3C
SUCCESS user 35 booked 3E
SUCCESS user 51 booked 3D
SUCCESS user 48 booked 7C
SUCCESS user 32 booked 2F
SUCCESS user 31 booked 4B
SUCCESS user 49 booked 6C
SUCCESS user 45 booked 5C
SUCCESS user 29 booked 4A
SUCCESS user 33 booked 6A
SUCCESS user 37 booked 6F
SUCCESS user 19 booked 5B
SUCCESS user 28 booked 5F
SUCCESS user 52 booked 9C
SUCCESS user 30 booked 6D
SUCCESS user 9 booked 5D
SUCCESS user 39 booked 5E
SUCCESS user 27 booked 4F
SUCCESS user 24 booked 4E
SUCCESS user 38 booked 8F
SUCCESS user 16 booked 9A
SUCCESS user 34 booked 9D
SUCCESS user 23 booked 9B
SUCCESS user 50 booked 8E
SUCCESS user 93 booked 11C
SUCCESS user 54 booked 12C
SUCCESS user 53 booked 13C
SUCCESS user 55 booked 13D
SUCCESS user 58 booked 14A
SUCCESS user 59 booked 13E
SUCCESS user 66 booked 14D
SUCCESS user 56 booked 14B
SUCCESS user 62 booked 14C
SUCCESS user 57 booked 13F
SUCCESS user 63 booked 14F
SUCCESS user 60 booked 14E
SUCCESS user 64 booked 15A
SUCCESS user 65 booked 15B
SUCCESS user 67 booked 15C
SUCCESS user 70 booked 15D
SUCCESS user 69 booked 15E
SUCCESS user 68 booked 15F
SUCCESS user 71 booked 16C
SUCCESS user 73 booked 16A
SUCCESS user 74 booked 16D
SUCCESS user 72 booked 16B
SUCCESS user 75 booked 16E
SUCCESS user 77 booked 16F
SUCCESS user 78 booked 17B
SUCCESS user 76 booked 17A
SUCCESS user 79 booked 17C
SUCCESS user 80 booked 17E
SUCCESS user 81 booked 17D
SUCCESS user 82 booked 17F
SUCCESS user 84 booked 18B
SUCCESS user 85 booked 18C
SUCCESS user 86 booked 19A
SUCCESS user 90 booked 18E
SUCCESS user 83 booked 18A
SUCCESS user 87 booked 18D
SUCCESS user 89 booked 18F
SUCCESS user 88 booked 19B
SUCCESS user 92 booked 19C
SUCCESS user 94 booked 19D
SUCCESS user 91 booked 19F
SUCCESS user 61 booked 19E
SUCCESS user 95 booked 20A
SUCCESS user 97 booked 20B
SUCCESS user 96 booked 20C
NO SEATS LEFT for user 101
NO SEATS LEFT for user 102
SUCCESS user 99 booked 20D
NO SEATS LEFT for user 103
NO SEATS LEFT for user 104
SUCCESS user 98 booked 20E
NO SEATS LEFT for user 106
NO SEATS LEFT for user 105
SUCCESS user 100 booked 20F
NO SEATS LEFT for user 110
NO SEATS LEFT for user 113
NO SEATS LEFT for user 116
NO SEATS LEFT for user 118
NO SEATS LEFT for user 120
NO SEATS LEFT for user 108
NO SEATS LEFT for user 109
NO SEATS LEFT for user 119
NO SEATS LEFT for user 117
NO SEATS LEFT for user 107
NO SEATS LEFT for user 115
NO SEATS LEFT for user 112
NO SEATS LEFT for user 111
NO SEATS LEFT for user 114
All tasks completed in 151 ms

Process finished with exit code 0

- As you could see seat once booked cant be rebooked due to constraint is user_id is null.
- Also, both instances were able to book all the seats without any duplicates.
- This shows that optimistic concurrency with retry and IS NULL check works even when two instances are running simultaneously.
- This is because both instances will try to book the same seat but only one will succeed as the other will find that user_id is no longer null and will retry for another seat.
- Thus, even with multiple instances running the same code simultaneously, we can achieve correct seat allocation without any duplicates.
- Also, use of pesimistic lock with SKIP LOCKED ensures that no two transactions block each other while trying to book the same seat.

## 📌 Important Design Learnings

* reading without locking can return stale data
* optimistic update can safely block stale writes
* retry eliminates lost allocation
* SKIP LOCKED improves concurrency
* seat assignment is a real-world CAS pattern
* correctness can be achieved without blocking
