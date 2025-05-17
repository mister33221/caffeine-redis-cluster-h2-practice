#!/bin/bash

echo "🚨 開始清除 Redis Cluster 中所有節點資料..."

# 你可以修改這裡的 port 範圍來對應節點數量
for port in 7000 7001 7002 7003 7004 7005; do
  node="redis-node$((port - 6999))"
  echo "🧹 flushing $node (port $port)..."
  docker exec "$node" redis-cli -p "$port" FLUSHDB
done

echo "✅ 所有節點資料已清除完成。"
