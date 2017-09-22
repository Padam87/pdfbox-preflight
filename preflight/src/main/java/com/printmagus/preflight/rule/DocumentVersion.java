package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.HashMap;
import java.util.List;

/**
 * PDF/X-3 is based upon the PDF 1.3 specification from Adobe. This specification
 * explicitly states (see page 56 of the PDF Reference Manual) that valid PDF 1.3 files
 * are those that have in their header PDF 1.3 or any earlier version.
 *
 * Callas technote reference:
 * - PDF must be version 1.3 or earlier [PDF/X-1a] [PDF/X-3]
 */
public class DocumentVersion extends AbstractRule
{
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        if (document.getVersion() > 1.3f) {
            HashMap<String, Object> context = new HashMap<String, Object>();

            context.put("version", document.getVersion());

            Violation violation = new Violation(
                this.getClass().getSimpleName(),
                "PDF must be version 1.3 or earlier",
                null,
                context
            );

            violations.add(violation);
        }
    }
}
