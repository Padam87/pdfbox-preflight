package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.color.*;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.contentstream.operator.text.*;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MaxInkDensityText extends AbstractRuleInterface {
    protected void doValidate(PDDocument document, List<Violation> violations) {
        try {
            InkDensity s = new InkDensity(document, violations);

            for (PDPage page : document.getPages()) {
                s.processPage(page);
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

    static class InkDensity extends PDFStreamEngine
    {
        PDDocument document;
        List<Violation> violations;
        PDColorSpace currentStrokingColorSpace;
        PDColorSpace currentNonStrokingColorSpace;
        StringBuilder currentText = new StringBuilder();

        InkDensity(PDDocument document, List<Violation> violations) throws IOException
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
        protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
                                 Vector displacement) throws IOException
        {
            super.showGlyph(textRenderingMatrix, font, code, unicode, displacement);

            PDGraphicsState state = getGraphicsState();

            processColor(state.getStrokingColor());
            processColor(state.getNonStrokingColor());
        }

        private void processColor(PDColor color) throws IOException {
            Float density = 0f;
            for (Float component : color.toCOSArray().toFloatArray()) {
                density += component * 100;
            }

            if (density > 0) {
                System.out.println(color.toCOSArray());
                System.out.println(density);
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
