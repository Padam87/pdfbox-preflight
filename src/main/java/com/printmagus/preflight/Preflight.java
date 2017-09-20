package com.printmagus.preflight;

import com.printmagus.preflight.rule.RuleInterface;
import com.printmagus.preflight.standard.StandardInterface;
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

    public void addRule(RuleInterface rule)
    {
        this.rules.add(rule);
    }

    public void addStandard(StandardInterface standard)
    {
        this.rules.addAll(standard.getRules());
    }
}
