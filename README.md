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
│   │   ├── config/         # Redis, Caffeine, JPA 等配置類
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

## 🔧 快取策略設計

* **Caffeine：** 用於快速本機查詢的 LRU 本地快取。
* **Redis Cluster：** 分散式共享快取，支援 failover 與資料持久性，適合多節點部署。
* **整合邏輯：** 透過 Spring Cache 抽象，先查詢 Caffeine，再查 Redis，最後查 DB。

---

## ⚙️ 設定說明

### application.yml

* 設定 Redis Cluster 節點
* 設定 Caffeine 快取參數（最大容量、TTL）
* 設定 H2 資料庫

---

## 🚀 如何啟動

1. 已經寫好 docker-compose.yml 放在專案根目錄中，使用 docker 啟動 三主三從的 Redis Cluster：
   ```bash
   docker-compose up -d
   ```
2. 也寫好了 init-cluster.sh，其中會先檢查 6 個 redis 在 port 7000-7005 是否都以成功啟動，接著就會自動建立 cluster，並且將 3 個主節點的 slot 分配給 6 個 redis 節點，最後會檢查 cluster 是否成功建立。若是在 windows 環境，可以使用 git-bash 或者 WSL 來執行 sh 這個腳本。
   ```bash
   sh init-cluster.sh
   ```

---

## 🧪 測試方式

* 使用 Postman 或 curl 呼叫 API
* 驗證首次查詢從 DB、後續查詢是否來自 Cache
* 可以關閉 Redis 模擬故障轉移是否 fallback 成功

---

## 🔍 可擴充方向

* 增加 Cache Eviction 策略與觀察日誌
* 整合 Spring Actuator 觀察快取狀態
* 支援動態切換快取層級（僅本機 / 分散式）

---

## 📚 參考資料

* [Spring Data JPA 官方文件](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
* [Caffeine GitHub](https://github.com/ben-manes/caffeine)
* [Spring Redis Cluster 支援](https://docs.spring.io/spring-data/redis/docs/current/reference/html/#redis.cluster)
* [H2 Database](https://www.h2database.com/)

---

## 🧑‍💻 作者

* Kai @ Systex

---

> 📢 開發進度與細節將持續補充於此 README，歡迎關注後續更新！



---

note

- spring-boot-devtools 會導致 redis cache 的問題。是 Spring Boot 開發中常見的 DevTools 類別載入器衝突問題。快取（如 Caffeine）將 A 類別的物件快取住，但熱重新編譯後 JVM 把 A 類別重新載入成 不同的版本，導致無法 cast 回原型。
```bash
ClassCastException: class ...User cannot be cast to class ...User
(...User is in unnamed module of loader 'app'; ...User is in unnamed module of loader org.springframework.boot.devtools.restart.classloader.RestartClassLoader) 
```
