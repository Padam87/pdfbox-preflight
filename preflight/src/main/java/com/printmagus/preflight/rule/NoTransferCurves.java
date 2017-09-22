package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.util.List;

/**
 * Transfer curves are not allowed in a PDF/X-1a or PDF/X-3 file.
 *
 * Callas technote reference:
 * - Transfer curves prohibited [PDF/X-1a] [PDF/X-3]
 */
public class NoTransferCurves extends AbstractRule
{
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        for (PDPage page : document.getPages()) {
            for (COSName gsName : page.getResources().getExtGStateNames()) {
                PDExtendedGraphicsState extendedGraphicsState = page.getResources().getExtGState(gsName);

                if (extendedGraphicsState.getTransfer() != null || extendedGraphicsState.getTransfer2() != null) {
                    Violation violation = new Violation(
                        this.getClass().getSimpleName(),
                        "Transfer curves prohibited",
                        null
                    );

                    violations.add(violation);
                }
            }
        }
    }
}
