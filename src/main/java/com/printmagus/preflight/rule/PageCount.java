package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.*;

public class PageCount extends AbstractRuleInterface {
    private final Integer min;
    private final Integer max;

    public PageCount(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }

    protected void doValidate(PDDocument document, List<Violation> violations) {
        if (document.getNumberOfPages() < min || document.getNumberOfPages() > max) {
            String message = String.format(
                    "The page count must be between %d and %d. This document has %d pages.",
                    min, max, document.getNumberOfPages()
            );

            violations.add(new Violation(this.getClass().getSimpleName(), message, null));
        }
    }
}
