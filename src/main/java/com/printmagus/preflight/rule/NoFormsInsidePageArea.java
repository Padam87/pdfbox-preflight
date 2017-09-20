package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * While annotations and form elements may be present in a PDF/X-3 file they must
 * reside completely outside of the TrimBox and BleedBox.
 *
 * Callas technote reference:
 * - Annotation and Acrobat form elements must be outside of TrimBox and BleedBox [PDF/X-1a] [PDF/X-3]
 */
public class NoFormsInsidePageArea extends AbstractRule
{
    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        for (PDPage page: document.getPages()) {
            try {
                for (COSName name: page.getResources().getXObjectNames()) {
                    PDXObject xObject = page.getResources().getXObject(name);

                    if (xObject instanceof PDFormXObject) {
                        PDFormXObject form = (PDFormXObject) xObject;

                        if (boxesOverlap(page.getTrimBox(), form.getBBox())
                            || boxesOverlap(page.getBleedBox(), form.getBBox())
                        ) {
                            HashMap<String, Object> context = new HashMap<String, Object>();

                            context.put("form", form);
                            context.put("rectangle", form.getBBox());
                            context.put("bleedBox", page.getBleedBox());
                            context.put("trimBox", page.getTrimBox());

                            Violation violation = new Violation(
                                this.getClass().getSimpleName(),
                                "Form elements must be outside of TrimBox and BleedBox.",
                                document.getPages().indexOf(page),
                                context
                            );

                            violations.add(violation);
                        }
                    }
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private Boolean boxesOverlap(PDRectangle outer, PDRectangle inner)
    {
        Float llx = inner.getLowerLeftX();
        Float lly = inner.getLowerLeftY();
        Float urx = inner.getUpperRightX();
        Float ury = inner.getUpperRightY();

        return outer.contains(llx, lly)
            || outer.contains(urx, lly)
            || outer.contains(urx, ury)
            || outer.contains(llx, ury)
        ;
    }
}
