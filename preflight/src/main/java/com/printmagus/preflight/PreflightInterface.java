package com.printmagus.preflight;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.List;

public interface PreflightInterface
{
    List<Violation> validate(PDDocument document);
}
