package com.printmagus.preflight.util;

import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;

/**
 * A faster method the image colorspace - name only.
 *
 * PDIndexed creation is very slow, and we only need the name in most cases anyway.
 */
public class ColorSpaceName
{
    public static COSName get(PDImageXObject image) throws IOException
    {
        COSBase colorSpace = image.getCOSObject().getDictionaryObject(COSName.COLORSPACE, COSName.CS);

        return get(colorSpace);
    }

    public static COSName get(COSBase colorSpace) throws IOException
    {
        if (colorSpace instanceof COSObject) {
            colorSpace = ((COSObject) colorSpace).getObject();
        }

        if (colorSpace instanceof COSName) {
            return (COSName) colorSpace;
        } else if (colorSpace instanceof COSArray) {
            COSArray array = (COSArray)colorSpace;
            if (array.size() == 0) {
                throw new IOException("Colorspace array is empty");
            }

            COSBase base = array.getObject(0);
            if (!(base instanceof COSName)) {
                throw new IOException("First element in colorspace array must be a name");
            }

            COSName name = (COSName) base;

            // With indexed color spaces we are only interested in the underlying colorspace.
            if (name == COSName.INDEXED) {
                return get(array.getObject(1));
            }

            return name;
        } else {
            throw new IOException("Expected a name or array but got: " + colorSpace);
        }
    }
}
