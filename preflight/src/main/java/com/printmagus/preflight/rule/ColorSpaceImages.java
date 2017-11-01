package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import com.printmagus.preflight.util.SampledRasterReader;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.swing.text.html.HTMLDocument;
import java.awt.color.ColorSpace;
import java.awt.image.Raster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * The only color space that is not allowed in a PDF/X-3 file is plain RGB (DeviceRGB).
 *
 * For a PDF/X-1a file, only the base color spaces DeviceGray, DeviceCMYK and Separation
 * (spot colors) are allowed. This applies for the color actually used as well as for
 * any alternate color spaces.
 *
 * Callas technote reference:
 * - Uses DeviceRGB [PDF/X-3]
 * - Only DeviceCMYK and spot colors allowed [PDF/X-1a]
 */
public class ColorSpaceImages extends AbstractRule
{
    private List<String> allowedColorSpaces;
    private List<String> disallowedColorSpaces;

    public ColorSpaceImages(List<String> allowedColorSpaces)
    {
        this.allowedColorSpaces = allowedColorSpaces;
        this.disallowedColorSpaces = new ArrayList<>();
    }

    public ColorSpaceImages(List<String> allowedColorSpaces, List<String> disallowedColorSpaces)
    {
        this.allowedColorSpaces = allowedColorSpaces;
        this.disallowedColorSpaces = disallowedColorSpaces;
    }


    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        try {
            ColorSpaceImages.ImageCs printer = new ColorSpaceImages.ImageCs(document, violations);

            for (PDPage page : document.getPages()) {
                printer.processPage(page);
            }
        } catch (IOException e) {
            violations.add(
                new Violation(
                    this.getClass().getSimpleName(),
                    String.format("An exception occurred during the parse process. Message: %s", e.getMessage()),
                    null
                )
            );
        }
    }

    public class ImageCs extends PDFStreamEngine
    {
        PDDocument document;
        List<Violation> violations;
        HashMap<COSName, PDColorSpace> cache;

        ImageCs(PDDocument document, List<Violation> violations) throws IOException
        {
            this.document = document;
            this.violations = violations;
            this.cache = new HashMap<COSName, PDColorSpace>();
        }

        @Override
        protected void processOperator(Operator operator, List<COSBase> operands) throws IOException
        {
            if (operator.getName().equals("Do")) {
                COSName objectName = (COSName)operands.get(0);
                PDColorSpace colorSpace = null;

                if (cache.containsKey(objectName)) {
                    colorSpace = cache.get(objectName);
                } else {
                    if (getResources().isImageXObject(objectName)) {
                        PDImageXObject image = (PDImageXObject) getResources().getXObject(objectName);

                        colorSpace = image.getColorSpace();
                    }

                    cache.put(objectName, colorSpace);
                }

                if (colorSpace != null && !isValidColorSpace(colorSpace)) {
                    HashMap<String, Object> context = new HashMap<String, Object>();
                    PDImageXObject image = (PDImageXObject) getResources().getXObject(objectName);

                    context.put("image", image);
                    context.put("colorSpace", image.getColorSpace());

                    Violation violation = new Violation(
                        ColorSpaceImages.class.getSimpleName(),
                        String.format("Invalid image ColorSpace found : %s.", image.getColorSpace().getName()),
                        document.getPages().indexOf(getCurrentPage()),
                        context
                    );

                    violations.add(violation);
                }
            }
        }
    }

    private Boolean isValidColorSpace(PDColorSpace colorSpace)
    {
        Boolean valid = allowedColorSpaces.isEmpty();

        if (colorSpace instanceof PDIndexed) {
            colorSpace = ((PDIndexed) colorSpace).getBaseColorSpace();
        }

        if (allowedColorSpaces.contains(colorSpace.getClass().getName())) {
            valid = true;
        }

        if (disallowedColorSpaces.contains(colorSpace.getClass().getName())) {
            valid = false;
        }

        return valid;
    }
}
