package com.cn.util;

import com.google.gson.*;

import java.lang.reflect.Type;

public class GSONFloatAdapter implements JsonSerializer<Float>, JsonDeserializer<Float> {
    @Override
    public Float deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        //定义为float类型,如果后台返回""或者null,则返回0.00
        if ("".equals(jsonElement.getAsString()) || "null".equals(jsonElement.getAsString())) {
            return 0.00f;
        }
        try {
            return jsonElement.getAsFloat();
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException(e);
        }
    }

    @Override
    public JsonElement serialize(Float aFloat, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(aFloat);
    }
}
