package com.music.frame.okhttp;

import com.alibaba.fastjson.JSON;

/**
 * Created by jack
 */

public class OkEntityRequest<Entity> extends OkHttpRequest<Entity> {

    private Class<Entity> aClazz;
    public OkEntityRequest(int tag, String url, Class<Entity> clazz){
        super(tag, url);
        this.aClazz = clazz;
    }

    @Override
    protected Entity getResult(String responseBody) {
        return JSON.parseObject(responseBody, aClazz);
    }

}
