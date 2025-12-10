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

## 📌 Important Design Learnings

* reading without locking can return stale data
* optimistic update can safely block stale writes
* retry eliminates lost allocation
* SKIP LOCKED improves concurrency
* seat assignment is a real-world CAS pattern
* correctness can be achieved without blocking
