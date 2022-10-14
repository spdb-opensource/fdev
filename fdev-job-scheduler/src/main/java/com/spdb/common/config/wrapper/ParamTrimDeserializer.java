package com.spdb.common.config.wrapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;

/**
 * @author lizz
 */
public class ParamTrimDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String trim = p.getText().trim();
        return StringEscapeUtils.unescapeJava(trim);
    }
}
