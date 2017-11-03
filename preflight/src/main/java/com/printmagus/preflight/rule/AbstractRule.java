package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import com.printmagus.preflight.util.PreflightStreamEngine;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.ArrayList;
import java.util.List;

abstract public class AbstractRule implements RuleInterface
{
    protected PreflightStreamEngine streamEngine;

    public List<Violation> validate(PDDocument document)
    {
        List<Violation> violations = new ArrayList<>();

        this.doValidate(document, violations);

        return violations;
    }

    protected abstract void doValidate(PDDocument document, List<Violation> violations);

    @Override
    public void setStreamEngine(PreflightStreamEngine engine)
    {
        streamEngine = engine;
    }
}
