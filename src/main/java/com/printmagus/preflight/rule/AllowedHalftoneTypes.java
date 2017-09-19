package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.io.IOException;
import java.util.List;

/**
 * Halftone dictionaries in a PDF/X-3 file must be of Type 1 or Type 5.
 *
 * Callas technote reference:
 * - Halftone must be of Type 1 or 5 [PDF/X-1a] [PDF/X-3]
 */
public class AllowedHalftoneTypes extends AbstractRule
{
    private List<Integer> types;

    public AllowedHalftoneTypes(List<Integer> types)
    {
        this.types = types;
    }

    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        try {
            List<COSObject> halftones = document.getDocument().getObjectsByType("Halftone");

            // @TODO: This should be the way to go, but I need a sample document
            // System.out.println(halftones);
        } catch (IOException e){
            // ignore
        }
    }
}
