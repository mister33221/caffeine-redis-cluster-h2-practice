#!/bin/bash

echo "🚀 開始插入測試資料到 Redis Stack Cluster..."

# 定義使用者 ID 清單
USER_IDS=(1001 1002 1003 1004 1005)

# 每位使用者插入 user + 2 筆 order + 每筆 order 各 2 筆 item
for USER_ID in "${USER_IDS[@]}"; do
  NODE_INDEX=$(( (USER_ID % 6) + 1 ))         # redis-node1 ~ redis-node6
  SLOT_NODE="redis-node$NODE_INDEX"
  PORT=$(( 7000 + (NODE_INDEX - 1) ))         # 對應 port 7000 ~ 7005
  echo "👤 插入資料 for user:$USER_ID -> $SLOT_NODE:$PORT"

  # 插入 user
  docker exec "$SLOT_NODE" redis-cli -p "$PORT" -c \
    JSON.SET user:{$USER_ID} '$' "{\"user_id\":$USER_ID,\"name\":\"User$USER_ID\",\"email\":\"user$USER_ID@example.com\"}"

  for ORDER_IDX in 1 2; do
    ORDER_ID=$((USER_ID * 10 + ORDER_IDX))

    # 插入 order
    docker exec "$SLOT_NODE" redis-cli -p "$PORT" -c \
      JSON.SET order:{$USER_ID}:$ORDER_ID '$' "{\"order_id\":$ORDER_ID,\"user_id\":$USER_ID,\"date\":\"2024-05-0$ORDER_IDX\"}"

    for ITEM_IDX in 1 2; do
      ITEM_ID=$((ORDER_ID * 10 + ITEM_IDX))
      PRODUCT="Product_${USER_ID}_${ORDER_IDX}_${ITEM_IDX}"
      PRICE=$((RANDOM % 1000 + 100))

      # 插入 item
      docker exec "$SLOT_NODE" redis-cli -p "$PORT" -c \
        JSON.SET item:{$USER_ID}:$ITEM_ID '$' "{\"item_id\":$ITEM_ID,\"order_id\":$ORDER_ID,\"product\":\"$PRODUCT\",\"price\":$PRICE}"
    done
  done
done

echo "✅ 測試資料插入完成，共 ${#USER_IDS[@]} 位使用者。"
