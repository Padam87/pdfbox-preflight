package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionFactory;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;

import java.io.IOException;
import java.util.*;

/**
 * A PDF/X-3 file must not contain Actions or JavaScript.
 *
 * Callas technote reference:
 * - Actions and JavaScript prohibited [PDF/X-1a] [PDF/X-3]
 */
public class NoActions extends AbstractRule
{
    static List<String> ACTIONS = Arrays.asList(
        "GoTo",
        "GoToR",
        "GoToE",
        "Launch",
        "Thread",
        "URI",
        "Sound",
        "Movie",
        "Hide",
        "Named",
        "SubmitForm",
        "ResetForm",
        "ImportData",
        "JavaScript",
        "SetOCGState",
        "Rendition",
        "Trans",
        "GoTo3DView"
    );

    PDDocument document;
    List<Violation> violations;

    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        this.document = document;
        this.violations = violations;

        for (PDPage page: document.getPages()) {
            try {
                for (PDAnnotation annotation: page.getAnnotations()) {
                    if (annotation instanceof PDAnnotationLink) {
                        PDAnnotationLink link = (PDAnnotationLink) annotation;

                        addViolation(link.getAction(), page);
                    }
                }
            } catch (IOException e) {
                // ignore
            }
        }

        for (COSObject cos: document.getDocument().getObjects()) {
            if (cos.getObject() instanceof COSDictionary) {
                COSDictionary dictionary = (COSDictionary) cos.getObject();

                if (isActionDictionary(dictionary)) {
                    addViolation(PDActionFactory.createAction(dictionary), null);
                }
            }
        }

        this.document = null;
        this.violations = null;
    }

    private Boolean isActionDictionary(COSDictionary dictionary)
    {
        if (dictionary.containsKey(COSName.S)) {
            COSName name = (COSName) dictionary.getDictionaryObject(COSName.S);

            if (ACTIONS.contains(name.getName())) {
                return true;
            }
        }

        return false;
    }

    private void addViolation(PDAction action, PDPage page)
    {
        HashMap<String, Object> context = new HashMap<String, Object>();

        context.put("action", action);

        Violation violation = new Violation(
            this.getClass().getSimpleName(),
            "Actions and JavaScript prohibited.",
            page != null ? document.getPages().indexOf(page) : null,
            context
        );

        violations.add(violation);
    }
}
