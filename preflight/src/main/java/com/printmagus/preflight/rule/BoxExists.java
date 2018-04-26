package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.util.List;

/**
 * A PDF/X file must contain either a TrimBox or an ArtBox for every page in the PDF.
 * While both TrimBox or ArtBox may be used, the PDF/X-3 standard recommends to
 * prefer the TrimBox.
 *
 * Callas technote reference:
 * - Either TrimBox or ArtBox must be present [PDF/X-1a] [PDF/X-3]
 */
public class BoxExists extends AbstractRule
{
    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        for (PDPage page: document.getPages()) {
            COSArray trimBox = (COSArray) page.getCOSObject().getDictionaryObject(COSName.TRIM_BOX);
            COSArray artBox = (COSArray) page.getCOSObject().getDictionaryObject(COSName.ART_BOX);

            if (trimBox == null && artBox == null) {
                Violation violation = new Violation(
                    this.getClass().getSimpleName(),
                    "box_exists.trim_box_or_art_box_must_be_present",
                    document.getPages().indexOf(page)
                );

                violations.add(violation);
            } else if (trimBox != null && artBox != null) {
                Violation violation = new Violation(
                    this.getClass().getSimpleName(),
                    "box_exists.trim_box_or_art_box_must_be_present_but_not_both",
                    document.getPages().indexOf(page)
                );

                violations.add(violation);
            }
        }
    }
}
