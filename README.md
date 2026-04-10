# 签名验签API接口项目 (signature-api)

基于Spring Boot的数字签名和验签RESTful API服务，提供安全可靠的签名生成和验证功能。

---

## 项目概述

本项目是一个独立的签名验签服务，主要功能是对业务请求进行数字签名，并对客户端提交的签名进行合法性验证。签名结果同时存储在MySQL数据库和Redis缓存中，保证验签的高性能和数据一致性。

### 核心功能

✅ **签名接口** - 对请求内容进行数字签名，签名结果持久化存储  
✅ **验签接口** - 对客户端签名进行合法性验证，支持缓存和数据库双检查  
✅ **数据存储** - MySQL持久化 + Redis二级缓存  
✅ **多算法支持** - 支持HMAC-SHA256、SHA-256签名算法  
✅ **接口文档** - 集成Swagger2自动生成API文档

---

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 1.8 | 开发语言 |
| Spring Boot | 2.7.18 | 应用框架 |
| Spring Data JPA | 2.7.x | ORM框架 |
| MySQL | 8.0.x | 关系型数据库 |
| Redis | 5.x+ | 分布式缓存 |
| Swagger2 | 2.9.2 | API文档生成 |
| Lombok | 最新 | 代码简化 |
| Apache Commons Codec | 1.15 | 编码工具 |
| Maven | 3.x | 项目构建 |

---

## 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+
- Windows/Linux/macOS

---

## 快速开始

### 1. 数据库部署

执行SQL脚本创建数据库和表结构：

```bash
mysql -u root -p < docs/sql/deploy.sql
```

或手动执行：
```sql
CREATE DATABASE signature_db DEFAULT CHARACTER SET utf8mb4;
USE signature_db;
-- 执行 docs/sql/deploy.sql 中的建表语句
```

### 2. 配置修改

修改 `src/main/resources/application.yml` 配置文件：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/signature_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username      # 改为你的MySQL用户名
    password: your_password      # 改为你的MySQL密码
  redis:
    host: localhost              # Redis地址
    port: 6379                   # Redis端口
    password: your_redis_password # Redis密码(如无则留空)
```

### 3. 项目启动

```bash
# 编译项目
mvn clean package -DskipTests

# 运行项目
mvn spring-boot:run
# 或
java -jar target/signature-api-1.0.0.jar
```

服务启动后访问：
- 应用地址：http://localhost:8080
- Swagger文档：http://localhost:8080/swagger-ui.html

---

## API接口文档

### 接口概览

| 接口地址 | 请求方式 | 说明 |
|----------|----------|------|
| `/api/signature/sign` | POST | 生成签名接口 |
| `/api/signature/verify` | POST | 验签接口 |
| `/api/signature/record/{signature}` | GET | 根据签名值查询记录 |
| `/api/signature/record/request/{requestId}` | GET | 根据请求ID查询记录 |

---

### 1. 生成签名接口

**接口地址：** `POST /api/signature/sign`

**请求参数：**
```json
{
    "signer": "client001",
    "content": "orderId=123456&amount=100.00&userId=u001",
    "algorithm": "HMAC-SHA256",
    "secretKey": "your-secret-key",
    "expireHours": 24
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| signer | String | 是 | 签名方标识，如客户端ID |
| content | String | 是 | 待签名的原始内容 |
| algorithm | String | 否 | 签名算法：HMAC-SHA256(默认) / SHA-256 |
| secretKey | String | 否 | 自定义密钥，不传则使用默认密钥 |
| expireHours | Integer | 否 | 过期时间(小时)，默认24小时 |
| params | Map | 否 | 参数形式的待签名内容 |

**响应示例：**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "requestId": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
        "signature": "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069",
        "algorithm": "HMAC-SHA256"
    }
}
```

---

### 2. 验签接口

**接口地址：** `POST /api/signature/verify`

**请求参数：**
```json
{
    "signature": "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069",
    "content": "orderId=123456&amount=100.00&userId=u001",
    "algorithm": "HMAC-SHA256",
    "secretKey": "your-secret-key"
}
```

**响应示例：**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "valid": true,
        "signature": "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069",
        "fromCache": true,
        "requestId": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
        "signer": "client001",
        "timestamp": "2026-04-10T10:30:00",
        "verifiedCount": 1
    }
}
```

---

## 项目结构

```
signature-api/
├── src/
│   ├── main/
│   │   ├── java/com/example/signatureapi/
│   │   │   ├── SignatureApiApplication.java    # 启动类
│   │   │   ├── config/
│   │   │   │   ├── RedisConfig.java            # Redis缓存配置
│   │   │   │   └── SwaggerConfig.java          # Swagger2配置
│   │   │   ├── controller/
│   │   │   │   └── SignatureController.java    # API接口控制器
│   │   │   ├── dto/                            # 数据传输对象
│   │   │   │   ├── ApiResponse.java            # 统一响应封装
│   │   │   │   ├── SignRequest.java            # 签名请求DTO
│   │   │   │   └── VerifyRequest.java          # 验签请求DTO
│   │   │   ├── entity/                         # 数据库实体
│   │   │   │   └── SignatureRecord.java        # 签名记录实体
│   │   │   ├── repository/                     # 数据访问层
│   │   │   │   └── SignatureRecordRepository.java
│   │   │   ├── service/                        # 业务逻辑层
│   │   │   │   ├── RedisCacheService.java      # Redis缓存服务
│   │   │   │   └── SignatureService.java       # 签名业务服务
│   │   │   └── util/                           # 工具类
│   │   │       └── SignatureUtil.java          # 签名算法工具
│   │   └── resources/
│   │       └── application.yml                 # 应用配置文件
│   └── test/
│       └── java/com/example/signatureapi/
│           └── SignatureApiApplicationTests.java # 单元测试
├── docs/
│   └── sql/
│       └── deploy.sql                          # 数据库部署脚本
├── pom.xml                                      # Maven配置
└── README.md                                    # 项目文档
```

---

## 核心实现说明

### 签名算法流程

1. **参数排序**：对传入参数按键名升序排列
2. **内容拼接**：按`key=value&key2=value2`格式拼接，最后追加密钥
3. **算法加密**：使用指定算法(HMAC-SHA256/SHA-256)生成摘要
4. **十六进制编码**：将二进制结果转为十六进制字符串
5. **持久化存储**：签名记录写入MySQL + Redis二级缓存

### 验签流程

1. **Redis查询**：优先从缓存获取签名数据（1ms级响应）
2. **数据库查询**：缓存未命中时查询MySQL
3. **签名校验**：使用相同算法重新签名并比对
4. **计数更新**：验签次数+1，更新数据库记录

---

## 测试说明

### 运行单元测试

```bash
mvn test
```

包含以下测试用例：
- 签名工具类基础功能测试
- Map参数签名/验签测试
- 服务层签名/验签完整流程测试
- 多算法兼容性测试
- 请求ID生成测试

### Postman测试

导入Swagger文档到Postman：
1. 访问 http://localhost:8080/v2/api-docs
2. 保存JSON导入Postman
3. 直接调试所有接口

---

## 部署说明

### 生产环境建议

1. **数据库配置**
   - 主从复制架构
   - 定期备份数据
   - 合理设置连接池参数

2. **Redis配置**
   - 开启RDB+AOF持久化
   - 设置合理的内存淘汰策略
   - 生产环境使用集群模式

3. **应用配置**
   - JVM参数：`-Xms2g -Xmx4g`
   - 开启GC日志
   - 使用Nginx做负载均衡

### Docker部署参考

```dockerfile
FROM openjdk:8-jre-alpine
WORKDIR /app
COPY target/signature-api-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 扩展功能建议

🔸 **客户端管理** - 添加签名客户端秘钥管理功能  
🔸 **白名单控制** - 接口调用IP白名单校验  
🔸 **限流熔断** - 集成Sentinel保护接口  
🔸 **异步落库** - 使用MQ异步写入数据库提高性能  
🔸 **国密算法** - 支持SM2/SM3国密算法  
🔸 **日志审计** - 集成ELK进行操作日志审计

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-10 | 初始版本，实现基础签名验签功能 |

---

## 联系方式

如有问题或建议，请提交Issue或联系开发团队。

---

**License**: MIT
