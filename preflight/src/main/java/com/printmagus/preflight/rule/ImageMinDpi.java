package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import com.printmagus.preflight.util.ImageProcessingStreamEngine;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;
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
public class ImageMinDpi extends AbstractRule
{
    private Integer min;

    public ImageMinDpi(Integer min)
    {
        this.min = min;
    }

    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        try {
            ImageDpi printer = new ImageDpi(document, violations);

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

    public class ImageDpi extends ImageProcessingStreamEngine
    {
        PDDocument document;
        List<Violation> violations;

        ImageDpi(PDDocument document, List<Violation> violations) throws IOException
        {
            this.document = document;
            this.violations = violations;

            addOperator(new Concatenate());
            addOperator(new SetGraphicsStateParameters());
            addOperator(new Save());
            addOperator(new Restore());
            addOperator(new SetMatrix());
        }

        @Override
        protected void processImage(COSName objectName, PDImageXObject image)
        {
            Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();

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
                    document.getPages().indexOf(getCurrentPage()),
                    context
                );

                violations.add(violation);
            }
        }
    }
}
