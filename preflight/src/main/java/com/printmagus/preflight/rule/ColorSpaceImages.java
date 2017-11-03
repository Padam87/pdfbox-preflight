package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import com.printmagus.preflight.util.ColorSpaceName;
import com.printmagus.preflight.util.ImageProcessingStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private List<COSName> allowedColorSpaces;
    private List<COSName> disallowedColorSpaces;

    public ColorSpaceImages(List<COSName> allowedColorSpaces)
    {
        this.allowedColorSpaces = allowedColorSpaces;
        this.disallowedColorSpaces = new ArrayList<>();
    }

    public ColorSpaceImages(List<COSName> allowedColorSpaces, List<COSName> disallowedColorSpaces)
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

    public class ImageCs extends ImageProcessingStreamEngine
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

        protected void processImage(COSName objectName, PDImageXObject image)
        {
            try {
                COSName colorSpace = ColorSpaceName.get(image);

                if (colorSpace != null && !isValidColorSpace(colorSpace)) {
                    HashMap<String, Object> context = new HashMap<String, Object>();

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
            } catch (IOException e) {
                //
            }
        }
    }

    private Boolean isValidColorSpace(COSName colorSpace)
    {
        Boolean valid = allowedColorSpaces.isEmpty();

        if (allowedColorSpaces.contains(colorSpace)) {
            valid = true;
        }

        if (disallowedColorSpaces.contains(colorSpace)) {
            valid = false;
        }

        return valid;
    }
}
