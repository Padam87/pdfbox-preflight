package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.util.HashMap;
import java.util.List;

/**
 * Box size must be the same as the order specifies.
 * Not a part of any standard.
 *
 * Callas technote reference: -
 */
public class BoxSize extends AbstractRule
{
    private COSName box;
    private final float width;
    private final float height;
    private int decimals;

    public BoxSize(COSName box, float width, float height, int roundTo)
    {
        this.box = box;
        this.width = width;
        this.height = height;
        this.decimals = roundTo;
    }

    public BoxSize(COSName box, float width, float height)
    {
        this.box = box;
        this.width = width;
        this.height = height;
        this.decimals = 0;
    }

    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        for (PDPage page: document.getPages()) {
            if (getBoxWidth(page) != round(width) || getBoxHeight(page) != round(height)) {
                HashMap<String, Object> context = new HashMap<String, Object>();

                context.put("width", getBoxWidth(page));
                context.put("height", getBoxHeight(page));

                Violation violation = new Violation(
                    BoxSize.class.getSimpleName(),
                    String.format("The %s must be exactly %f x %f mm-s.", box.getName(), round(width), round(height)),
                    document.getPages().indexOf(page),
                    context
                );

                violations.add(violation);
            }
        }
    }

    private double round(float number)
    {
        return Math.round(number * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }

    private double getBoxWidth(PDPage page)
    {
        PDRectangle rectangle = getBox(page);

        return round((rectangle.getUpperRightX() - rectangle.getLowerLeftX()) * 25.4f / 72);
    }

    private double getBoxHeight(PDPage page)
    {
        PDRectangle rectangle = getBox(page);

        return round((rectangle.getUpperRightY() - rectangle.getLowerLeftY()) * 25.4f / 72);
    }

    private PDRectangle getBox(PDPage page)
    {
        if (box.getName().equals(COSName.ART_BOX.getName())) {
            return page.getArtBox();
        }

        if (box.getName().equals(COSName.BLEED_BOX.getName())) {
            return page.getBleedBox();
        }

        if (box.getName().equals(COSName.CROP_BOX.getName())) {
            return page.getCropBox();
        }

        if (box.getName().equals(COSName.MEDIA_BOX.getName())) {
            return page.getMediaBox();
        }

        if (box.getName().equals(COSName.TRIM_BOX.getName())) {
            return page.getTrimBox();
        }

        return null;
    }
}
