package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import com.printmagus.preflight.util.ColorSpaceName;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;

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
public class ColorSpaceImages extends AbstractRule implements XObjectValidator
{
    private List<COSName> allowedColorSpaces;
    private List<COSName> disallowedColorSpaces;
    private PDDocument document;

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

        try {
            COSName colorSpace = ColorSpaceName.get(image);

            if (colorSpace != null && !isValidColorSpace(colorSpace)) {
                HashMap<String, Object> context = new HashMap<String, Object>();

                context.put("image", image);
                context.put("colorSpace", colorSpace.getName());

                Violation violation = new Violation(
                    ColorSpaceImages.class.getSimpleName(),
                    "color_space_images.invalid.%colorSpace%",
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
