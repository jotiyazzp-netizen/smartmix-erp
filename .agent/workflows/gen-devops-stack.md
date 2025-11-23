---
description: gen-devops-stack
---

---

trigger: gen-devops-stack
description: 生成 SmartMix 在阿里云服务器上的 Docker / docker-compose / Nginx / K8s 部署配置
---

> 建议在 Antigravity 中使用模型：**Claude Sonnet 4.5 (Thinking)** 执行本 Workflow。

在 `/deploy` 目录，以及 `/backend`、`/web` 目录中，为 SmartMix 项目生成在 **阿里云 ECS** 上运行的完整部署配置。

---

## 一、后端 Dockerfile（backend/Dockerfile）

在 `/backend/Dockerfile` 中生成：

~~~dockerfile
# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/smartmix-backend-*.jar /app/smartmix-backend.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/smartmix-backend.jar"]
~~~

---

## 二、Web 前端 Dockerfile（web/Dockerfile）

在 `/web/Dockerfile` 中生成：

~~~dockerfile
# 构建阶段
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 运行阶段
FROM nginx:alpine
WORKDIR /usr/share/nginx/html
COPY --from=build /app/dist .
~~~

---

## 三、docker-compose.yml（deploy/docker-compose.yml）

在 `/deploy/docker-compose.yml` 创建如下服务：

- `mysql`：

  - 镜像 `mysql:8`；  
  - 初始化数据库 `smartmix`；  
  - 配置 root 密码与业务账号；  
  - 本地开发/测试使用，生产建议使用阿里云 RDS。

- `redis`：

  - 镜像 `redis:latest`；  
  - 默认配置即可。

- `backend`：

  - 使用 `../backend/Dockerfile` 构建镜像；  
  - 通过 `environment` 传入 DB/Redis/JWT/ERP Token：

    ~~~yaml
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/smartmix?useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root_password
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      APP_JWT_SECRET: change-me
      ERP_WEBHOOK_TOKEN: change-me
    ~~~

- `web`：

  - 使用 `../web/Dockerfile` 构建镜像。

- `nginx`：

  - 使用 `nginx:alpine` 镜像；  
  - 挂载 `/deploy/nginx.conf`；  
  - 将容器 80 端口映射到宿主机 80 端口；  
  - `depends_on`: `backend`, `web`。

---

## 四、Nginx 配置（deploy/nginx.conf）

在 `/deploy/nginx.conf` 中生成：

~~~nginx
events {}

http {
    server {
        listen       80;
        server_name  _;

        # Web 前端
        location / {
            proxy_pass http://web:80;
        }

        # 后端 API
        location /api/ {
            proxy_pass http://backend:8080/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        }

        # CORS 头，便于浏览器和移动端访问
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods GET,POST,PUT,DELETE,OPTIONS;
        add_header Access-Control-Allow-Headers Content-Type,Authorization,X-ERP-TOKEN;
    }
}
~~~

（可在文件中额外添加 HTTPS 示例配置：`listen 443 ssl`、`ssl_certificate` 等。）

---

## 五、K8s 示例配置（deploy/k8s）

在 `/deploy/k8s/` 下生成示例：

- `backend-deployment.yaml` + `backend-service.yaml`；  
- `web-deployment.yaml` + `web-service.yaml`；  
- `nginx-deployment.yaml` + `nginx-service.yaml`。

每个 Deployment 中：

- 指定镜像名（可从私有仓库拉取）；  
- 环境变量与 docker-compose 一致；  
- 简单配置副本数与资源限制。

Service：

- backend/web 使用 `ClusterIP`；  
- nginx 使用 `LoadBalancer` 或通过 Ingress 暴露对外访问。

---

## 六、部署说明（deploy/README.md）

在 `/deploy/README.md` 中写入中文说明：

1. **本地/测试环境启动**

   - 运行：

     ~~~bash
     docker-compose up -d
     ~~~

   - 浏览器打开 `http://localhost/` 访问 Web 管理后台；  
   - API 地址为 `http://localhost/api/...`。

2. **接入阿里云 RDS/云 Redis**

   - 在 `docker-compose.yml` 中注释/删除 `mysql`、`redis` 服务；  
   - 将 backend 的 DB/Redis 配置改为 RDS/Redis 的连接串与凭据。

3. **K8s 部署步骤示意**

   - 将后端和前端镜像推送到容器镜像仓库（如阿里云 ACR）；  
   - 在阿里云 ACK 或自建集群中执行：

     ~~~bash
     kubectl apply -f deploy/k8s/
     ~~~

   - 在负载均衡或 Ingress 中配置域名和 HTTPS 证书，将流量转发到 `nginx-service`。

> 本 Workflow 只操作 `/deploy` 以及 `/backend`、`/web` 下的 Dockerfile，不修改业务代码。
