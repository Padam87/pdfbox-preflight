package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        for (COSObject cos: document.getDocument().getObjects()) {
            if (cos.getObject() instanceof COSDictionary) {
                COSDictionary dictionary = (COSDictionary) cos.getObject();
                if (dictionary.containsKey(COSName.S)) {
                    COSName name = (COSName) dictionary.getDictionaryObject(COSName.S);

                    if (ACTIONS.contains(name.getName())) {
                        HashMap<String, Object> context = new HashMap<String, Object>();

                        context.put("action", PDActionFactory.createAction(dictionary));

                        Violation violation = new Violation(
                            this.getClass().getSimpleName(),
                            "Actions and JavaScript prohibited.",
                            null,
                            context
                        );

                        violations.add(violation);
                    }
                }
            }
        }
    }
}
