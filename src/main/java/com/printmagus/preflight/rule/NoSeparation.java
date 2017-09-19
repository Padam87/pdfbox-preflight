package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.util.List;

/**
 * PDF/X-3 only allows non separated pages in the PDF file. If a page has a SeparationInfo
 * dictionary this is a clear indicator that the page is a pre-separated page.
 *
 * Callas technote reference:
 * - Page must not be separated [PDF/X-1a] [PDF/X-3]
 */
public class NoSeparation extends AbstractRule
{
    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        for (PDPage page: document.getPages()) {
            if (page.getCOSObject().containsKey("SeparationInfo")) {
                Violation violation = new Violation(
                    this.getClass().getSimpleName(),
                    "Page must not be separated.",
                    document.getPages().indexOf(page)
                );

                violations.add(violation);
            }
        }
    }
}
