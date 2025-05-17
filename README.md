# Spring Boot Sample Project: JPA + Caffeine + Redis Cluster + H2

## 📌 專案簡介

一個展示 Spring Boot 整合 Spring Data JPA、Caffeine 本地快取、Redis Cluster 分散式快取，以及內嵌 H2 資料庫的範例專案，適合學習如何組合快取策略與資料層架構。

---

## 🧱 技術棧

* Spring Boot
* Spring Data JPA
* Caffeine Cache
* Redis Cluster (Spring Data Redis)
* H2 Database (In-Memory)
* Maven

---

## 📁 專案結構

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── config/         # Redis, Caffeine, Swagger 等配置類
│   │   ├── controller/     # REST API 控制器
│   │   ├── entity/         # JPA 實體
│   │   ├── repository/     # JPA Repository
│   │   ├── service/        # 業務邏輯與快取邏輯
│   │   └── DemoApplication.java
│   └── resources/
│       ├── application.yml # 設定檔
│       └── data.sql        # 初始化資料
├── redis-cluster/
│   ├── node1/redis.conf    # Redis node1 設定（port 7000）
│   ├── node2/redis.conf    # Redis node2 設定（port 7001）
│   ├── node3/redis.conf    # Redis node3 設定（port 7002）
│   ├── node4/redis.conf    # Redis node4 設定（port 7003）
│   ├── node5/redis.conf    # Redis node5 設定（port 7004）
│   └── node6/redis.conf    # Redis node6 設定（port 7005）
├── docker-compose.yml      # 建立 Redis Cluster 的容器設定檔
```

---

## 快取策略設計

* **Caffeine：** 用於快速本機查詢的 LRU(Least Recently Used) 本地快取。
* **Redis Cluster：** 分散式共享快取，支援 failover 與資料持久性，適合多節點部署。

---

## 設定說明

### application.yml

* 設定 H2 資料庫
* 設定 Redis Cluster 節點
* 設定 Caffeine 快取參數（最大容量、TTL）

---

## 如何啟動

1. 已經寫好 `docker-compose-redis-cluster.yml` 放在專案根目錄中，使用 docker 啟動 三主三從的 Redis Cluster：
   ```bash
   docker-compose -f docker-compose-redis-cluster.yml up -d
   ```
2. 我也寫了一個用來開啟 redisinsight 的 docker-compose 檔案，方便觀察 Redis Cluster 的狀態：
   ```bash
   docker-compose -f docker-compose-redisinsight.yml up -d
   ```
   - RedisInsight 預設會在 `http://localhost:5540` 開啟
3. 也寫好了 init-cluster.sh，其中會先檢查 6 個 redis 在 port 7000-7005 是否都以成功啟動，接著就會自動建立 cluster，並且將 3 個主節點的 slot 分配給 6 個 redis 節點，最後會檢查 cluster 是否成功建立。若是在 windows 環境，可以使用 git-bash 或者 WSL 來執行 sh 這個腳本。
   ```bash
   sh init-cluster.sh
   ```
4. 接著用你的 IDE 啟動專案就可以囉

- 開啟 h2 console 
   ```bash
   http://localhost:8080/h2-console
   ```
   - JDBC URL: `jdbc:h2:mem:testdb`
   - User Name: `sa`
   - Password: `password`
- 開啟 RedisInsight
   ```bash
   http://localhost:5540
   ```
   - 因為都是用 docker 啟動的，所以 host 要用 `host.docker.internal`，這樣 RedisInsight 才能連到 Redis Cluster
   - Redis Cluster 連線資訊：`redis://host.docker.internal:7000`
- 開啟 Swagger
   ```bash
   http://localhost:8080/swagger-ui/index.html
   ```
   - Swagger UI 會列出所有的 API，並且可以直接測試

---

## 快取邏輯

- 在 config 資料夾中有 CacheConfig.java，我在這個 class 中，實作了三種策略。可以自行把註解打開來測試不同的快取邏輯：
  1. 只使用 Caffeine 快取
  2. 只使用 Redis 快取
  3. 先取用 Caffeine 快取，若沒有再取用 Redis 快取

---

note

- spring-boot-devtools 會導致 redis cache 的問題。是 Spring Boot 開發中常見的 DevTools 類別載入器衝突問題。快取（如 Caffeine）將 A 類別的物件快取住，但熱重新編譯後 JVM 把 A 類別重新載入成 不同的版本，導致無法 cast 回原型。
```bash
ClassCastException: class ...User cannot be cast to class ...User
(...User is in unnamed module of loader 'app'; ...User is in unnamed module of loader org.springframework.boot.devtools.restart.classloader.RestartClassLoader) 
```
## Redis Object vs Redis Hash 快取比較

| 項目                   | Redis Object 快取（序列化物件）        | Redis Hash 快取（HSET 欄位） |
| -------------------- | ----------------------------- | ---------------------- |
| 資料結構                 | 一個 key 對應一個序列化物件              | 一個 key 對應多個欄位          |
| 可讀性                  | ❌ 不可讀（JDK 或 JSON 序列化）         | ✅ 可讀（純字串欄位）            |
| 單欄位查詢/更新             | ❌ 只能整筆更新/查詢                   | ✅ 支援 `HGET`、`HSET` 操作  |
| Spring @Cacheable 支援 | ✅ 完全支援                        | ❌ 需手動操作 RedisTemplate  |
| 跨語言可用性               | ❌ 需 JVM 類別與序列化一致              | ✅ JSON 可跨語言存取          |
| 適用情境                 | 快取整個 Java 物件（通常配合 @Cacheable） | 儲存結構化資料或部分欄位存取需求       |

### 🟩 適用情境建議：

* ✅ **使用 Redis Object 快取：**

   * 搭配 Spring `@Cacheable` 快取整筆查詢結果
   * 不需要查詢或修改部分欄位
   * 快速整體序列化、反序列化即可

* ✅ **使用 Redis Hash（HSET）：**

   * 資料欄位明確、可拆分（如商品名稱、市值）
   * 須查詢或更新單一欄位（如只更新 marketValue）
   * 須跨語言操作（JSON 格式友好）

---

### 🔄 關聯式資料庫資料轉移至 Redis 的選擇建議

| 判斷條件              | 建議使用                  | 原因說明                          |
| ----------------- | --------------------- | ----------------------------- |
| 是否有複雜關聯、完整物件結構需要  | ✅ Redis Object（序列化）   | 操作簡單，轉換整筆 Java Entity 快速直接    |
| 是否需單欄查詢、更新        | ✅ Redis Hash（HSET 欄位） | 欄位可個別操作，資料結構清楚                |
| 是否跨服務、跨語言存取       | ✅ Redis Hash          | Hash 存的是可讀 JSON，不會序列化相容性問題    |
| 是否只用於 Java 程式內部快取 | ✅ Redis Object        | 可與 `@Cacheable` 結合，效能與整合性高    |
| 是否需要支援查詢子欄位或篩選條件  | ✅ Redis Hash          | 可搭配欄位存取（如 `HSCAN`, `HGETALL`） |

---

範例 Redis Hash 結構：

```
Key: asset:top_user_agg:{party_id}
Value: Hash
  "2330" => "{\"name\":\"台積電\",\"alias\":\"TSMC\",\"marketValue\":1000000}"
  "0050" => "{\"name\":\"元大50\",\"alias\":\"ETF\",\"marketValue\":500000}"
```



-------------------

note

- 如果是使用 docker 開啟 redis 單體模式，開始 redis-cli 的語法
```bash
docker exec -it redis-node1 redis-cli -p 7000
```
- 如果是使用 docker 開啟 redis cluster 模式，開始 redis-cli 的語法
```bash
docker exec -it redis-node1 redis-cli -p 7000 -c
```

- 要注意在 redis cluster 的狀況下，資料被儲存的 slot 跟他的 index 必須在停一個 node 的狀況下才能被查詢到，否則會查不到

- 清除資料
```bash
docker exec redis-node1 redis-cli -p 7000 FLUSHDB
docker exec redis-node2 redis-cli -p 7001 FLUSHDB
docker exec redis-node3 redis-cli -p 7002 FLUSHDB
docker exec redis-node4 redis-cli -p 7003 FLUSHDB
docker exec redis-node5 redis-cli -p 7004 FLUSHDB
docker exec redis-node6 redis-cli -p 7005 FLUSHDB
```
```sql
JSON.SET user:{1001} $ '{"user_id": 1001, "name": "Alice", "email": "alice@example.com"}'
JSON.SET order:{1001}:2001 $ '{"order_id": 2001, "user_id": 1001, "date": "2024-05-01"}'
JSON.SET order:{1001}:2002 $ '{"order_id": 2002, "user_id": 1001, "date": "2024-05-02"}'
JSON.SET item:{1001}:3001 $ '{"item_id": 3001, "order_id": 2001, "product": "Book", "price": 300}'
JSON.SET item:{1001}:3002 $ '{"item_id": 3002, "order_id": 2001, "product": "Pen", "price": 20}'
```
```sql
FT.CREATE idx:users ON JSON PREFIX 1 "user:{1001}" SCHEMA $.name AS name TEXT $.email AS email TAG
FT.CREATE idx:orders ON JSON PREFIX 1 "order:{1001}:" SCHEMA $.order_id AS order_id NUMERIC $.user_id AS user_id NUMERIC
FT.CREATE idx:items ON JSON PREFIX 1 "item:{1001}:" SCHEMA $.product AS product TEXT $.order_id AS order_id NUMERIC
```
```sql
FT.SEARCH idx:users "@name:Alice"
FT.SEARCH idx:orders "@user_id:[1001 1001]"
FT.SEARCH idx:items "@order_id:[2001 2001]"
```

| 操作 | 是否會自動 redirect | 特別注意 |
| --- | --- | --- |
| JSON.SET | ✅ 是 | 自動寫入正確節點（slot 對應） |
| JSON.GET | ✅ 是 | 自動讀取正確節點 |
| FT.SEARCH | ✅ 是 | 查詢會自動 redirect 到正確節點 |
| FT.CREATE | ❌ 不會 | 只能在目標節點手動執行！ |
