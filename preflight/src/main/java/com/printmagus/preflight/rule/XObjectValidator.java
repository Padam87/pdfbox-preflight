package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;

import java.util.List;

public interface XObjectValidator
{
    List<Violation> validate(COSName objectName, PDXObject xobject, PDPage page, PDGraphicsState graphicsState);
}
