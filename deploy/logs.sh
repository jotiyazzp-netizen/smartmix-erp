#!/bin/bash

# SmartMix 日志查看脚本

if [ -z "$1" ]; then
    echo "📊 SmartMix 服务日志"
    echo "========================================="
    echo ""
    echo "用法："
    echo "  ./logs.sh [服务名] [选项]"
    echo ""
    echo "服务名："
    echo "  backend  - 后端服务日志"
    echo "  web      - Web 前端日志"
    echo "  nginx    - Nginx 日志"
    echo "  all      - 所有服务日志（默认）"
    echo ""
    echo "选项："
    echo "  -f       - 实时跟踪日志（follow）"
    echo "  -n NUM   - 显示最后 NUM 行（默认 100）"
    echo ""
    echo "示例："
    echo "  ./logs.sh backend -f     # 实时查看后端日志"
    echo "  ./logs.sh nginx -n 50    # 查看 Nginx 最后 50 行日志"
    echo "  ./logs.sh all -f         # 实时查看所有服务日志"
    echo ""
    exit 0
fi

SERVICE=$1
shift

case "$SERVICE" in
    backend|web|nginx)
        docker-compose -f docker-compose.prod.yml logs "$@" "$SERVICE"
        ;;
    all)
        docker-compose -f docker-compose.prod.yml logs "$@"
        ;;
    *)
        echo "❌ 未知的服务名: $SERVICE"
        echo "可用服务: backend, web, nginx, all"
        exit 1
        ;;
esac
