package com.printmagus.preflight.serializer;

import com.google.gson.*;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public class COSSerializer
{
    public class COSObjectableSerializer implements JsonSerializer<COSObjectable>
    {
        public JsonElement serialize(COSObjectable src, Type typeOfSrc, JsonSerializationContext context)
        {
            return context.serialize(src.getCOSObject());
        }
    }

    public class COSBaseSerializer implements JsonSerializer<COSBase>
    {
        public JsonElement serialize(COSBase src, Type typeOfSrc, JsonSerializationContext context)
        {
            return new JsonPrimitive(String.format("No serializer for COS class: %s", src.getClass().getName()));
        }
    }

    public class COSDictionarySerializer implements JsonSerializer<COSDictionary>
    {
        public JsonElement serialize(COSDictionary src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject result = new JsonObject();

            for (COSName key: src.keySet()) {
                result.add(key.getName(), context.serialize(src.getDictionaryObject(key)));
            }

            return result;
        }
    }

    public class COSNameSerializer implements JsonSerializer<COSName>
    {
        public JsonElement serialize(COSName src, Type typeOfSrc, JsonSerializationContext context)
        {
            return new JsonPrimitive(src.getName());
        }
    }

    public class COSStringSerializer implements JsonSerializer<COSString>
    {
        public JsonElement serialize(COSString src, Type typeOfSrc, JsonSerializationContext context)
        {
            return new JsonPrimitive(src.getString());
        }
    }

    public class COSBooleanSerializer implements JsonSerializer<COSBoolean>
    {
        public JsonElement serialize(COSBoolean src, Type typeOfSrc, JsonSerializationContext context)
        {
            return new JsonPrimitive(src.getValue());
        }
    }

    public class COSArraySerializer implements JsonSerializer<COSArray>
    {
        public JsonElement serialize(COSArray src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonArray array = new JsonArray();

            for (COSBase cos: src) {
                array.add(context.serialize(cos));
            }

            return array;
        }
    }

    public class COSFloatSerializer implements JsonSerializer<COSFloat>
    {
        public JsonElement serialize(COSFloat src, Type typeOfSrc, JsonSerializationContext context)
        {
            return new JsonPrimitive(src.floatValue());
        }
    }

    public class COSIntegerSerializer implements JsonSerializer<COSInteger>
    {
        public JsonElement serialize(COSInteger src, Type typeOfSrc, JsonSerializationContext context)
        {
            return new JsonPrimitive(src.intValue());
        }
    }
}

