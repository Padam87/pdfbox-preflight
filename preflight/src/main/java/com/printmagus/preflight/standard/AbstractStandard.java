package com.printmagus.preflight.standard;

import com.printmagus.preflight.Violation;
import com.printmagus.preflight.rule.RuleInterface;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.ArrayList;
import java.util.List;

abstract public class AbstractStandard implements StandardInterface
{
    @Override
    public List<Violation> validate(PDDocument document)
    {
        List<Violation> violations = new ArrayList<>();

        for (RuleInterface rule: getRules()) {
            violations.addAll(rule.validate(document));
        }

        return violations;
    }
}
