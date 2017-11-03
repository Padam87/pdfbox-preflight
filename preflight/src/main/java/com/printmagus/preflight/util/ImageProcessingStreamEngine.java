package com.printmagus.preflight.util;

import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.util.List;

/**
 * A special stream engine for image processing.
 */
abstract public class ImageProcessingStreamEngine extends PDFStreamEngine
{
    abstract protected void processImage(COSName objectName, PDImageXObject image);

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException
    {
        if (operator.getName().equals("Do")) {
            COSName objectName = (COSName)operands.get(0);

            if (getResources().isImageXObject(objectName)) {
                processImage(objectName, (PDImageXObject) getResources().getXObject(objectName));
            } else {
                PDXObject xobject = getResources().getXObject(objectName);

                if (xobject instanceof PDTransparencyGroup) {
                    showTransparencyGroup((PDTransparencyGroup) xobject);
                } else if (xobject instanceof PDFormXObject) {
                    showForm((PDFormXObject) xobject);
                }
            }
        } else {
            super.processOperator(operator, operands);
        }
    }
}
