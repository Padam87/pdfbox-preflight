package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.ArrayList;
import java.util.List;

abstract public class AbstractRule implements RuleInterface
{
    public List<Violation> validate(PDDocument document)
    {
        List<Violation> violations = new ArrayList<>();

        this.doValidate(document, violations);

        return violations;
    }

    protected abstract void doValidate(PDDocument document, List<Violation> violations);
}
