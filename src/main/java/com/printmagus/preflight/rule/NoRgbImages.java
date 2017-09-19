package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class NoRgbImages extends AbstractRuleInterface
{
    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        try {
            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();

                for (COSName name : resources.getXObjectNames()) {
                    if (resources.isImageXObject(name)) {
                        PDImageXObject image = (PDImageXObject)resources.getXObject(name);

                        if (isRgbColorSpace(image.getColorSpace())) {
                            String message = "RGB image found.";

                            HashMap<String, Object> context = new HashMap<String, Object>();

                            context.put("image", image);

                            Violation violation = new Violation(
                                NoRgbText.class.getSimpleName(),
                                message,
                                document.getPages()
                                        .indexOf(page),
                                context
                            );
                        }
                    }
                }
            }
        } catch (IOException e) {
            violations.add(
                new Violation(
                    this.getClass()
                        .getSimpleName(),
                    String.format("An exception occurred during the parse process. Message: %s", e.getMessage()),
                    null
                )
            );
        }
    }

    private Boolean isRgbColorSpace(PDColorSpace colorSpace)
    {
        if (Objects.equals(colorSpace.getName(), "DeviceRGB")) {
            return true;
        }

        // TODO: An additional check needed for custom color spaces here

        return false;
    }
}
