package com.zdv.codyuncang.model.converter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Info:
 * Created by xiaoyl
 * 创建时间:2017/4/14 11:03
 */

public class CustomXmlConverter extends Converter.Factory{
    /** Create an instance using a default {@link Persister} instance for conversion. */
    public static CustomXmlConverter create() {
        return create(new Persister());
    }

    /** Create an instance using {@code serializer} for conversion. */
    public static CustomXmlConverter create(Serializer serializer) {
        return new CustomXmlConverter(serializer, true);
    }

    /** Create an instance using a default {@link Persister} instance for non-strict conversion. */
    public static CustomXmlConverter createNonStrict() {
        return createNonStrict(new Persister());
    }

    /** Create an instance using {@code serializer} for non-strict conversion. */
    public static CustomXmlConverter createNonStrict(Serializer serializer) {
        return new CustomXmlConverter(serializer, false);
    }

    private final Serializer serializer;
    private final boolean strict;

    private CustomXmlConverter(Serializer serializer, boolean strict) {
        if (serializer == null) throw new NullPointerException("serializer == null");
        this.serializer = serializer;
        this.strict = strict;
    }

    public boolean isStrict() {
        return strict;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        if (!(type instanceof Class)) {
            return null;
        }
        Class<?> cls = (Class<?>) type;
        return new CustomXmlResponseBody<>(cls, serializer, strict);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (!(type instanceof Class)) {
            return null;
        }
        return new CustomXmlRequestBody<>(serializer);
    }
}
