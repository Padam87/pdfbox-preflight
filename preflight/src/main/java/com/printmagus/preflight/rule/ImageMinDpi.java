package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.util.Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Not a part of any standard, but very important for QA.
 *
 * Images under a certain DPI (~300) can appear crude on paper.
 * Customers should be warned about this.
 *
 * Callas technote reference: -
 */
public class ImageMinDpi extends AbstractRule implements XObjectValidator
{
    private Integer min;
    private PDDocument document;

    public ImageMinDpi(Integer min)
    {
        this.min = min;
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

        Matrix ctm = graphicsState.getCurrentTransformationMatrix();

        Integer DpiX = (int) Math.ceil(Math.abs(image.getWidth() * 72 / ctm.getScaleX()));
        Integer DpiY = (int) Math.ceil(Math.abs(image.getHeight() * 72 / ctm.getScaleY()));

        if (DpiX < min || DpiY < min) {
            HashMap<String, Object> context = new HashMap<String, Object>();

            context.put("image", image);
            context.put("dpiX", DpiX);
            context.put("dpiY", DpiY);

            Violation violation = new Violation(
                ImageMinDpi.class.getSimpleName(),
                String.format("Image with low DPI (X: %d, Y: %d)", DpiX, DpiY),
                document.getPages().indexOf(page),
                context
            );

            violations.add(violation);
        }

        return violations;
    }
}
