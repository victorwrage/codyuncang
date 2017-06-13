package com.zdv.codyuncang.model.converter;


import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Info:
 * Created by xiaoyl
 * 创建时间:2017/4/7 11:45
 */

final class CustomGsonResponseBody<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final TypeAdapter<T> adapter;

    CustomGsonResponseBody(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
      //  KLog.v("wrage---:"+new String(value.bytes(),"UTF-8"));
        JsonReader jsonReader = gson.newJsonReader(value.charStream());
        try {

            return adapter.read(jsonReader);
        } finally {
            value.close();
        }
    }

}
