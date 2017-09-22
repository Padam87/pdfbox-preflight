package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.List;

/**
 * A PDF file usually has in its trailer section a document ID containing two parts. One
 * part is computed, using a complex algorithm called «MD5», when the document is
 * created, and the second part os computed every time the document has been
 * changed. The MD5 algorithm generates – with a very high probability – values that
 * will be different each time the are computed. Thus, simply by looking at the document
 * ID it can easily be derived whether two files are identical or not. The PDF/X
 * standards require that this document ID be present.
 *
 * Callas technote reference:
 * - Document ID must be present in PDF trailer [PDF/X-1a] [PDF/X-3]
 */
public class DocumentIdExists extends AbstractRule
{
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        if (!document.getDocument().getTrailer().containsKey(COSName.ID)) {
            Violation violation = new Violation(
                this.getClass().getSimpleName(),
                "Document ID must be present in PDF trailer.",
                null
            );

            violations.add(violation);
        }
    }
}
