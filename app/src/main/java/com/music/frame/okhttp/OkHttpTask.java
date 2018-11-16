package com.music.frame.okhttp;

import android.text.TextUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.music.AppDroid;
import com.music.frame.bean.InfoResult;
import com.music.model.UserInfo;
import com.music.utils.NetworkUtils;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jack
 */

public class OkHttpTask<T> implements Runnable {

    /**
     * "application/x-www-form-urlencoded"，是默认的MIME内容编码类型，一般可以用于所有的情况，但是在传输比较大的二进制或者文本数据时效率低。
     这时候应该使用"multipart/form-data"。如上传文件或者二进制数据和非ASCII数据。
     */
   /* public static final MediaType MEDIA_TYPE_NORAML_FORM = MediaType.parse("application/x-www-form-urlencoded;charset=utf-8");
    //既可以提交普通键值对，也可以提交(多个)文件键值对。
    public static final MediaType MEDIA_TYPE_MULTIPART_FORM = MediaType.parse("multipart/form-data;charset=utf-8");
    //只能提交二进制，而且只能提交一个二进制，如果提交文件的话，只能提交一个文件,后台接收参数只能有一个，而且只能是流（或者字节数组）
    public static final MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream");
    public static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain;charset=utf-8");
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");*/

    private OkHttpRequest<T> okHttpRequest;
    private InfoResult infoResult;
    private Request request = null;
    private OkHttpListener okHttpListener;

    public OkHttpTask(OkHttpRequest<T> okHttpRequest,OkHttpListener okHttpListener){
        this.okHttpRequest = okHttpRequest;
        this.okHttpListener = okHttpListener;
    }

    @Override
    public void run() {
        if(NetworkUtils.getInstance().isNetworkConnected()){
            if (checkHttpAndHttpsStart(okHttpRequest.getUrl())) {
                //是否含有请求头
                Headers headers = null;
                if(okHttpRequest.isHeader()){
                    HashMap<String, String> headerMap = new HashMap<>();
                    UserInfo userInfo = AppDroid.getInstance().getUserInfo();
                    if(userInfo!=null){
                        if (AppDroid.getInstance().getUserInfo() != null) {
                            headerMap.put("X-Token", AppDroid.getInstance().getUserInfo().getToken());
                        }
                        headers = Headers.of(headerMap);
                    }
                }

                if(okHttpRequest.isFile()){

                    boolean isSingleFile = okHttpRequest.isSingleFile;
                    RequestBody body = null;
                    MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
                    multipartBodyBuilder.setType(MultipartBody.FORM);

                    if(okHttpRequest.getRequestParams().size()>0){
                        //判断是否有文件
                        int count = 0;
                        for(int i = 0;i<okHttpRequest.getRequestParams().size();i++){
                            if(okHttpRequest.getRequestParams().get(i).getValue() instanceof File){
                                count = count + 1;
                            }
                        }
                        //表单提交
                        //存在文件
                        Map<String, String> map = new HashMap<>();
                        Map<String, File> mapFiles = new HashMap<>();
                        List<File> files = new ArrayList<>();
                        for(int i = 0;i<okHttpRequest.getRequestParams().size();i++){
                            if(okHttpRequest.getRequestParams().get(i).getValue() instanceof File){
                                mapFiles.put(okHttpRequest.getRequestParams().get(i).getKey(),
                                        (File) okHttpRequest.getRequestParams().get(i).getValue());
                                files.add((File) okHttpRequest.getRequestParams().get(i).getValue());
                            }else {
                                map.put(okHttpRequest.getRequestParams().get(i).getKey(),
                                        (String) okHttpRequest.getRequestParams().get(i).getValue());
                            }
                        }
                        if (map.size()>0) {
                            String sign = getSignContent(map).toUpperCase();
                            map.put("sign", sign);
                            for (String key : map.keySet()) {
                                String value = map.get(key);
                                multipartBodyBuilder.addFormDataPart(key, value);
                            }
                        }
                        if(count>0){
                            MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
                            //单个文件提交
                            if(count==1){
                                if(isSingleFile){
                                    multipartBodyBuilder.addFormDataPart("files", files.get(0).getName(),
                                            RequestBody.create(MEDIA_TYPE_PNG, files.get(0)));
                                }else {
                                    multipartBodyBuilder.addFormDataPart("files", files.get(0).getName(),
                                            RequestBody.create(MEDIA_TYPE_PNG, files.get(0)));
                                }
                            }else {
                               /* List<String> keys = new ArrayList<String>(mapFiles.keySet());
                                for (int i = 0; i < keys.size(); i++) {
                                    String key = keys.get(i);
                                    File file = mapFiles.get(key);
                                    multipartBodyBuilder.addFormDataPart(key, file.getName(),
                                            RequestBody.create(MEDIA_TYPE_PNG, file));
                                }*/
                                if (null != files && files.size() > 0) {
                                    for (File file : files) {
                                        multipartBodyBuilder.addFormDataPart("files", file.getName(),
                                                RequestBody.create(MEDIA_TYPE_PNG, file));
                                    }
                                }
                            }
                        }
                        body = multipartBodyBuilder.build();
                    }else {
                        body = multipartBodyBuilder.build();
                    }
                    if(headers != null){
                        request = new Request.Builder()
                                .url(okHttpRequest.getUrl())
                                .post(body)
                                .tag(okHttpRequest.getTag())
                                .headers(headers)
                                .build();
                    }else {
                        request = new Request.Builder()
                                .url(okHttpRequest.getUrl())
                                .post(body)
                                .tag(okHttpRequest.getTag())
                                .build();
                    }
                    try {
                        Response response = OkHttpUtils.getInstance().getOkHttpClient().newCall(request).execute();
                        if (response.isSuccessful()) {
                            String responseBodyString = response.body().string();
                            String data = "";
                            if (!TextUtils.isEmpty(responseBodyString)) {
                                //这里根据项目约定解析返回的数据
                                JSONObject jsonObject = JSON.parseObject(responseBodyString);
                                if(jsonObject!=null){
                                    if (jsonObject.getIntValue("code") == 1) { // 业务真正的成功。
                                        data = jsonObject.getString("data");
                                        if(!TextUtils.isEmpty(data)){
                                            T result = okHttpRequest.getResult(data);
                                            infoResult = new InfoResult<>(true, result, 1, jsonObject.getString("desc"));
                                        }else{
                                            infoResult = new InfoResult<>(true, null, 1, jsonObject.getString("desc"));
                                        }
                                    } else {
                                        infoResult = new InfoResult<>(false, null, jsonObject.getIntValue("code"),
                                                jsonObject.getString("desc"));
                                    }
                                }else {
                                    infoResult = new InfoResult<>(false, null, -1,"服务器数据格式错误，请稍后重试!");
                                }
                            }else {
                                infoResult = new InfoResult<>(false, null, -1,"服务器数据格式错误，请稍后重试!");
                            }
                        }else{
                            infoResult = new InfoResult<>(false, null, -1,"请求错误，请稍后重试!");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        infoResult = new InfoResult<>(false, null, -1,"服务器断开连接!");
                    }

                }else {
                    RequestBody body = null;
                    String json = " ";
                    if(okHttpRequest.getRequestParams().size()>0){
                        //判断是否有文件
                        int count = 0;
                        for(int i = 0;i<okHttpRequest.getRequestParams().size();i++){
                            if(okHttpRequest.getRequestParams().get(i).getValue() instanceof File){
                                count = count + 1;
                            }
                        }
                        Map<String, String> map = new HashMap<>();
                        for(int i = 0;i<okHttpRequest.getRequestParams().size();i++){
                            map.put(okHttpRequest.getRequestParams().get(i).getKey(),
                                    (String) okHttpRequest.getRequestParams().get(i).getValue());
                        }
                        json = toBody(map,null);
                        body = RequestBody.create(MediaType.parse("application/json"), json);
                    }else {
                        body = RequestBody.create(MediaType.parse("application/json"), json);
                    }
                    if(headers != null){
                        request = new Request.Builder()
                                .url(okHttpRequest.getUrl())
                                .post(body)
                                .tag(okHttpRequest.getTag())
                                .headers(headers)
                                .build();
                    }else {
                        request = new Request.Builder()
                                .url(okHttpRequest.getUrl())
                                .post(body)
                                .tag(okHttpRequest.getTag())
                                .build();
                    }
                    try {
                        Response response = OkHttpUtils.getInstance().getOkHttpClient().newCall(request).execute();
                        if (response.isSuccessful()) {
                            String responseBodyString = response.body().string();
                            String data = "";
                            if (!TextUtils.isEmpty(responseBodyString)) {
                                //这里根据项目约定解析返回的数据
                                JSONObject jsonObject = JSON.parseObject(responseBodyString);
                                if(jsonObject!=null){
                                    if (jsonObject.getIntValue("code") == 1) { // 业务真正的成功。
                                        data = jsonObject.getString("data");
                                        if(!TextUtils.isEmpty(data)){
                                            T result = okHttpRequest.getResult(data);
                                            infoResult = new InfoResult<>(true, result, 1, jsonObject.getString("desc"));
                                        }else{
                                            infoResult = new InfoResult<>(true, null, 1, jsonObject.getString("desc"));
                                        }
                                    } else {
                                        infoResult = new InfoResult<>(false, null, jsonObject.getIntValue("code"),
                                                jsonObject.getString("desc"));
                                    }
                                }else {
                                    infoResult = new InfoResult<>(false, null, -1,"服务器数据格式错误，请稍后重试!");
                                }
                            }else {
                                infoResult = new InfoResult<>(false, null, -1,"服务器数据格式错误，请稍后重试!");
                            }
                        }else{
                            infoResult = new InfoResult<>(false, null, -1,"请求错误，请稍后重试!");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        infoResult = new InfoResult<>(false, null, -1,"服务器断开连接!");
                    }
                }
            }else{
                infoResult = new InfoResult<>(false, null, -1,"请求地址出错!");
            }
        }else{
            infoResult = new InfoResult<>(false, null, -1000,"没有网络连接, 请检查您的网络!");
        }
        if(infoResult.isSuccess()){
            okHttpListener.dataSucceed(okHttpRequest.getTag(),infoResult);
        }else{
            okHttpListener.dataFailed(okHttpRequest.getTag(),infoResult);
        }
    }

    /**
     * 生成请求的body体
     * @param map
     * @param listMap
     * @return
     */
    public String toBody(Map<String, String> map, Map<String, ArrayList<String>> listMap){
        if(map != null && map.size()>0){
            String sign = getSignContent(map);
            map.put("sign",sign.toUpperCase());
        }
        String body = " ";
        StringBuffer buffer= new StringBuffer();
        buffer.append("{");
        if(map != null && map.size()>0){
            Iterator iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                buffer.append("\""+key+"\"");
                buffer.append(":");
                buffer.append("\""+val+"\"");
                buffer.append(",");
            }
        }
        if(listMap != null && listMap.size()>0)
        {
            Iterator iter = listMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                ArrayList<String> list = (ArrayList<String>) entry.getValue();
                buffer.append("\""+key+"\"");
                buffer.append(":");
                buffer.append("[");
                if(list != null && list.size() > 0){
                    for(int i = 0; i < list.size(); i ++){
                        buffer.append("\""+list.get(i)+"\"");
                        if(i!=list.size()-1){
                            buffer.append(",");
                        }
                    }
                    buffer.append("]");
                }else{
                    buffer.append("]");
                }
                buffer.append(",");
            }
            body = buffer.toString().substring(0,buffer.toString().length()-1) + "}";
        }else{
            if(buffer.toString().length() == 1){
                return body;
            }else{
                body = buffer.toString().substring(0,buffer.toString().length()-1) + "}";
            }
        }
        return body;
    }

    private String getSignContent(Map<String, String> sortedParams) {
        StringBuffer content = new StringBuffer();
        List<String> keys = new ArrayList<String>(sortedParams.keySet());
        Collections.sort(keys);
        int index = 0;
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = sortedParams.get(key);
            if (!TextUtils.isEmpty(value)) {
                content.append((index == 0 ? "" : "&") + key + "=" + value);
                index++;
            }
        }
        String key = "&key=FEE58CD98162444D9F977DC3D930B27A";
        return toMD5(content.toString()+key);
    }

    private String toMD5(String txt){
        try {
            // 生成实现指定摘要算法的 MessageDigest 对象。
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 使用指定的字节数组更新摘要。
            md.update(txt.getBytes("utf-8"));
            // 通过执行诸如填充之类的最终操作完成哈希计算。
            byte b[] = md.digest();
            // 生成具体的md5密码到buf数组
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //System.out.println("32位: " + buf.toString());// 32位的加密
            //System.out.println("16位: " + buf.toString().substring(8, 24));// 16位的加密，其实就是32位加密后的截取
            return buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return txt;
    }

    /**
     * 检测url地址是否是以http或者https开头
     *
     * @param url
     * @return
     */
    private boolean checkHttpAndHttpsStart(String url) {

        boolean flag = false;
        if (TextUtils.isEmpty(url)) {
            return false;
        } else {
            Pattern pattern2 = Pattern
                    .compile("(http|ftp|https):\\/\\/([\\w.]+\\/?)\\S*");
            Matcher matcher2 = pattern2.matcher(url);
            return matcher2.find();
        }
    }
}
