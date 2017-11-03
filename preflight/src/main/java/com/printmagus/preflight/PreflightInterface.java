package com.printmagus.preflight;

import com.printmagus.preflight.util.PreflightStreamEngine;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.List;

public interface PreflightInterface
{
    List<Violation> validate(PDDocument document);

    void setStreamEngine(PreflightStreamEngine engine);
}
