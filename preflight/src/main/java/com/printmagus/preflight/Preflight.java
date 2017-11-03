package com.printmagus.preflight;

import com.printmagus.preflight.rule.RuleInterface;
import com.printmagus.preflight.standard.StandardInterface;
import com.printmagus.preflight.util.PreflightStreamEngine;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDPrintFieldAttributeObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Preflight implements PreflightInterface
{
    private List<RuleInterface> rules = new ArrayList<>();
    private PreflightStreamEngine streamEngine;

    public List<Violation> validate(PDDocument document)
    {
        if (streamEngine == null) {
            streamEngine = new PreflightStreamEngine();
        }

        List<Violation> violations = new ArrayList<>();

        for (RuleInterface rule: rules) {
            rule.setStreamEngine(streamEngine);

            violations.addAll(rule.validate(document));
        }

        for (PDPage page: document.getPages()) {
            try {
                streamEngine.processPage(page);
            } catch (IOException e) {
                //
            }
        }

        violations.addAll(streamEngine.getViolations());

        return violations;
    }

    @Override
    public void setStreamEngine(PreflightStreamEngine engine)
    {
        streamEngine = engine;
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
