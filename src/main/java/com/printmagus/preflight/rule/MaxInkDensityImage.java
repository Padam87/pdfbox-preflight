package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import com.printmagus.preflight.util.SampledRasterReader;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
public class MaxInkDensityImage extends AbstractRule
{
    private Integer maxDensity;

    public MaxInkDensityImage(Integer maxDensity)
    {
        this.maxDensity = maxDensity;
    }

    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        try {
            ImageDensity printer = new ImageDensity(document, violations);

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

    public class ImageDensity extends PDFStreamEngine
    {
        PDDocument document;
        List<Violation> violations;

        ImageDensity(PDDocument document, List<Violation> violations) throws IOException
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

                    if (image.getColorSpace() instanceof PDDeviceCMYK) {
                        Raster r = SampledRasterReader.getRaster(image, image.getColorKeyMask());

                        float[] pixels = r.getPixels(0,0, r.getWidth(), r.getHeight(), (float[])null);

                        Float max = 0f;
                        for (int i = 0; i < pixels.length; i += 4) {
                            Float c = pixels[i] / 255 * 100;
                            Float m = pixels[i + 1] / 255 * 100;
                            Float y = pixels[i + 2] / 255 * 100;
                            Float k = pixels[i + 3] / 255 * 100;

                            Float density = c + m + y + k;

                            if (density > max) {
                                max = density;
                            }
                        }

                        if (max > maxDensity) {
                            String message = String.format("Image color density exceeds maximum of %d.", maxDensity);

                            HashMap<String, Object> context = new HashMap<String, Object>();

                            context.put("density", max);
                            context.put("image", image);

                            Violation violation = new Violation(
                                MaxInkDensityImage.class.getSimpleName(),
                                message,
                                document.getPages().indexOf(getCurrentPage()),
                                context
                            );

                            violations.add(violation);
                        }
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
