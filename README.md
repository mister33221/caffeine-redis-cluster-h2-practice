
# caffeine-redis-cluster-h2-practice

## 📌 專案簡介

本專案為 Spring Boot 範例，整合了 JPA、Caffeine 本地快取、Redis Cluster 分散式快取，以及 H2 內嵌資料庫。適合學習多層快取策略、資料同步與快取一致性設計。

---


## 🧱 技術棧

- Spring Boot
- Spring Data JPA
- Caffeine Cache
- Redis Cluster (Spring Data Redis)
- H2 Database (In-Memory)
- Maven
- Docker

---

src/

## 📁 專案結構

```
.
├── src/
│   ├── main/
│   │   ├── java/com/kai/caffeine_redis_cluster_h2_practice/
│   │   │   ├── config/         # 快取、Redis、Swagger 設定
│   │   │   ├── controller/     # REST API 控制器
│   │   │   ├── entity/         # JPA 實體
│   │   │   ├── repository/     # JPA Repository
│   │   │   ├── service/        # 業務邏輯與快取邏輯
│   │   │   └── CaffeineRedisClusterH2PracticeApplication.java
│   │   └── resources/
│   │       ├── application.yml # Spring Boot 設定
│   │       ├── data.sql        # 初始化資料
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       └── java/com/kai/caffeine_redis_cluster_h2_practice/
│           └── CaffeineRedisClusterH2PracticeApplicationTests.java
├── redis-cluster/
│   ├── node1/redis.conf
│   ├── node2/redis.conf
│   ├── node3/redis.conf
│   ├── node4/redis.conf
│   ├── node5/redis.conf
│   └── node6/redis.conf
├── docker-compose-redis-cluster.yml
├── docker-compose-redisinsight.yml
├── init-cluster.sh
├── create_index.sh
├── flush_redis_cluster.sh
├── insert_sample_data.sh
├── pom.xml
└── README.md
```

---


## ⚙️ 設定說明

- `application.yml` 設定 H2 資料庫、Redis Cluster 節點、JPA 參數與快取設定。
- Redis Cluster 節點預設為 `localhost:7000~7002`。
- H2 Console 可於 `http://localhost:8080/h2-console` 開啟。

---

## 🚀 快速啟動

1. 啟動 Redis Cluster（需先安裝 Docker）：
   ```bash
   docker-compose -f docker-compose-redis-cluster.yml up -d
   ```
2. 初始化 Redis Cluster（建議用 git-bash 或 WSL 執行）：
   ```bash
   sh init-cluster.sh
   ```
3. 啟動 RedisInsight（可視化管理 Redis）：
   ```bash
   docker-compose -f docker-compose-redisinsight.yml up -d
   ```
   - RedisInsight: http://localhost:5540
   - 連線資訊：`redis://host.docker.internal:7000`
4. 啟動 Spring Boot 專案（IDE 或 `./mvnw spring-boot:run`）

---

## 🗄️ 快取策略

- **Caffeine**：本地 LRU 快取，提升單機查詢效能。
- **Redis Cluster**：分散式快取，支援多節點與 failover。
- 可於 `CacheConfig.java` 切換三種快取策略（只用 Caffeine、只用 Redis、兩層快取）。

---

## 🧪 測試與 API

- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- RedisInsight: [http://localhost:5540](http://localhost:5540)

---


## 📝 其他說明

- `spring-boot-devtools` 可能導致快取類型轉換錯誤（ClassLoader 問題）。
- 支援 Redis Object 及 Redis Hash 兩種快取方式，適用不同場景。
- 內附多個 shell script，方便初始化、清除資料與建立索引。

### Redis Cluster + Redis Stack (RedisJSON/RediSearch) 限制說明

使用 Redis Cluster 架構時，若要搭配 Redis Stack 的 RedisJSON 與 RediSearch，會遇到「資料與索引必須在同一個 slot」的限制。這代表：

- 你要搜尋的 JSON 資料（如 user:{1001}）和對應的索引（如 idx:users）都必須被分配到同一個 slot（同一個 node）。
- 若將所有要搜尋的資料都設計成同一個 slot，會導致資料集中在單一節點，失去 Redis Cluster 分散式的優勢與高可用性。
- 在本專案中，可以看到成功 save JSON 資料，但 search 時因 index 與資料 slot 不同而查不到結果，這是 Redis Cluster 架構下的已知限制。

#### 解決方案展望

未來可考慮將 Redis 作為快取層，搭配 ElasticSearch 作為全文檢索引擎，讓資料分散於多個 Redis 節點，同時又能支援高效的分散式搜尋。之後會再實作一個「Redis + ElasticSearch」的專案來解決這類問題。

---

## 🧩 參考指令

- 啟動/關閉 Redis Cluster：
  ```bash
  docker-compose -f docker-compose-redis-cluster.yml up -d
  docker-compose -f docker-compose-redis-cluster.yml down
  ```
- 初始化 Redis Cluster：
  ```bash
  sh init-cluster.sh
  ```
- 啟動 RedisInsight：
  ```bash
  docker-compose -f docker-compose-redisinsight.yml up -d
  docker-compose -f docker-compose-redisinsight.yml down
  ```
- 清除所有 Redis 節點資料：
  ```bash
  docker exec redis-node1 redis-cli -p 7000 FLUSHDB
  docker exec redis-node2 redis-cli -p 7001 FLUSHDB
  docker exec redis-node3 redis-cli -p 7002 FLUSHDB
  docker exec redis-node4 redis-cli -p 7003 FLUSHDB
  docker exec redis-node5 redis-cli -p 7004 FLUSHDB
  docker exec redis-node6 redis-cli -p 7005 FLUSHDB
  ```
- 進入 Redis CLI（單體/Cluster）：
  ```bash
  docker exec -it redis-node1 redis-cli -p 7000
  docker exec -it redis-node1 redis-cli -p 7000 -c
  ```
- Redis JSON 操作範例：
  ```bash
  JSON.SET user:{1001} $ '{"user_id": 1001, "name": "Alice", "email": "alice@example.com"}'
  JSON.GET user:{1001}
  ```
- RediSearch 建立/查詢索引：
  ```bash
  FT.CREATE idx:users ON JSON PREFIX 1 "user:{1001}" SCHEMA $.name AS name TEXT $.email AS email TAG
  FT.SEARCH idx:users "@name:Alice"
  FT._LIST
  ```
- 查詢 slot 與節點：
  ```bash
  CLUSTER KEYSLOT user:{1001}
  CLUSTER NODES
  ```
- 插入範例資料：
  ```bash
  sh insert_sample_data.sh
  ```
- 建立 RediSearch 索引：
  ```bash
  sh create_index.sh
  ```
- 快取測試 API 可參考 Swagger UI。
