package com.printmagus.preflight.serializer;

import com.google.gson.GsonBuilder;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class GsonBuilderFactory
{
    public static GsonBuilder create()
    {
        GsonBuilder builder = new GsonBuilder();

        COSSerializer cosSerializer = new COSSerializer();

        // Convert PD classes to COS for serialization.
        // If a PD class has an explicitly defined serializer (below), that will take precedence.
        builder.registerTypeHierarchyAdapter(COSObjectable.class, cosSerializer.new COSObjectableSerializer());

        // PD class conversions
        builder.registerTypeAdapter(PDImageXObject.class, new PDImageXObjectSerializer().new NullSerializer());


        // Shows a message that the COS class serialization is not implemented.
        // If a COS class has an explicitly defined serializer (below), that will take precedence.
        builder.registerTypeHierarchyAdapter(COSBase.class, cosSerializer.new COSBaseSerializer());

        // COS class conversions
        builder.registerTypeHierarchyAdapter(COSDictionary.class, cosSerializer.new COSDictionarySerializer());
        builder.registerTypeAdapter(COSName.class, cosSerializer.new COSNameSerializer());
        builder.registerTypeAdapter(COSString.class, cosSerializer.new COSStringSerializer());
        builder.registerTypeAdapter(COSBoolean.class, cosSerializer.new COSBooleanSerializer());
        builder.registerTypeAdapter(COSArray.class, cosSerializer.new COSArraySerializer());
        builder.registerTypeAdapter(COSFloat.class, cosSerializer.new COSFloatSerializer());
        builder.registerTypeAdapter(COSInteger.class, cosSerializer.new COSIntegerSerializer());

        return builder;
    }
}
