package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDPostScriptXObject;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * A PDF/X-3 file must not contain embedded PostScript.
 *
 * Callas technote reference:
 * - Embedded PostScript prohibited [PDF/X-1a] [PDF/X-3]
 */
public class NoPostScripts extends AbstractRule
{
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        for (PDPage page: document.getPages()) {
            for (COSName name: page.getResources().getXObjectNames()) {
                try {
                    PDXObject xobject = page.getResources().getXObject(name);

                    if (xobject instanceof PDPostScriptXObject) {
                        HashMap<String, Object> context = new HashMap<String, Object>();

                        context.put("script", xobject);

                        Violation violation = new Violation(
                            this.getClass().getSimpleName(),
                            "no_postscripts.embedded_postscript_not_allowed",
                            document.getPages().indexOf(page),
                            context
                        );

                        violations.add(violation);
                    }
                } catch (IOException e){
                    // ignore
                }
            }
        }
    }
}
