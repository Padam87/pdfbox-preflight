package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.*;

/**
 * This rule is not a part of any standard, it should be only used on a per-document basis.
 *
 * The page count should be the 1st thing you check before print.
 * Customers sometimes try order a 32 page magazine with a 30 page pdf,
 * later realizing they uploaded the wrong material :)
 *
 * Callas technote reference: -
 */
public class PageCount extends AbstractRule
{
    private final Integer min;
    private final Integer max;

    public PageCount(Integer min, Integer max)
    {
        this.min = min;
        this.max = max;
    }

    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        if (document.getNumberOfPages() < min || document.getNumberOfPages() > max) {
            HashMap<String, Object> context = new HashMap<String, Object>();

            context.put("min", min);
            context.put("max", max);
            context.put("pages", document.getNumberOfPages());

            Violation violation = new Violation(
                this.getClass().getSimpleName(),
                "page_count.must_be_between.%min%.%max%.%pages%",
                null,
                context
            );

            violations.add(violation);
        }
    }
}
