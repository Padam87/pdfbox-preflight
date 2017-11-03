package com.printmagus.preflight.util;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A special stream engine for image processing, with multi-threading.
 */
abstract public class MultiThreadImageProcessingStreamEngine extends ImageProcessingStreamEngine
{
    private ExecutorService executor;

    abstract protected Runnable getWorker(COSName objectName, PDImageXObject image, PDPage page, Matrix ctm);

    @Override
    protected void processImage(COSName objectName, PDImageXObject image)
    {
        executor.submit(getWorker(objectName, image, getCurrentPage(), getGraphicsState().getCurrentTransformationMatrix().clone()));
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
}
