package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import com.printmagus.preflight.util.ColorSpaceName;
import com.printmagus.preflight.util.SampledRasterReader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import java.awt.image.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Not a part of any standard, but very important for QA.
 *
 * Images under a certain DPI (~300) can appear crude on paper.
 * Customers should be warned about this.
 *
 * BE WARNED: This rule is very slow compared to the others.
 * A pixel by pixel check is the only way to to this as far as I know.
 * GhostScript implements it the same way.
 *
 * Callas technote reference: -
 */
public class MaxInkDensityImage extends AbstractRule implements XObjectValidator
{
    HashMap<COSName, Float> cache;
    private Integer maxDensity;
    private Integer maxArea;
    private PDDocument document;

    public MaxInkDensityImage(Integer maxDensity)
    {
        this.maxDensity = maxDensity;
        this.maxArea = Integer.MAX_VALUE;
        this.cache = new HashMap<COSName, Float>();
    }

    public MaxInkDensityImage(Integer maxDensity, Integer maxArea)
    {
        this.maxDensity = maxDensity;
        this.maxArea = maxArea;
        this.cache = new HashMap<COSName, Float>();
    }

    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        this.document = document;

        streamEngine.addValidator(this);
    }

    @Override
    public List<Violation> validate(COSName objectName, PDXObject xobject, PDPage page, PDGraphicsState graphicsState)
    {
        List<Violation> violations = new ArrayList<>();

        if (!(xobject instanceof PDImageXObject)) {
            return violations;
        }

        PDImageXObject image = (PDImageXObject) xobject;

        if (image.getWidth() * image.getHeight() > maxArea) {
            return violations;
        }

        try {
            Float max = 0f;

            if (cache.containsKey(objectName)) {
                max = cache.get(objectName);
            } else if (ColorSpaceName.get(image) == COSName.DEVICECMYK) {
                Raster r = SampledRasterReader.getRaster(image, image.getColorKeyMask());

                float[] pixels = r.getPixels(0,0, r.getWidth(), r.getHeight(), (float[])null);

                for (int i = 0; i < pixels.length; i += 4) {
                    Float density = pixels[i] + pixels[i + 1] + pixels[i + 2] + pixels[i + 3];

                    if (density > max) {
                        max = density;
                    }
                }

                max = max / 255 * 100;
            }

            cache.put(objectName, max);

            if (max > maxDensity) {
                HashMap<String, Object> context = new HashMap<String, Object>();

                context.put("density", max);
                context.put("image", image);

                Violation violation = new Violation(
                    MaxInkDensityImage.class.getSimpleName(),
                    String.format("Image color density exceeds maximum of %d.", maxDensity),
                    document.getPages().indexOf(page),
                    context
                );

                violations.add(violation);
            }
        } catch (IOException e) {
            //
        }

        return violations;
    }
}
