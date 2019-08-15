package com.theforceprotocol.bbd.util;

import com.alibaba.fastjson.JSONObject;

public class JsonBuilder {
    private final JSONObject jsonObject;

    private JsonBuilder() {
        jsonObject = new JSONObject();
    }

    public static JSONObject successBuilder(Object data) {
        return new JsonBuilder().set("status", 200).set("msg", "success").set("data", data).build();
    }

    public static JSONObject errorBuilder(int status, String desc) {
        return new JsonBuilder().set("status", status).set("msg", desc).build();
    }

    private JsonBuilder set(String key, Object value) {
        jsonObject.put(key, value);
        return this;
    }

    private JSONObject build() {
        return jsonObject;
    }
}
