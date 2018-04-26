package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This rules combines a info dict key pattern match rules.
 *
 * Callas technote reference:
 * - GTS_PDFXVersion key must be present [PDF/X-1a] [PDF/X-3]
 * - Invalid GTS_PDFXVersion [PDF/X-1a]
 * - Invalid GTS_PDFXVersion [PDF/X-3]
 * - Invalid GTS_PDFXConformance [PDF/X-1a]
 * - Trapped key must be present [PDF/X-1a] [PDF/X-3]
 */
public class InfoKeysMatch extends AbstractRule
{
    private HashMap<String, Pattern> keys;

    public InfoKeysMatch(HashMap<String, Pattern> keys)
    {
        this.keys = keys;
    }

    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        for (Map.Entry<String, Pattern> entry: keys.entrySet()) {
            if (document.getDocumentInformation().getCOSObject().containsKey(entry.getKey())) {
                COSBase cos = document.getDocumentInformation().getCOSObject().getDictionaryObject(entry.getKey());
                String value = null;

                if (cos instanceof COSString) {
                    value = ((COSString) cos).getString();
                } else if (cos instanceof COSName) {
                    value = ((COSName) cos).getName();
                }

                if (value == null || !entry.getValue().matcher(value).matches()) {
                    HashMap<String, Object> context = new HashMap<String, Object>();

                    context.put("value", value);
                    context.put("key", entry.getKey());
                    context.put("pattern", entry.getValue());

                    Violation violation = new Violation(
                        this.getClass().getSimpleName(),
                        "info_key_match.mismatch.%key%.%pattern%.%value%",
                        null,
                        context
                    );

                    violations.add(violation);
                }
            } else {
                HashMap<String, Object> context = new HashMap<String, Object>();

                context.put("key", entry.getKey());

                Violation violation = new Violation(
                    this.getClass().getSimpleName(),
                    "info_key_match.missing.%key%",
                    null,
                    context
                );

                violations.add(violation);
            }
        }
    }
}
