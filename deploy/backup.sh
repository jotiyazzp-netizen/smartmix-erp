#!/bin/bash

# SmartMix 数据备份脚本
# 注意：如果使用阿里云 RDS，建议使用 RDS 的自动备份功能
# 此脚本仅用于本地数据库备份

set -e

BACKUP_DIR="./backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo "========================================="
echo "SmartMix 数据备份脚本"
echo "========================================="
echo ""

# 创建备份目录
mkdir -p "$BACKUP_DIR"

echo "📦 备份时间: $TIMESTAMP"
echo ""

# 检查是否使用本地数据库
if docker-compose -f docker-compose.prod.yml ps | grep -q "mysql"; then
    echo "📊 备份本地 MySQL 数据..."
    docker-compose -f docker-compose.prod.yml exec -T mysql \
        mysqldump -u root -proot_password smartmix \
        > "$BACKUP_DIR/smartmix_db_$TIMESTAMP.sql"
    
    echo "✅ 数据库备份完成: $BACKUP_DIR/smartmix_db_$TIMESTAMP.sql"
else
    echo "⚠️  警告：未检测到本地 MySQL 容器"
    echo "如果使用阿里云 RDS，请使用 RDS 控制台的备份功能"
fi

echo ""
echo "📁 备份文件上传..."
if [ -d "backend-uploads" ]; then
    tar -czf "$BACKUP_DIR/smartmix_uploads_$TIMESTAMP.tar.gz" backend-uploads/
    echo "✅ 文件上传备份完成: $BACKUP_DIR/smartmix_uploads_$TIMESTAMP.tar.gz"
fi

echo ""
echo "========================================="
echo "✅ 备份任务完成"
echo "========================================="
echo ""
echo "备份文件位置: $BACKUP_DIR/"
ls -lh "$BACKUP_DIR/" | tail -n 5
echo ""
echo "💡 建议定期清理旧备份文件"
echo ""
