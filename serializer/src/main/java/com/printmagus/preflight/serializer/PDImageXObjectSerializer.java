package com.printmagus.preflight.serializer;

import com.google.gson.*;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.HashMap;

public class PDImageXObjectSerializer
{
    public static String key = String.valueOf(System.currentTimeMillis());

    public class NullSerializer implements JsonSerializer<PDImageXObject>
    {
        public JsonElement serialize(PDImageXObject src, Type typeOfSrc, JsonSerializationContext context)
        {
            return null;
        }
    }

    public class Base64Serializer implements JsonSerializer<PDImageXObject>
    {
        HashMap<Integer, String> cache = new HashMap<>();

        public JsonElement serialize(PDImageXObject src, Type typeOfSrc, JsonSerializationContext context)
        {
            String result = null;

            if (cache.containsKey(src.hashCode())) {
                result = cache.get(src.hashCode());
            } else {
                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    Base64OutputStream b64 = new Base64OutputStream(os);

                    ImageIO.write(src.getImage(), "png", b64);

                    result = os.toString("UTF-8");

                    cache.put(src.hashCode(), result);
                } catch (Exception e) {
                    // null is fine
                }
            }

            return result == null
                ? null
                : new JsonPrimitive(result);
        }
    }

    public class FileSerializer implements JsonSerializer<PDImageXObject>
    {
        private HashMap<Integer, File> cache = new HashMap<>();
        private String directory;
        private Boolean referenceOnly;

        public FileSerializer(String directory)
        {
            this.directory = directory;
            this.referenceOnly = false;
        }

        public FileSerializer(String directory, Boolean referenceOnly)
        {
            this.directory = directory;
            this.referenceOnly = referenceOnly;
        }

        public JsonElement serialize(PDImageXObject src, Type typeOfSrc, JsonSerializationContext context)
        {
            if (cache.containsKey(src.hashCode())) {
                if (referenceOnly) {
                    return new JsonPrimitive(src.hashCode());
                }

                File file = cache.get(src.hashCode());

                return new JsonPrimitive(file.getAbsolutePath());
            } else {
                try {
                    File file = new File(
                        Paths.get(
                            directory,
                            String.valueOf(src.hashCode())
                        ).toString() + ".png"
                    );

                    file.getParentFile().mkdirs();

                    ImageIO.write(src.getImage(), "png", file);

                    cache.put(src.hashCode(), file);

                    if (referenceOnly) {
                        return new JsonPrimitive(src.hashCode());
                    }

                    return new JsonPrimitive(file.getAbsolutePath());
                } catch (Exception e) {
                    // null is fine
                }
            }

            return null;
        }

        public HashMap<Integer, File> getCache()
        {
            return cache;
        }

        public void setCache(HashMap<Integer, File> cache)
        {
            this.cache = cache;
        }

        public void clearCache()
        {
            this.cache = new HashMap<>();
        }
    }
}
