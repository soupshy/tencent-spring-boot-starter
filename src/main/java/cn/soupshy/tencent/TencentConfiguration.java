package cn.soupshy.tencent;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author 石络
 */
@Configuration
@ConfigurationProperties(prefix = "tencentyun")
@Data
public class TencentConfiguration {

    /**操作IM账号地址**/
    private String operateImAccountUrl;
    /**设置IM资料地址**/
    private String setPortraitUrl;
    /**第三方应用唯一标识**/
    private Long sdkAppId;
    /**私钥 用于生成IM账户密钥**/
    private String priKey;
    /**资料唯一标识 门店id **/
    private String tagProfileCustomStoreId;
    /**资料唯一标识 门店员工id **/
    private String tagProfileCustomStaffId;
    /**资料唯一标识 门店名称 **/
    private String tagProfileCustomSName;


    /**密钥ID**/
    private String secretId;
    /**密钥KEY**/
    private String secretKey;
    /**调用节点**/
    private String endpoint;
    /**地区**/
    private String region;
}
