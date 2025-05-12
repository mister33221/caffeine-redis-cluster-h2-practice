#!/bin/bash

echo "⏳ 檢查 Redis 節點是否已啟動..."

check_node_ready() {
  local host=$1
  local port=$2
  docker exec redis-node1 redis-cli -h $host -p $port ping | grep -q PONG
}

# 檢查所有節點是否就緒（最多等 30 秒）
for i in {1..30}; do
  ready=true
  for port in 7000 7001 7002 7003 7004 7005; do
    if ! check_node_ready "redis-node$((port - 6999))" $port; then
      echo "等待 redis-node$((port - 6999)):$port 啟動中..."
      ready=false
      break
    fi
  done

  if $ready; then
    echo "✅ 所有 Redis 節點已就緒，開始建立 Cluster"
    break
  fi

  sleep 1
done

if ! $ready; then
  echo "❌ Redis 節點未全數啟動，請檢查容器狀態。"
  exit 1
fi

# 建立 Cluster（自動輸入 yes）
echo "yes" | docker exec -i redis-node1 redis-cli --cluster create \
  redis-node1:7000 \
  redis-node2:7001 \
  redis-node3:7002 \
  redis-node4:7003 \
  redis-node5:7004 \
  redis-node6:7005 \
  --cluster-replicas 1

# 等待 Cluster 建立完成
echo "⏳ 等待 Cluster 建立完成..."
for i in {1..30}; do
  if docker exec redis-node1 redis-cli -p 7000 cluster info | grep -q "cluster_state:ok"; then
    echo "✅ Cluster 建立完成！"
    break
  fi
  sleep 1
done

echo -e "\n📡 檢查 Cluster 狀態..."
docker exec redis-node1 redis-cli -p 7000 cluster info

echo -e "\n🔗 顯示節點分配："
docker exec redis-node1 redis-cli -p 7000 cluster nodes
