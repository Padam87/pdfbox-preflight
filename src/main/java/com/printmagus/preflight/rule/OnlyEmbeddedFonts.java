package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * All fonts must be embedded in a PDF/X-1a or PDF/X-3 file.
 *
 * Callas technote reference:
 * - Fonts must be embedded [PDF/X-1a] [PDF/X-3]
 */
public class OnlyEmbeddedFonts extends AbstractRule
{
    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        for (PDPage page: document.getPages()) {
            for (COSName fontName : page.getResources().getFontNames()) {
                try {
                    PDFont font = page.getResources().getFont(fontName);

                    if (!font.isEmbedded()) {
                        HashMap<String, Object> context = new HashMap<String, Object>();

                        context.put("font", font);

                        Violation violation = new Violation(
                            this.getClass().getSimpleName(),
                            "Fonts must be embedded.",
                            document.getPages().indexOf(page),
                            context
                        );
                    }
                } catch (IOException e) {
                    // just ignore it
                }
            }
        }
    }
}
