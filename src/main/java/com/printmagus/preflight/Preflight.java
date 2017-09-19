package com.printmagus.preflight;

import com.printmagus.preflight.rule.RuleInterface;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.ArrayList;
import java.util.List;

public class Preflight implements PreflightInterface
{
    private List<RuleInterface> rules = new ArrayList<>();

    public List<Violation> validate(PDDocument document)
    {
        List<Violation> violations = new ArrayList<>();

        this.rules.forEach((ruleInterface) -> {
            violations.addAll(ruleInterface.validate(document));
        });

        return violations;
    }

    public List<RuleInterface> getRules()
    {
        return rules;
    }

    public void addRule(RuleInterface ruleInterface)
    {
        this.rules.add(ruleInterface);
    }
}
