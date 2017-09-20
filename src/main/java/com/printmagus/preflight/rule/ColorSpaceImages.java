package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
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
            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();

                for (COSName name : resources.getXObjectNames()) {
                    if (resources.isImageXObject(name)) {
                        PDImageXObject image = (PDImageXObject)resources.getXObject(name);

                        if (!isValidColorSpace(image.getColorSpace())) {
                            HashMap<String, Object> context = new HashMap<String, Object>();

                            context.put("image", image);
                            context.put("colorSpace", image.getColorSpace());

                            Violation violation = new Violation(
                                this.getClass().getSimpleName(),
                                String.format("Invalid image ColorSpace found : %s.", image.getColorSpace().getName()),
                                document.getPages().indexOf(page),
                                context
                            );

                            violations.add(violation);
                        }
                    }
                }
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

    private Boolean isValidColorSpace(PDColorSpace colorSpace)
    {
        Boolean valid = allowedColorSpaces.isEmpty();

        if (allowedColorSpaces.contains(colorSpace.getClass().getName())) {
            valid = true;
        }

        if (disallowedColorSpaces.contains(colorSpace.getClass().getName())) {
            valid = false;
        }

        return valid;
    }
}
