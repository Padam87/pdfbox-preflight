package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
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
                String value = document.getDocumentInformation().getCOSObject().getString(entry.getKey());

                if (value == null || !entry.getValue().matcher(value).matches()) {
                    HashMap<String, Object> context = new HashMap<String, Object>();

                    context.put("value", value);
                    context.put("key", entry.getKey());
                    context.put("pattern", entry.getValue());

                    Violation violation = new Violation(
                        this.getClass().getSimpleName(),
                        String.format("Invalid info dict entry: %s.", entry.getKey()),
                        null,
                        context
                    );

                    violations.add(violation);
                }
            } else {
                Violation violation = new Violation(
                    this.getClass().getSimpleName(),
                    String.format("The key '%s' is required, but not found in the info dict.", entry.getKey()),
                    null
                );

                violations.add(violation);
            }
        }
    }
}
