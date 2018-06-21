package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import com.printmagus.preflight.util.ColorSpaceName;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;

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
public class ColorSpacePage extends AbstractRule
{
    private List<COSName> allowedColorSpaces;
    private List<COSName> disallowedColorSpaces;

    public ColorSpacePage(List<COSName> allowedColorSpaces)
    {
        this.allowedColorSpaces = allowedColorSpaces;
        this.disallowedColorSpaces = new ArrayList<>();
    }

    public ColorSpacePage(List<COSName> allowedColorSpaces, List<COSName> disallowedColorSpaces)
    {
        this.allowedColorSpaces = allowedColorSpaces;
        this.disallowedColorSpaces = disallowedColorSpaces;
    }

    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        for (PDPage page: document.getPages()) {
            COSDictionary resources = (COSDictionary) page.getCOSObject().getDictionaryObject(COSName.RESOURCES);

            if (resources == null || !resources.containsKey(COSName.COLORSPACE)) {
                continue;
            }

            COSDictionary colorSpaces = (COSDictionary) resources.getDictionaryObject(COSName.COLORSPACE);

            for (COSName name: page.getResources().getColorSpaceNames()) {
                COSBase colorSpace = colorSpaces.getDictionaryObject(name);

                try {
                    COSName cs = ColorSpaceName.get(colorSpace);

                    if (!this.isValidColorSpace(cs)) {
                        HashMap<String, Object> context = new HashMap<String, Object>();

                        context.put("colorSpace", cs.getName());

                        Violation violation = new Violation(
                            ColorSpaceText.class.getSimpleName(),
                            "color_space_page.invalid.%colorSpace%",
                            document.getPages().indexOf(page),
                            context
                        );

                        violations.add(violation);
                    }
                } catch (IOException e) {
                    //
                }
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
