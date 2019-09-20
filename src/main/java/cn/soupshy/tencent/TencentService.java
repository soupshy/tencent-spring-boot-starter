package cn.soupshy.tencent;

import cn.soupshy.tencent.http.ImAccountOperateInfo;
import com.tencentcloudapi.cms.v20190321.models.ImageModerationResponse;
import com.tencentcloudapi.cms.v20190321.models.TextModerationResponse;

/**
 * @author 石络
 */
public interface TencentService {

    /**
     * 文本内容检测
     */
    TextModerationResponse textModeration(String content);

    /**
     * 图片内容检测
     */
    ImageModerationResponse imageModeration(String fileUrl);

    /**
     * 设置IM资料 门店编号 门店员工编号 门店名称
     */
    void setImData(ImAccountOperateInfo info);

    /**
     * 腾讯云 操作IM账号
     */
    String operateImAccount(ImAccountOperateInfo info);
}
