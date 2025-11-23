#!/bin/bash

# SmartMix 停止服务脚本

set -e

echo "========================================="
echo "SmartMix 服务停止脚本"
echo "========================================="
echo ""

echo "🛑 停止所有服务..."
docker-compose -f docker-compose.prod.yml down

echo ""
echo "✅ 服务已停止"
echo ""
echo "💡 提示："
echo "  - 重新启动服务: ./deploy.sh"
echo "  - 查看容器状态: docker ps -a"
echo ""
