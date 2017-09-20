package com.printmagus.preflight.standard;

import com.printmagus.preflight.rule.*;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class X1a extends AbstractStandard
{
    @Override
    public List<RuleInterface> getRules()
    {
        ArrayList<RuleInterface> rules = new ArrayList<>();

        // PDF must be version 1.3 or earlier
        rules.add(new DocumentVersion());

        // Page must not be separated
        rules.add(new NoSeparation());

        // OutputIntent must be present
        // OutputIntent must contain exactly one PDF/X entry
        // OutputConditionIdentifier required in PDF/X OutputIntent
        // Destination profile must be embedded or Registry Name must be filled out
        // OutputIntent Info key must be present
        // Destination profile must be ICC output profile (type ‘prtr’)
        rules.add(new OutputIntent());

        // Only DeviceCMYK and spot colors allowed
        ArrayList<String> allowedColorSpaces = new ArrayList<>();
        allowedColorSpaces.add(PDDeviceCMYK.class.getName());
        allowedColorSpaces.add(PDDeviceGray.class.getName());
        allowedColorSpaces.add(PDDeviceN.class.getName());
        allowedColorSpaces.add(PDSeparation.class.getName());

        rules.add(new ColorSpaceText(allowedColorSpaces));
        rules.add(new ColorSpaceImages(allowedColorSpaces));

        // Fonts must be embedded
        rules.add(new OnlyEmbeddedFonts());

        // LZW compression prohibited
        // @TODO

        // Trapped key must be True or False
        // GTS_PDFXVersion key must be present
        // Invalid GTS_PDFXVersion (PDF/X-1a)
        // Invalid GTS_PDFXConformance (PDF/X-1a)
        // CreationDate, ModDate and Title required
        ArrayList<String> keysExist = new ArrayList<>();
        keysExist.add(COSName.TITLE.getName());
        keysExist.add(COSName.CREATION_DATE.getName());
        keysExist.add(COSName.MOD_DATE.getName());

        rules.add(new InfoKeysExist(keysExist));

        HashMap<String, Pattern> keysMatch = new HashMap<>();
        keysMatch.put("GTS_PDFXVersion", Pattern.compile("PDF/X-1:2001"));
        keysMatch.put("GTS_PDFXConformance", Pattern.compile("PDF/X-1a:2001"));
        keysMatch.put(COSName.TRAPPED.getName(), Pattern.compile("True|False"));

        rules.add(new InfoKeysMatch(keysMatch));

        // Document ID must be present in PDF trailer
        rules.add(new DocumentIdExists());

        // Either TrimBox or ArtBox must be present
        rules.add(new BoxExists());

        // Page boxes must be nested properly
        rules.add(new BoxNesting());

        // Transfer curves prohibited
        rules.add(new NoTransferCurves());

        // Halftone must be of Type 1 or 5
        // Halftone Name key prohibited
        rules.add(new AllowedHalftoneTypes(Arrays.asList(1, 5)));

        // Embedded PostScript prohibited
        rules.add(new NoPostScripts());

        // Encryption prohibited
        // @TODO

        // Alternate image must not be default for printing
        // @TODO

        // Annotation and Acrobat form elements must be outside of TrimBox and BleedBox
        rules.add(new NoAnnotationsInsidePageArea());
        rules.add(new NoFormsInsidePageArea());

        // Actions and JavaScript prohibited
        rules.add(new NoActions());

        // Operators not defined in PDF 1.3 prohibited
        // @TODO

        // File specifications not allowed
        // @TODO

        // Transparency not allowed
        rules.add(new NoTransparency());

        return rules;
    }
}
