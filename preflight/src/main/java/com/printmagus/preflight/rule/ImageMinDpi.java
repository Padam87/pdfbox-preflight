package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
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

    public class ImageDpi extends PDFStreamEngine
    {
        PDDocument document;
        List<Violation> violations;

        ImageDpi(PDDocument document, List<Violation> violations) throws IOException
        {
            this.document = document;
            this.violations = violations;

            addOperator(new Concatenate());
            addOperator(new DrawObject()); // text version - we just need the info, no need to actually draw them
            addOperator(new SetGraphicsStateParameters());
            addOperator(new Save());
            addOperator(new Restore());
            addOperator(new SetMatrix());
        }

        @Override
        protected void processOperator(Operator operator, List<COSBase> operands) throws IOException
        {
            if (operator.getName().equals("Do")) {
                COSName objectName = (COSName)operands.get(0);
                PDXObject xobject = getResources().getXObject(objectName);

                if (xobject instanceof PDImageXObject) {
                    PDImageXObject image = (PDImageXObject)xobject;
                    Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();

                    Float DpiX = Math.abs(image.getWidth() * 72 / ctm.getScaleX());
                    Float DpiY = Math.abs(image.getHeight() * 72 / ctm.getScaleY());

                    if (DpiX < min || DpiY < min) {
                        String message = String.format("Image with low DPI (X: %.0f, Y: %.0f)", DpiX, DpiY);

                        HashMap<String, Object> context = new HashMap<String, Object>();

                        context.put("image", image);
                        context.put("dpiX", DpiX);
                        context.put("dpiY", DpiY);

                        Violation violation = new Violation(
                            ImageMinDpi.class.getSimpleName(),
                            message,
                            document.getPages().indexOf(getCurrentPage()),
                            context
                        );

                        violations.add(violation);
                    }


                } else if (xobject instanceof PDFormXObject) {
                    PDFormXObject form = (PDFormXObject) xobject;
                    showForm(form);
                }
            } else {
                super.processOperator(operator, operands);
            }
        }
    }
}
