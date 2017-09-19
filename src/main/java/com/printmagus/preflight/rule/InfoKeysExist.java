package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * This rules combines a info dict key existance rules.
 *
 * The Info dictionary keys CreationDate, ModDate and Title in any PDF/X file should
 * contain reasonable values. Formally the only thing that can be checked is whether
 * they are missing or empty.
 *
 * Callas technote reference:
 * - CreationDate, ModDate and Title required [PDF/X-1a] [PDF/X-3]
 * - GTS_PDFXVersion key must be present [PDF/X-1a] [PDF/X-3]
 */
public class InfoKeysExist extends AbstractRule
{
    private List<String> keys;

    public InfoKeysExist(List<String> keys)
    {
        this.keys = keys;
    }

    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        for (String key : keys) {
            if (!document.getDocumentInformation().getCOSObject().containsKey(key)) {
                Violation violation = new Violation(
                    this.getClass().getSimpleName(),
                    String.format("The key '%s' is required, but not found in the info dict.", key),
                    null
                );

                violations.add(violation);
            }
        }
    }
}
