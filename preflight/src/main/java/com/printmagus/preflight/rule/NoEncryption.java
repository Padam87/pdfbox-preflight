package com.printmagus.preflight.rule;

import com.printmagus.preflight.Violation;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.List;

/**
 * A PDF/X-3 must not be encrypted. Encryption is used, if access to a PDF file is password
 * protected. Even if no password is needed for opening or printing a PDF file
 * password protection for modifying the file is prohibited.
 *
 * Callas technote reference:
 * - Encryption prohibited [PDF/X-1a] [PDF/X-3]
 */
public class NoEncryption extends AbstractRule
{
    @Override
    protected void doValidate(PDDocument document, List<Violation> violations)
    {
        if (document.isEncrypted()) {
            Violation violation = new Violation(
                this.getClass().getSimpleName(),
                "no_encryption.no_encryption_allowed",
                null
            );

            violations.add(violation);
        }
    }
}
