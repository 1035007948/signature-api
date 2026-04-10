# 签名和验签接口 API 文档

## 文档信息

- **版本**: 1.0.0
- **基础URL**: `http://localhost:8080`
- **接口前缀**: `/api/v1/signature`
- **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## 接口列表

| 序号 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 1 | /api/v1/signature/sign | POST | 生成数字签名 |
| 2 | /api/v1/signature/verify | POST | 验证签名有效性 |
| 3 | /api/v1/signature/health | GET | 服务健康检查 |

---

## 1. 签名接口

### 基本信息

- **接口地址**: `/api/v1/signature/sign`
- **请求方式**: POST
- **Content-Type**: application/json

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| appId | string | 是 | 应用ID | APP001 |
| timestamp | string | 是 | 时间戳（毫秒） | 1704067200000 |
| nonce | string | 是 | 随机字符串（防重放） | abc123xyz |
| data | string | 否 | 业务数据（JSON格式） | {"orderNo":"202401010001"} |

### 请求示例

```json
{
  "appId": "APP001",
  "timestamp": "1704067200000",
  "nonce": "abc123xyz",
  "data": "{\"orderNo\":\"202401010001\",\"amount\":100.00}"
}
```

### 响应参数

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| success | boolean | 是否成功 | true |
| message | string | 响应消息 | 签名成功 |
| signature | string | 签名结果（64位大写十六进制） | A1B2C3D4E5F6... |
| signType | string | 签名算法类型 | SHA256 |
| timestamp | string | 时间戳 | 1704067200000 |

### 响应示例

**成功响应**:
```json
{
  "success": true,
  "message": "签名成功",
  "signature": "A1B2C3D4E5F6789012345678901234567890ABCDEF1234567890ABCDEF123456",
  "signType": "SHA256",
  "timestamp": "1704067200000"
}
```

**失败响应**:
```json
{
  "success": false,
  "message": "appId不能为空",
  "signature": null,
  "signType": null,
  "timestamp": null
}
```

### 签名算法说明

签名生成规则：
1. 将所有参数（除 signature 外）按参数名 ASCII 码从小到大排序
2. 拼接成 `key1=value1&key2=value2` 格式
3. 末尾追加 `&key=密钥`
4. 进行 SHA256 哈希运算
5. 结果转为大写十六进制字符串

**示例**:
```
排序后: appId=APP001&data={"orderNo":"202401010001"}&nonce=abc123xyz&timestamp=1704067200000
加密钥: appId=APP001&data={"orderNo":"202401010001"}&nonce=abc123xyz&timestamp=1704067200000&key=your-secret-key
SHA256: A1B2C3D4E5F6...(64位大写)
```

### 错误码

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误（如必填字段为空） |
| 500 | 服务器内部错误 |

---

## 2. 验签接口

### 基本信息

- **接口地址**: `/api/v1/signature/verify`
- **请求方式**: POST
- **Content-Type**: application/json

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| appId | string | 是 | 应用ID | APP001 |
| signature | string | 是 | 待验证的签名 | A1B2C3D4E5F6... |
| data | string | 否 | 业务数据（用于辅助验证） | {"orderNo":"202401010001"} |

### 请求示例

```json
{
  "appId": "APP001",
  "signature": "A1B2C3D4E5F6789012345678901234567890ABCDEF1234567890ABCDEF123456",
  "data": "{\"orderNo\":\"202401010001\"}"
}
```

### 响应参数

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| success | boolean | 请求是否成功处理 | true |
| valid | boolean | 签名是否有效 | true |
| message | string | 响应消息 | 验签成功 |

### 响应示例

**成功响应（签名有效）**:
```json
{
  "success": true,
  "valid": true,
  "message": "验签成功"
}
```

**成功响应（签名无效）**:
```json
{
  "success": true,
  "valid": false,
  "message": "验签失败，签名不匹配"
}
```

**失败响应（未找到记录）**:
```json
{
  "success": false,
  "valid": false,
  "message": "验签失败，未找到签名记录"
}
```

### 验签流程

1. **缓存查询**: 优先从 Redis 缓存中查询签名
2. **数据库查询**: 缓存未命中则查询 MySQL 数据库
3. **签名验证**: 使用原始参数重新计算签名并比对
4. **缓存更新**: 验证成功后更新 Redis 缓存

### 错误码

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 500 | 服务器内部错误 |

---

## 3. 健康检查接口

### 基本信息

- **接口地址**: `/api/v1/signature/health`
- **请求方式**: GET
- **Content-Type**: text/plain

### 请求参数

无

### 响应示例

**成功响应**:
```
OK
```

### 用途

- 用于负载均衡健康检查
- 用于监控系统检测服务状态
- 用于 Kubernetes 存活探针

---

## 数据模型

### SignRequest（签名请求）

```json
{
  "appId": "string",      // 应用ID，必填
  "timestamp": "string",  // 时间戳，必填
  "nonce": "string",      // 随机数，必填
  "data": "string"        // 业务数据，可选
}
```

### SignResponse（签名响应）

```json
{
  "success": true,        // 是否成功
  "message": "string",    // 响应消息
  "signature": "string",  // 签名结果
  "signType": "string",   // 签名类型
  "timestamp": "string"   // 时间戳
}
```

### VerifyRequest（验签请求）

```json
{
  "appId": "string",      // 应用ID，必填
  "signature": "string",  // 待验证签名，必填
  "data": "string"        // 业务数据，可选
}
```

### VerifyResponse（验签响应）

```json
{
  "success": true,        // 请求是否成功处理
  "valid": true,          // 签名是否有效
  "message": "string"     // 响应消息
}
```

---

## 调用示例

### cURL 示例

**签名请求**:
```bash
curl -X POST http://localhost:8080/api/v1/signature/sign \
  -H "Content-Type: application/json" \
  -d '{
    "appId": "APP001",
    "timestamp": "1704067200000",
    "nonce": "abc123xyz",
    "data": "{\"orderNo\":\"202401010001\"}"
  }'
```

**验签请求**:
```bash
curl -X POST http://localhost:8080/api/v1/signature/verify \
  -H "Content-Type: application/json" \
  -d '{
    "appId": "APP001",
    "signature": "A1B2C3D4E5F6789012345678901234567890ABCDEF1234567890ABCDEF123456"
  }'
```

**健康检查**:
```bash
curl http://localhost:8080/api/v1/signature/health
```

### Java 调用示例

```java
// 签名请求
SignRequest signRequest = SignRequest.builder()
    .appId("APP001")
    .timestamp(String.valueOf(System.currentTimeMillis()))
    .nonce(UUID.randomUUID().toString().substring(0, 8))
    .data("{\"orderNo\":\"202401010001\"}")
    .build();

// 发送 HTTP POST 请求
RestTemplate restTemplate = new RestTemplate();
ResponseEntity<SignResponse> response = restTemplate.postForEntity(
    "http://localhost:8080/api/v1/signature/sign",
    signRequest,
    SignResponse.class
);

SignResponse signResponse = response.getBody();
System.out.println("签名结果: " + signResponse.getSignature());
```

---

## 安全说明

### 1. 防重放攻击

- 使用时间戳（timestamp）和随机数（nonce）机制
- 建议时间戳有效期为 5 分钟
- 同一组（appId + timestamp + nonce）只能使用一次

### 2. 密钥管理

- 密钥应存储在服务端，不要暴露给客户端
- 生产环境建议使用不同的密钥
- 定期更换密钥以提高安全性

### 3. 数据传输

- 生产环境建议使用 HTTPS 传输
- 敏感数据建议加密存储

---

## 常见问题

### Q1: 签名和验签使用的密钥不一致会怎样？

A: 验签会失败，因为使用不同密钥生成的签名不匹配。

### Q2: 签名有效期是多久？

A: 签名记录在数据库永久保存，Redis 缓存 24 小时过期。

### Q3: 时间戳有什么要求？

A: 时间戳应为毫秒级时间戳，建议使用 System.currentTimeMillis() 生成。

### Q4: nonce 有什么要求？

A: nonce 应为随机字符串，建议长度 8-32 位，每次请求都应不同。

---

## 更新日志

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| 1.0.0 | 2024-01-01 | 初始版本，实现基础签名验签功能 |
