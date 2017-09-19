package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.util.List;

/**
 * The relevant page boxes – namely MediaBox, BleedBox and TrimBox – must be
 * nested properly. The TrimBox must extend neither beyond the BleedBox nor the
 * MediaBox, and the BleedBox must not extend beyond the MediaBox.
 *
 * Callas technote reference:
 * - Page boxes must be nested properly [PDF/X-1a] [PDF/X-3]
 */
public class BoxNesting extends AbstractRuleInterface
{
    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        for (PDPage page: document.getPages()) {
            if (!boxContainsBox(page.getMediaBox(), page.getTrimBox())) {
                Violation violation = new Violation(
                    BoxNesting.class.getSimpleName(),
                    "The TrimBox must not extend beyond the MediaBox.",
                    document.getPages().indexOf(page)
                );

                violations.add(violation);
            }

            if (!boxContainsBox(page.getBleedBox(), page.getTrimBox())) {
                Violation violation = new Violation(
                    BoxNesting.class.getSimpleName(),
                    "The TrimBox must not extend beyond the BleedBox.",
                    document.getPages().indexOf(page)
                );

                violations.add(violation);
            }

            if (!boxContainsBox(page.getMediaBox(), page.getBleedBox())) {
                Violation violation = new Violation(
                    BoxNesting.class.getSimpleName(),
                    "The BleedBox must not extend beyond the MediaBox.",
                    document.getPages().indexOf(page)
                );

                violations.add(violation);
            }
        }
    }

    private Boolean boxContainsBox(PDRectangle outer, PDRectangle inner)
    {
        return outer.contains(inner.getLowerLeftX(), inner.getLowerLeftY())
            && outer.contains(inner.getUpperRightX(), inner.getUpperRightY());
    }
}
