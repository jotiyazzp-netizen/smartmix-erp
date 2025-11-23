#!/bin/bash

# SmartMix 一键部署脚本
# 用于快速部署或更新 SmartMix 生产环境

set -e  # 遇到错误立即退出

echo "========================================="
echo "SmartMix 生产环境部署脚本"
echo "========================================="
echo ""

# 检查 .env.production 是否存在
if [ ! -f ".env.production" ]; then
    echo "❌ 错误：未找到 .env.production 文件"
    echo "请先创建并配置 .env.production 文件"
    exit 1
fi

# 检查是否包含默认值
if grep -q "CHANGE_ME_IN_PRODUCTION" .env.production; then
    echo "⚠️  警告：.env.production 中仍然包含 CHANGE_ME_IN_PRODUCTION 占位符"
    echo "请确保已经修改了所有生产环境配置"
    read -p "是否继续部署？(y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "部署已取消"
        exit 1
    fi
fi

echo "📦 步骤 1/4: 停止旧服务..."
docker-compose -f docker-compose.prod.yml down || true

echo ""
echo "🏗️  步骤 2/4: 构建 Docker 镜像..."
docker-compose -f docker-compose.prod.yml build --no-cache

echo ""
echo "🚀 步骤 3/4: 启动服务..."
docker-compose -f docker-compose.prod.yml up -d

echo ""
echo "⏳ 步骤 4/4: 等待服务启动..."
sleep 10

echo ""
echo "🔍 检查服务状态..."
docker-compose -f docker-compose.prod.yml ps

echo ""
echo "========================================="
echo "✅ 部署完成！"
echo "========================================="
echo ""
echo "📊 查看日志："
echo "  - 所有服务: ./logs.sh"
echo "  - 后端: docker-compose -f docker-compose.prod.yml logs -f backend"
echo "  - 前端: docker-compose -f docker-compose.prod.yml logs -f web"
echo "  - Nginx: docker-compose -f docker-compose.prod.yml logs -f nginx"
echo ""
echo "🛑 停止服务: ./stop.sh"
echo ""
echo "🌐 访问地址："
echo "  - HTTP: http://您的服务器IP"
echo "  - HTTPS: https://您的域名 (如果已配置)"
echo ""
