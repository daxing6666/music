package com.music.frame.okhttp;

import com.alibaba.fastjson.JSON;
import java.util.List;

/**
 * Created by jack
 */

public class OkEntityListRequest<Entity> extends OkHttpRequest<List<Entity>>{

    private Class<Entity> aClazz;
    public OkEntityListRequest(int tag, String url, Class<Entity> clazz){
        super(tag, url);
        this.aClazz = clazz;
    }

    @Override
    protected List<Entity> getResult(String responseBody) {
        return JSON.parseArray(responseBody, aClazz);
    }

}
