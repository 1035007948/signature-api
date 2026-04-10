# API接口详细文档

## 接口规范

### 通信协议
- 协议：HTTP/HTTPS
- 字符编码：UTF-8
- 请求格式：JSON
- 响应格式：JSON

### 通用响应格式

所有接口返回统一的响应格式：

```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 响应状态码：200成功，500失败，400参数错误 |
| message | String | 响应消息描述 |
| data | Object | 响应业务数据 |

### 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 接口详情

### 1. 生成签名接口

**接口说明**：对请求内容进行数字签名，签名结果将同时存储到MySQL数据库和Redis缓存中。

**接口地址**：`POST /api/signature/sign`

**请求头**：
- Content-Type: application/json

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| signer | body | String | 是 | 签名方唯一标识，用于区分不同调用方 |
| content | body | String | 是 | 待签名的原始内容字符串 |
| params | body | Map | 否 | 参数形式的待签名内容，与content二选一 |
| algorithm | body | String | 否 | 签名算法，可选值：HMAC-SHA256(默认)、SHA-256 |
| secretKey | body | String | 否 | 自定义签名密钥，不传入则使用系统默认密钥 |
| expireHours | body | Integer | 否 | 签名过期时间（小时），默认24小时 |

**请求示例**：
```json
{
    "signer": "mall-pay-system",
    "content": "merchantId=M001&orderNo=PAY202604100001&amount=1299.00&currency=CNY",
    "algorithm": "HMAC-SHA256",
    "secretKey": "M001-SecRet-Key-2026",
    "expireHours": 72
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| requestId | String | 32位唯一请求ID，可用于后续查询 |
| signature | String | 生成的签名值（64位十六进制字符串） |
| algorithm | String | 实际使用的签名算法 |

**响应示例**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "requestId": "f8a7e6d5c4b3a2918273645546372819",
        "signature": "a7c2f98d3e6b1548f0c3d7a92e4b6f8a0c2e4d6b8a0f3c5e7d9b1a3f5e7c9d2b",
        "algorithm": "HMAC-SHA256"
    }
}
```

---

### 2. 验签接口

**接口说明**：对客户端传入的签名进行合法性验证。优先从Redis缓存查询，未命中时查询数据库。

**接口地址**：`POST /api/signature/verify`

**请求头**：
- Content-Type: application/json

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| signature | body | String | 是 | 待验证的签名值 |
| content | body | String | 否 | 原始内容字符串 |
| params | body | Map | 否 | 参数形式的原始内容 |
| algorithm | body | String | 否 | 签名算法，默认HMAC-SHA256 |
| secretKey | body | String | 否 | 签名密钥，与签名时一致 |

**请求示例**：
```json
{
    "signature": "a7c2f98d3e6b1548f0c3d7a92e4b6f8a0c2e4d6b8a0f3c5e7d9b1a3f5e7c9d2b",
    "content": "merchantId=M001&orderNo=PAY202604100001&amount=1299.00&currency=CNY",
    "algorithm": "HMAC-SHA256",
    "secretKey": "M001-SecRet-Key-2026"
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| valid | Boolean | 验签结果：true=签名有效，false=签名无效 |
| signature | String | 传入的签名值 |
| fromCache | Boolean | 是否从Redis缓存获取数据 |
| requestId | String | 签名时生成的请求ID |
| signer | String | 签名方标识 |
| timestamp | String | 签名时间戳 |
| verifiedCount | Integer | 该签名累计验签次数 |

**响应示例 - 验签成功**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "valid": true,
        "signature": "a7c2f98d3e6b1548f0c3d7a92e4b6f8a0c2e4d6b8a0f3c5e7d9b1a3f5e7c9d2b",
        "fromCache": true,
        "requestId": "f8a7e6d5c4b3a2918273645546372819",
        "signer": "mall-pay-system",
        "timestamp": "2026-04-10T14:30:00",
        "verifiedCount": 3
    }
}
```

**响应示例 - 验签失败**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "valid": false,
        "signature": "invalid-signature-value",
        "fromCache": false
    }
}
```

---

### 3. 根据签名值查询记录

**接口说明**：根据签名值查询完整的签名记录详情

**接口地址**：`GET /api/signature/record/{signature}`

**路径参数**：
- signature：签名值（URL编码）

**响应示例**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "requestId": "f8a7e6d5c4b3a2918273645546372819",
        "signer": "mall-pay-system",
        "content": "merchantId=M001&orderNo=PAY202604100001&amount=1299.00",
        "signatureValue": "a7c2f98d3e6b1548f0c3d7a92e4b6f8a0c2e4d6b8a0f3c5e7d9b1a3f5e7c9d2b",
        "algorithm": "HMAC-SHA256",
        "timestamp": "2026-04-10T14:30:00",
        "expireTime": "2026-04-13T14:30:00",
        "verifiedCount": 3,
        "status": "VALID"
    }
}
```

---

### 4. 根据请求ID查询记录

**接口地址**：`GET /api/signature/record/request/{requestId}`

**路径参数**：
- requestId：签名时返回的32位请求ID

---

## 在线调试

项目启动后，可通过SwaggerUI进行在线接口调试：

- 地址：http://localhost:8080/swagger-ui.html
- JSON文档：http://localhost:8080/v2/api-docs

---

## Java调用示例

```java
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

public class SignatureClient {
    
    private static final String BASE_URL = "http://localhost:8080";
    private RestTemplate restTemplate = new RestTemplate();
    
    public String sign(String signer, String content) {
        String url = BASE_URL + "/api/signature/sign";
        Map<String, Object> request = new HashMap<>();
        request.put("signer", signer);
        request.put("content", content);
        request.put("algorithm", "HMAC-SHA256");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return (String) data.get("signature");
    }
    
    public boolean verify(String signature, String content) {
        String url = BASE_URL + "/api/signature/verify";
        Map<String, Object> request = new HashMap<>();
        request.put("signature", signature);
        request.put("content", content);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return (Boolean) data.get("valid");
    }
}
```
