package com.printmagus.preflight.util;

import com.printmagus.preflight.Violation;
import com.printmagus.preflight.rule.XObjectValidator;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A special stream engine for image processing.
 */
public class PreflightStreamEngine extends PDFStreamEngine
{
    private List<XObjectValidator> xObjectValidators = new ArrayList<>();
    private List<Violation> violations = new ArrayList<>();
    private Integer maxImageArea = Integer.MAX_VALUE;

    public void addValidator(XObjectValidator validator)
    {
        xObjectValidators.add(validator);
    }

    public List<Violation> getViolations()
    {
        return violations;
    }

    private ExecutorService executor;

    public PreflightStreamEngine() {
        addOperator(new Concatenate());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());
    }

    @Override
    public void processPage(PDPage page) throws IOException
    {
        try {
            this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            super.processPage(page);

            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            //
        }
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException
    {
        if (operator.getName().equals("Do")) {
            COSName objectName = (COSName)operands.get(0);

            COSDictionary dict = (COSDictionary) getResources().getCOSObject().getDictionaryObject(COSName.XOBJECT);
            if (dict != null) {
                COSBase object = dict.getDictionaryObject(objectName);

                if (object instanceof COSDictionary) {
                    COSDictionary dict2 = (COSDictionary) object;

                    if (dict2.getCOSName(COSName.SUBTYPE) == COSName.IMAGE) {
                        Integer width = dict2.getInt(COSName.WIDTH);
                        Integer height = dict2.getInt(COSName.HEIGHT);

                        Integer area = width * height;

                        if (area > maxImageArea) {
                            return;
                        }
                    }
                }
            }

            PDXObject xobject = getResources().getXObject(objectName);
            PDPage page = getCurrentPage();
            PDGraphicsState graphicsState = getGraphicsState().clone();

            for (XObjectValidator validator: xObjectValidators) {
                executor.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        List<Violation> ruleV = validator.validate(objectName, xobject, page, graphicsState);

                        if (!ruleV.isEmpty()) {
                            violations.addAll(ruleV);
                        }
                    }
                });
            }

            if (xobject instanceof PDTransparencyGroup) {
                showTransparencyGroup((PDTransparencyGroup) xobject);
            } else if (xobject instanceof PDFormXObject) {
                showForm((PDFormXObject) xobject);
            }
        } else {
            super.processOperator(operator, operands);
        }
    }

    public void setMaxImageArea(Integer maxImageArea)
    {
        this.maxImageArea = maxImageArea;
    }
}
