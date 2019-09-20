package cn.soupshy.tencent;

import cn.soupshy.tencent.http.HttpClientUtil;
import cn.soupshy.tencent.http.ImAccountOperateInfo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencentcloudapi.cms.v20190321.CmsClient;
import com.tencentcloudapi.cms.v20190321.models.ImageModerationRequest;
import com.tencentcloudapi.cms.v20190321.models.ImageModerationResponse;
import com.tencentcloudapi.cms.v20190321.models.TextModerationRequest;
import com.tencentcloudapi.cms.v20190321.models.TextModerationResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tls.tls_sigature.tls_sigature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author 石络
 */
@Slf4j
@Service
public class TencentServiceImpl implements TencentService {

    @Autowired
    private TencentConfiguration tencentConfiguration;
    @Autowired
    private HttpClientUtil HttpClientUtil;

    public static void main(String[] args) {
        String priKey = "-----BEGIN PRIVATE KEY-----\n" +
                "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgbG1zCR0LbVcUtxFz\n" +
                "Qs53gNUCnPV3pfTqBH4UO5PkmRKhRANCAASgB1G9WPZI7GUrRtVh7/PV00wbsqby\n" +
                "haYXMGQ0wc1n3MUCoO68TfHe9AAULFcTm23nvXe8BxRwA0pW0QYKk884\n" +
                "-----END PRIVATE KEY-----\n";
        tls_sigature.GenTLSSignatureResult result = tls_sigature.genSig(1400240133, "13", Integer.MAX_VALUE, priKey);
        System.out.println(JSON.toJSONString(result));
    }

    /**
     * 生成IM密钥
     *
     * @param identifier
     * @return
     */
    private String genSig(String identifier) {
        tls_sigature.GenTLSSignatureResult result = tls_sigature.genSig(tencentConfiguration.getSdkAppId(), identifier,
                Integer.MAX_VALUE, tencentConfiguration.getPriKey());
        if (StringUtils.isEmpty(result.urlSig)) {
            log.error("调用腾讯云 申请IM账户密钥失败 错误消息 {}", result.errMessage);
        }
        log.info("获取IM账号密钥成功 identifier {} urlSig {}", identifier, result.urlSig);
        return result.urlSig;
    }


    @Override
    public String operateImAccount(ImAccountOperateInfo info) {
        long start = System.currentTimeMillis();
        log.info("腾讯云 操作IM账号 identifier {} nick {} faceUrl {}", info.getIdentifier(),
                info.getNick(), info.getFaceUrl());
        JSONObject pms = new JSONObject();
        pms.put("Identifier", info.getIdentifier());
        if (!StringUtils.isEmpty(info.getNick())) {
            pms.put("Nick", info.getNick());
        }
        if (!StringUtils.isEmpty(info.getFaceUrl())) {
            pms.put("FaceUrl", info.getFaceUrl());
        }
        try {
            // 操作IM相关数据
            HttpClientUtil.doPost(tencentConfiguration.getOperateImAccountUrl(), pms, null);
            String urlSig = null;
            if (info.getIsRegister()) {
                urlSig = genSig(info.getIdentifier());
            }
            log.info("腾讯云 操作IM账号成功 耗时 {}ms", System.currentTimeMillis() - start);
            return urlSig;
        } catch (Exception e) {
            log.error("腾讯云 操作IM账号异常", e);
        }
        return null;
    }


    @Override
    public void setImData(ImAccountOperateInfo info) {
        try {
            JSONObject setPortraitJson = new JSONObject();
            setPortraitJson.put("From_Account", info.getIdentifier());
            JSONArray array = new JSONArray();
            // 设置门店编号
            if (!StringUtils.isEmpty(info.getStoreId())) {
                JSONObject jsonObjectStoreId = new JSONObject();
                jsonObjectStoreId.put("Tag", tencentConfiguration.getTagProfileCustomStoreId());
                jsonObjectStoreId.put("Value", info.getStoreId());
                array.add(jsonObjectStoreId);
            }
            // 设置门店员工编号
            if (!StringUtils.isEmpty(info.getStoreEmployeeId())) {
                JSONObject jsonObjectStoreEmployeeId = new JSONObject();
                jsonObjectStoreEmployeeId.put("Tag", tencentConfiguration.getTagProfileCustomStaffId());
                jsonObjectStoreEmployeeId.put("Value", info.getStoreEmployeeId());
                array.add(jsonObjectStoreEmployeeId);
            }
            // 设置门店名称
            if (!StringUtils.isEmpty(info.getStoreName())) {
                JSONObject jsonObjectStoreName = new JSONObject();
                jsonObjectStoreName.put("Tag", tencentConfiguration.getTagProfileCustomSName());
                jsonObjectStoreName.put("Value", info.getStoreName());
                array.add(jsonObjectStoreName);
            }
            setPortraitJson.put("ProfileItem", array);
            HttpClientUtil.doPost(tencentConfiguration.getSetPortraitUrl(), setPortraitJson, null);
        } catch (Exception e) {
            log.error("腾讯云 设置IM资料异常", e);
        }
    }

    @Override
    public TextModerationResponse textModeration(String content) {
        try {
            long start = System.currentTimeMillis();
            CmsClient client = getCmsClient();
            // 组装文本内容 需要base64编码
            byte[] encodeBase64 = Base64.encodeBase64(content.getBytes("UTF-8"));
            String params = "{\"Content\":\"" + new String(encodeBase64) + "\"}";
            TextModerationRequest req = TextModerationRequest.fromJsonString(params, TextModerationRequest.class);
            TextModerationResponse resp = client.TextModeration(req);
            log.info("调用腾讯云 文本内容检测接口成功: 耗时 {}ms 返回信息 {}", System.currentTimeMillis() - start,
                    TextModerationRequest.toJsonString(resp));
            return resp;
        } catch (TencentCloudSDKException te) {
            log.error("调用腾讯云 文本内容检测接口异常: {}", JSON.toJSONString(te));
        } catch (Exception e) {
            log.error("调用腾讯云 文本内容检测接口 未知异常", e);
        }
        return null;
    }


    @Override
    public ImageModerationResponse imageModeration(String fileUrl) {
        try {
            long start = System.currentTimeMillis();
            CmsClient client = getCmsClient();
            String params = "{\"FileUrl\":\"" + fileUrl + "\"}";
            ImageModerationRequest req = ImageModerationRequest.fromJsonString(params, ImageModerationRequest.class);
            ImageModerationResponse resp = client.ImageModeration(req);
            log.info("调用腾讯云 图片内容检测接口成功: 耗时 {}ms 返回信息 {}", System.currentTimeMillis() - start,
                    ImageModerationRequest.toJsonString(resp));
            return resp;
        } catch (TencentCloudSDKException te) {
            log.error("调用腾讯云 图片内容检测接口异常: {}", JSON.toJSONString(te));
        } catch (Exception e) {
            log.error("调用腾讯云 图片内容检测接口 未知异常", e);
        }
        return null;
    }

    /**
     * 获取CMS Client
     *
     * @return
     */
    private CmsClient getCmsClient() {
        Credential cred = new Credential(tencentConfiguration.getSecretId(), tencentConfiguration.getSecretKey());
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(tencentConfiguration.getEndpoint());
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        return new CmsClient(cred, tencentConfiguration.getRegion(), clientProfile);
    }
}
