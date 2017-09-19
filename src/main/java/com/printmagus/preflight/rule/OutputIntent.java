package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;

import java.util.HashMap;
import java.util.List;

/**
 * (1) A PDF/X-3 file must have an OutputIntent array. This array is supposed to contain a
 * PDF/X-3 specific OutputIntent. This PDF/X-3 OutputIntent indicates, for which printing
 * condition the PDF/X-3 file has been prepared.
 *
 * (2) While an OutputIntent array may contain more than one entry, exactly one of its
 * entries must have the Subtype «GTS_PDFX».
 *
 * (3) The PDF/X OutputIntent entry must have an OutputConditionIdentifier. This Output-
 * ConditionIdentifier can be the value of a registered printing condition – in this case
 * it is necessary to indicate the registry in the OutputIntent key «RegistryName» or
 * another entry identifying the intended printing condition such that the recipient will
 * recognize it.
 *
 * (4) It is required for a PDF/X-1a or PDF/X-3 file that either the RegistryName be present
 * or an ICC output profile that characterizes the intended printing condition is embedded
 * in the OutputIntent. Also, if colors other than the process colors of the printing
 * process of the intended printing condition
 *
 * (5) The Info key in a PDF/X OutputIntent is required. It should contain descriptive information
 * about the intended printing condition.
 *
 * (6) The ICC profile embedded as a destination profile into a PDF/X-3 OutputIntent must
 * be an output profile (type ‘prtr’).
 *
 * Callas technote reference:
 * - (1) OutputIntent must be present [PDF/X-1a] [PDF/X-3]
 * - (2) OutputIntent must contain exactly one PDF/X entry [PDF/X-1a] [PDF/X-3]
 * - (3) OutputConditionIdentifier required in PDF/X OutputIntent [PDF/X-1a] [PDF/X-3]
 * - (4) Destination profile must be embedded or Registry Name must be filled out [PDF/X-1a] [PDF/X-3]
 * - (5) OutputIntent Info key must be present [PDF/X-1a] [PDF/X-3]
 * - (6) Destination profile must be ICC output profile (type ‘prtr’) [PDF/X-1a] [PDF/X-3]
 */
public class OutputIntent extends AbstractRule
{
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        if (document.getDocumentCatalog().getOutputIntents().isEmpty()) {
            // (1)
            Violation violation = new Violation(
                this.getClass().getSimpleName(),
                "OutputIntent must be present.",
                null
            );

            violations.add(violation);
        }

        Integer pdfx = 0;
        for (PDOutputIntent outputIntent: document.getDocumentCatalog().getOutputIntents()) {
            COSDictionary dictionary = (COSDictionary) outputIntent.getCOSObject();
            COSName subtype = (COSName) dictionary.getDictionaryObject(COSName.S);

            if (subtype.getName().equals("GTS_PDFX")) {
                pdfx++;

                String outputConditionIdentifier = outputIntent.getOutputConditionIdentifier();

                if (outputConditionIdentifier == null || outputConditionIdentifier.isEmpty()) {
                    // (3)
                    Violation violation = new Violation(
                        this.getClass().getSimpleName(),
                        "OutputConditionIdentifier required in PDF/X OutputIntent.",
                        null
                    );

                    violations.add(violation);
                }

                if (!dictionary.containsKey(COSName.REGISTRY_NAME) && !dictionary.containsKey(COSName.DEST_OUTPUT_PROFILE)) {
                    // (4)
                    Violation violation = new Violation(
                        this.getClass().getSimpleName(),
                        "Destination profile must be embedded or Registry Name must be filled out.",
                        null
                    );

                    violations.add(violation);
                }

                if (!dictionary.containsKey(COSName.INFO)) {
                    // (5)
                    Violation violation = new Violation(
                        this.getClass().getSimpleName(),
                        "OutputIntent Info key must be present.",
                        null
                    );

                    violations.add(violation);
                }

                COSStream outputProfile = (COSStream) dictionary.getDictionaryObject(COSName.DEST_OUTPUT_PROFILE);

                if (!outputProfile.toTextString().substring(12, 16).equals("prtr")) {
                    Violation violation = new Violation(
                        this.getClass().getSimpleName(),
                        "Destination profile must be ICC output profile (type ‘prtr’).",
                        null
                    );

                    violations.add(violation);
                }
            }
        }

        if (pdfx != 1) {
            HashMap<String, Object> context = new HashMap<String, Object>();

            context.put("count", pdfx);

            // (2)
            Violation violation = new Violation(
                this.getClass().getSimpleName(),
                "OutputIntent must contain exactly one PDF/X entry.",
                null,
                context
            );

            violations.add(violation);
        }
    }
}
