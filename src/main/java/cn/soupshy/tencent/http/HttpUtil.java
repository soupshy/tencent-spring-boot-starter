package cn.soupshy.tencent.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Component
@Slf4j
public class HttpUtil {

    @Resource
    private HttpConnectionManager httpConnectionManager = new HttpConnectionManager();

    private JSONObject httpClintExecute(HttpRequestBase httpRequestBase, CloseableHttpClient httpClient) throws Exception{
        HttpResponse httpResponse = httpClient.execute(httpRequestBase);
        HttpEntity he = httpResponse.getEntity();
        JSONObject respContent = JSON.parseObject(EntityUtils.toString(he,"UTF-8"));
        EntityUtils.consume(he);
        return respContent;
    }


    public JSONObject doPost(String url, JSONObject pms, Map<String,String> headers) throws Exception{
        return doPost(url, pms, headers, true, -1, -1);
    }

    public JSONObject upload(String url) throws Exception{
        CloseableHttpClient client = httpConnectionManager.getHttpClient(-1, -1);
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = client.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        //建立gzip解压工作流
        @Cleanup GZIPInputStream gZin = new GZIPInputStream(httpEntity.getContent());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int num;
        byte[] buf = new byte[1024];
        while ((num = gZin.read(buf, 0, buf.length)) != -1) {
            byteArrayOutputStream.write(buf, 0, num);
        }
        EntityUtils.consume(httpEntity);
        return JSON.parseObject(byteArrayOutputStream.toString("utf-8").replace("\n",""));
    }

    public JSONObject doPost(String url, JSONObject pms, Map<String,String>headers, boolean checkResult) throws Exception{
        return doPost(url, pms, headers, checkResult, -1, -1);
    }

    public JSONObject doPost(String url, JSONObject pms, Map headers, boolean checkResult, int retryTimes, int socketTimeout) throws Exception{
        CloseableHttpClient client = httpConnectionManager.getHttpClient(retryTimes, socketTimeout);
        HttpPost httpPost = new HttpPost(url);
        setEntity(pms, httpPost);

        setHeader(httpPost,headers);

        JSONObject respContent = httpClintExecute(httpPost, client);
        checkServiceResult(respContent, checkResult);
        return respContent;
    }

    private void setEntity(JSONObject pms, HttpEntityEnclosingRequestBase httpRequestBase) {
        //json方式
        //解决中文乱码问题
        StringEntity entity = new StringEntity(pms.toJSONString(),"utf-8");
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpRequestBase.setEntity(entity);
    }

    private void setHeader(HttpRequestBase httpRequestBase, Map<String,String> headers) {
        if(null != headers){
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpRequestBase.setHeader(entry.getKey(),entry.getValue());
            }
        }
    }

    private void checkServiceResult(JSONObject respContent, boolean checkResult) throws Exception {
        //如果传false,不检查返回结果
        if(!checkResult)return;
        if(respContent == null){
            log.error("调用腾讯云IM API返回体为空");
        }
        int code = respContent.getInteger("ErrorCode");
        if (0 != code){
            log.error("调用腾讯云IM API失败 返回 msg :" + respContent.getString("ErrorInfo"));
            throw new Exception("调用腾讯云IM API失败 返回 msg :"+ respContent.getString("ErrorInfo"));
        }
    }

}
