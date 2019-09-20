package cn.soupshy.tencent.http;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 操作IM账号信息
 * @author 石络
 */
@Data
@Accessors(chain = true)
public class ImAccountOperateInfo {

    /**IM账号唯一标识**/
    private String identifier;
    /**IM账号昵称**/
    private String nick;
    /**IM账号头像**/
    private String faceUrl;
    /**IM账号是否走新注册 是表示需要生成IM密钥 否表示只需更新IM昵称 头像信息**/
    private Boolean isRegister;

    /**IM账号资料 门店编号**/
    private String storeId;
    /**IM账号资料 门店员工编号**/
    private String storeEmployeeId;
    /**IM账号资料 门店名称**/
    private String storeName;
}
