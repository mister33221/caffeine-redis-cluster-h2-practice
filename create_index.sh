#!/bin/bash

echo "🔍 開始建立 RediSearch 索引..."

# 我們挑其中一組已插入資料的 slot (例如 user:1001 對應節點)
USER_ID=1001
NODE_INDEX=$(( (USER_ID % 6) + 1 ))
SLOT_NODE="redis-node$NODE_INDEX"
PORT=$(( 7000 + (NODE_INDEX - 1) ))

# 建立 user 索引
docker exec "$SLOT_NODE" redis-cli -p "$PORT" -c \
  FT.CREATE idx:users ON JSON PREFIX 1 "user:{" SCHEMA \
    $.user_id AS user_id NUMERIC \
    $.name AS name TEXT \
    $.email AS email TAG

# 建立 order 索引
docker exec "$SLOT_NODE" redis-cli -p "$PORT" -c \
  FT.CREATE idx:orders ON JSON PREFIX 1 "order:{$USER_ID}:" SCHEMA \
    $.order_id AS order_id NUMERIC \
    $.user_id AS user_id NUMERIC \
    $.date AS date TAG

# 建立 item 索引
docker exec "$SLOT_NODE" redis-cli -p "$PORT" -c \
  FT.CREATE idx:items ON JSON PREFIX 1 "item:{$USER_ID}:" SCHEMA \
    $.item_id AS item_id NUMERIC \
    $.order_id AS order_id NUMERIC \
    $.product AS product TEXT \
    $.price AS price NUMERIC

echo "✅ 索引建立完成，請繼續進行查詢測試。"
