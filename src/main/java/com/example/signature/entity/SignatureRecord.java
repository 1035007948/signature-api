/**
 * 签名记录实体类
 *
 * 对应数据库表：signature_record
 * 用于存储签名相关的所有信息
 */
package com.example.signature.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 签名记录实体类
 * 使用 JPA 注解映射数据库表结构
 * 使用 Lombok 注解简化代码
 */
@Data  // Lombok：生成 getter、setter、toString、equals、hashCode
@NoArgsConstructor  // Lombok：生成无参构造方法
@AllArgsConstructor  // Lombok：生成全参构造方法
@Builder  // Lombok：生成建造者模式
@Entity  // JPA：标识为实体类
@Table(name = "signature_record")  // JPA：指定表名
public class SignatureRecord {

    /**
     * 主键 ID
     * 自增策略：数据库 ID 自增
     */
    @Id  // JPA：标识为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // JPA：自增策略
    private Long id;

    /**
     * 应用 ID
     * 用于标识不同的应用系统
     * 非空字段，最大长度 64
     */
    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    /**
     * 时间戳
     * 请求的时间戳，用于防重放攻击
     * 非空字段，最大长度 20
     */
    @Column(name = "timestamp", nullable = false, length = 20)
    private String timestamp;

    /**
     * 随机数
     * 随机生成的字符串，用于防重放攻击
     * 非空字段，最大长度 32
     */
    @Column(name = "nonce", nullable = false, length = 32)
    private String nonce;

    /**
     * 数据内容
     * 需要签名的业务数据（JSON 格式）
     * 可为空，使用 TEXT 类型存储
     */
    @Column(name = "data_content", columnDefinition = "TEXT")
    private String dataContent;

    /**
     * 签名结果
     * SHA256 算法生成的签名字符串
     * 非空字段，最大长度 256
     */
    @Column(name = "signature", nullable = false, length = 256)
    private String signature;

    /**
     * 签名类型
     * 标识使用的签名算法，如 SHA256
     * 可为空，最大长度 10
     */
    @Column(name = "sign_type", length = 10)
    private String signType;

    /**
     * 创建时间
     * 记录创建时间，自动填充
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 记录最后更新时间，自动填充
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 实体持久化前回调方法
     * 自动设置创建时间和更新时间
     */
    @PrePersist  // JPA：持久化前执行
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    /**
     * 实体更新前回调方法
     * 自动更新更新时间
     */
    @PreUpdate  // JPA：更新前执行
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

}
