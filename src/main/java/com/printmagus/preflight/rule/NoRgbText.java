package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.color.*;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.contentstream.operator.text.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * The only color space that is not allowed in a PDF/X-3 file is plain RGB (DeviceRGB).
 *
 * Callas technote reference:
 * - Uses DeviceRGB [PDF/X-1a]
 * - Only DeviceCMYK and spot colors allowed [PDF/X-3]
 */
public class NoRgbText extends AbstractRuleInterface
{
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        try {
            PrintTextColors s = new PrintTextColors(document, violations);

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

    static class PrintTextColors extends PDFStreamEngine
    {
        PDDocument document;
        List<Violation> violations;
        PDColorSpace currentStrokingColorSpace;
        PDColorSpace currentNonStrokingColorSpace;
        StringBuilder currentText = new StringBuilder();

        PrintTextColors(PDDocument document, List<Violation> violations) throws IOException
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
            Matrix textRenderingMatrix, PDFont font, int code, String unicode,
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
                || currentNonStrokingColorSpace != state.getNonStrokingColorSpace()) {
                processColorSpaceText();

                currentStrokingColorSpace = state.getStrokingColorSpace();
                currentNonStrokingColorSpace = state.getNonStrokingColorSpace();
                currentText = new StringBuilder();
            }

            currentText.append(unicode);
        }

        private void processColorSpaceText()
        {
            if (this.isRgbColorSpace(currentStrokingColorSpace) || this.isRgbColorSpace(currentNonStrokingColorSpace)) {
                HashMap<String, Object> context = new HashMap<String, Object>();

                context.put("text", currentText);
                context.put("graphicsState", getGraphicsState().clone());

                String message = String.format("RGB text found: %s", currentText);

                Violation violation = new Violation(
                    NoRgbText.class.getSimpleName(),
                    message,
                    document.getPages()
                            .indexOf(getCurrentPage()),
                    context
                );

                violations.add(violation);
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
}
