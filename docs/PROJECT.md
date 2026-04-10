# 签名和验签接口项目文档

## 1. 项目概述

### 1.1 项目简介
本项目是一个基于 Spring Boot 开发的签名和验签接口服务，提供对请求数据进行数字签名和验证签名的功能。签名结果存储在 MySQL 数据库中，同时使用 Redis 进行缓存以提高验签性能。

### 1.2 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 1.8 | 编程语言 |
| Spring Boot | 2.7.18 | 应用框架 |
| Spring Data JPA | 2.7.18 | 数据持久层 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 5.0+ | 缓存数据库 |
| Swagger2 | 2.9.2 | API 文档 |
| Maven | 3.6+ | 构建工具 |
| Lombok | 1.18+ | 代码简化 |

### 1.3 项目结构

```
signature-api/
├── pom.xml                                    # Maven 配置文件
├── README.md                                  # 项目说明文档
├── docs/                                      # 项目文档目录
│   ├── PROJECT.md                             # 项目详细文档
│   ├── API.md                                 # 接口文档
│   └── deploy.sql                             # SQL 部署脚本
├── src/
│   ├── main/
│   │   ├── java/com/example/signature/
│   │   │   ├── SignatureApiApplication.java   # Spring Boot 启动类
│   │   │   ├── config/                        # 配置类
│   │   │   ├── controller/                    # 控制器层
│   │   │   ├── dto/                           # 数据传输对象
│   │   │   ├── entity/                        # 实体类
│   │   │   ├── repository/                    # 数据访问层
│   │   │   ├── service/                       # 业务逻辑层
│   │   │   └── util/                          # 工具类
│   │   └── resources/
│   │       └── application.yml                # 应用配置文件
│   └── test/                                  # 测试代码
```

## 2. 核心功能

### 2.1 签名功能
- 接收客户端的请求参数（appId、timestamp、nonce、data）
- 使用 SHA256 算法生成数字签名
- 将签名信息保存到 MySQL 数据库
- 将签名结果缓存到 Redis（24小时过期）
- 返回签名结果给客户端

### 2.2 验签功能
- 接收客户端的签名验证请求
- 优先从 Redis 缓存中查询签名
- 如缓存未命中，从 MySQL 数据库查询
- 验证签名是否匹配
- 返回验签结果

## 3. 签名算法说明

### 3.1 签名生成规则
1. 将所有请求参数（除 signature 外）按参数名 ASCII 码从小到大排序
2. 将排序后的参数拼接成字符串，格式为 `key1=value1&key2=value2`
3. 在字符串末尾追加 `&key=密钥`
4. 对最终字符串进行 SHA256 哈希运算
5. 将结果转换为大写十六进制字符串

### 3.2 示例
```
参数：
- appId = APP001
- timestamp = 1704067200000
- nonce = abc123
- data = {"orderNo":"202401010001"}

排序后拼接：
appId=APP001&data={"orderNo":"202401010001"}&nonce=abc123&timestamp=1704067200000&key=your-secret-key

SHA256 结果：
A1B2C3D4E5F6...(64位大写十六进制字符串)
```

## 4. 数据库设计

### 4.1 表结构

**表名**: `signature_record`

| 字段名 | 类型 | 长度 | 是否为空 | 说明 |
|--------|------|------|----------|------|
| id | BIGINT | - | 否 | 主键，自增 |
| app_id | VARCHAR | 64 | 否 | 应用ID |
| timestamp | VARCHAR | 20 | 否 | 时间戳 |
| nonce | VARCHAR | 32 | 否 | 随机数 |
| data_content | TEXT | - | 是 | 数据内容 |
| signature | VARCHAR | 256 | 否 | 签名结果 |
| sign_type | VARCHAR | 10 | 是 | 签名类型 |
| create_time | DATETIME | - | 是 | 创建时间 |
| update_time | DATETIME | - | 是 | 更新时间 |

### 4.2 索引设计

- 主键索引：`id`
- 普通索引：`signature` - 用于快速查询签名记录
- 组合索引：`app_id + timestamp + nonce` - 用于防重放攻击检查

## 5. 配置说明

### 5.1 数据库配置 (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/signature_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 5.2 Redis 配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
    timeout: 3000ms
```

### 5.3 JPA 配置

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update    # 自动更新表结构
    show-sql: true        # 显示 SQL 语句
```

## 6. 部署说明

### 6.1 环境要求
- JDK 1.8 或更高版本
- MySQL 8.0 或更高版本
- Redis 5.0 或更高版本
- Maven 3.6 或更高版本

### 6.2 部署步骤

1. **创建数据库**
   ```sql
   CREATE DATABASE signature_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **执行 SQL 脚本**
   ```bash
   mysql -u root -p signature_db < docs/deploy.sql
   ```

3. **修改配置文件**
   编辑 `src/main/resources/application.yml`，配置数据库和 Redis 连接信息。

4. **编译打包**
   ```bash
   mvn clean package
   ```

5. **启动服务**
   ```bash
   java -jar target/signature-api-1.0.0.jar
   ```

### 6.3 访问接口文档

服务启动后，访问以下地址查看 Swagger 接口文档：
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v2/api-docs

## 7. 测试说明

### 7.1 运行单元测试
```bash
mvn test
```

### 7.2 测试覆盖范围
- Controller 层接口测试
- Service 层业务逻辑测试

## 8. 安全说明

### 8.1 防重放攻击
- 使用时间戳和随机数（nonce）机制
- 同一组参数（appId + timestamp + nonce）只能使用一次

### 8.2 密钥管理
- 密钥应存储在安全的环境变量或配置中心
- 生产环境建议定期更换密钥
- 不同应用应使用不同的密钥

### 8.3 数据安全
- 敏感数据传输建议使用 HTTPS
- 数据库密码应加密存储

## 9. 监控与日志

### 9.1 日志配置
- 日志级别：INFO（生产环境）/ DEBUG（开发环境）
- 日志路径：控制台输出（可配置为文件）
- 日志格式：时间戳 + 线程名 + 日志级别 + 类名 + 消息

### 9.2 健康检查
- 接口：`GET /api/v1/signature/health`
- 返回：`OK`

## 10. 常见问题

### Q1: 签名失败怎么办？
A: 检查参数是否正确排序，密钥是否正确，时间戳格式是否正确。

### Q2: 验签失败怎么办？
A: 检查签名是否过期（超过24小时），参数是否被篡改，密钥是否一致。

### Q3: Redis 连接失败怎么办？
A: 检查 Redis 服务是否启动，配置是否正确，网络是否连通。

## 11. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2024-01-01 | 初始版本，实现基础签名验签功能 |

## 12. 联系方式

- 开发者：Developer
- 邮箱：developer@example.com
