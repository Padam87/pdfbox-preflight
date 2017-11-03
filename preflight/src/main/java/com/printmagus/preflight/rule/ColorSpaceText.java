package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.color.*;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.contentstream.operator.text.*;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

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
public class ColorSpaceText extends AbstractRule
{
    private List<COSName> allowedColorSpaces;
    private List<COSName> disallowedColorSpaces;

    public ColorSpaceText(List<COSName> allowedColorSpaces)
    {
        this.allowedColorSpaces = allowedColorSpaces;
        this.disallowedColorSpaces = new ArrayList<>();
    }

    public ColorSpaceText(List<COSName> allowedColorSpaces, List<COSName> disallowedColorSpaces)
    {
        this.allowedColorSpaces = allowedColorSpaces;
        this.disallowedColorSpaces = disallowedColorSpaces;
    }

    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        try {
            TextColors s = new TextColors(document, violations);

            for (PDPage page : document.getPages()) {
                s.processPage(page);
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

    class TextColors extends PDFStreamEngine
    {
        PDDocument document;
        List<Violation> violations;
        PDColorSpace currentStrokingColorSpace;
        PDColorSpace currentNonStrokingColorSpace;
        StringBuilder currentText = new StringBuilder();

        TextColors(PDDocument document, List<Violation> violations) throws IOException
        {
            this.document = document;
            this.violations = violations;

            addOperator(new BeginText());
            addOperator(new Concatenate());
            addOperator(new DrawObject()); // special text version
            addOperator(new EndText());
            addOperator(new SetGraphicsStateParameters());
            addOperator(new Save());
            addOperator(new Restore());
            addOperator(new NextLine());
            addOperator(new SetCharSpacing());
            addOperator(new MoveText());
            addOperator(new MoveTextSetLeading());
            addOperator(new SetFontAndSize());
            addOperator(new ShowText());
            addOperator(new ShowTextAdjusted());
            addOperator(new SetTextLeading());
            addOperator(new SetMatrix());
            addOperator(new SetTextRenderingMode());
            addOperator(new SetTextRise());
            addOperator(new SetWordSpacing());
            addOperator(new SetTextHorizontalScaling());
            addOperator(new ShowTextLine());
            addOperator(new ShowTextLineAndSpace());

            addOperator(new SetStrokingColorSpace());
            addOperator(new SetNonStrokingColorSpace());
            addOperator(new SetStrokingDeviceCMYKColor());
            addOperator(new SetNonStrokingDeviceCMYKColor());
            addOperator(new SetNonStrokingDeviceRGBColor());
            addOperator(new SetStrokingDeviceRGBColor());
            addOperator(new SetNonStrokingDeviceGrayColor());
            addOperator(new SetStrokingDeviceGrayColor());
            addOperator(new SetStrokingColor());
            addOperator(new SetStrokingColorN());
            addOperator(new SetNonStrokingColor());
            addOperator(new SetNonStrokingColorN());
        }

        @Override
        protected void showGlyph(
            Matrix textRenderingMatrix,
            PDFont font,
            int code,
            String unicode,
            Vector displacement
        ) throws IOException
        {
            super.showGlyph(textRenderingMatrix, font, code, unicode, displacement);

            PDGraphicsState state = getGraphicsState();

            if (currentStrokingColorSpace == null) {
                currentStrokingColorSpace = state.getStrokingColorSpace();
                currentNonStrokingColorSpace = state.getNonStrokingColorSpace();
            }

            if (currentStrokingColorSpace != state.getStrokingColorSpace()
                || currentNonStrokingColorSpace != state.getNonStrokingColorSpace()
            ) {
                processColorSpaceText();

                currentStrokingColorSpace = state.getStrokingColorSpace();
                currentNonStrokingColorSpace = state.getNonStrokingColorSpace();
                currentText = new StringBuilder();
            }

            currentText.append(unicode);
        }

        private void processColorSpaceText()
        {
            if (!this.isValidColorSpace(currentStrokingColorSpace) || !this.isValidColorSpace(currentNonStrokingColorSpace)) {
                HashMap<String, Object> context = new HashMap<String, Object>();

                context.put("text", currentText);
                context.put("colorSpaceStroking", currentStrokingColorSpace);
                context.put("colorSpaceNonStroking", currentNonStrokingColorSpace);

                Violation violation = new Violation(
                    ColorSpaceText.class.getSimpleName(),
                    String.format("Invalid image ColorSpace found : %s.", currentStrokingColorSpace),
                    document.getPages().indexOf(getCurrentPage()),
                    context
                );

                violations.add(violation);
            }
        }

        private Boolean isValidColorSpace(PDColorSpace colorSpace)
        {
            Boolean valid = allowedColorSpaces.isEmpty();

            if (colorSpace instanceof PDIndexed) {
                colorSpace = ((PDIndexed)colorSpace).getBaseColorSpace();
            }

            COSName cosName = COSName.getPDFName(colorSpace.getName());

            if (allowedColorSpaces.contains(cosName)) {
                valid = true;
            }

            if (disallowedColorSpaces.contains(cosName)) {
                valid = false;
            }

            return valid;
        }
    }
}
