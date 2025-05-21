package com.kai.caffeine_redis_cluster_h2_practice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kai.caffeine_redis_cluster_h2_practice.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.search.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserRedisService {

    Set<HostAndPort> nodes = Set.of(
            new HostAndPort("host.docker.internal", 7000),
            new HostAndPort("host.docker.internal", 7001),
            new HostAndPort("host.docker.internal", 7002),
            new HostAndPort("host.docker.internal", 7003),
            new HostAndPort("host.docker.internal", 7004),
            new HostAndPort("host.docker.internal", 7005)
    );
    JedisCluster cluster = new JedisCluster(nodes);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String indexName = "idx:user";
    private final String prefix = "user:{user}:"; // 固定這樣寫

    @PostConstruct
    public void createUserIndexIfNotExists() {
        try {
            IndexDefinition indexDefinition = new IndexDefinition(IndexDefinition.Type.JSON)
                    .setPrefixes("user:{user}:"); // <== 寫死字串，不要用 prefix 變數

            Schema schema = new Schema()
                    .addTextField("$.username", 1.0).as("username")
                    .addNumericField("$.age").as("age");

            cluster.ftCreate(indexName, IndexOptions.defaultOptions().setDefinition(indexDefinition), schema);
        } catch (Exception e) {
            System.out.println("索引已存在或建立失敗：" + e.getMessage());
        }
    }

    public void saveUserJson(User user) {
//        cluster.jsonSet(prefix + user.getId(), Path.ROOT_PATH, user);
        String key = prefix + user.getId(); // 例如 user:{user}:4
        cluster.jsonSet(key, Path.ROOT_PATH, user);
    }

    public User getUserJson(Long id) {
        String key = prefix + id;

        // 這裡正確地用 String.class 讓它回傳 JSON 字串
        Object json = cluster.jsonGet(key);

        return objectMapper.convertValue(json, User.class); // 如果用 Jackson
    }

    public List<Document> searchByName(String keyword) {
        String fullKeyword = "*" + keyword + "*"; // 使用通配符查詢
        // 使用 @欄位名:關鍵字 查詢指定欄位
        Query query = new Query(fullKeyword)
                .returnFields("username", "age")
                .limit(0, 10);

        SearchResult result = cluster.ftSearch(indexName, query);
        return result.getDocuments();
    }
}
