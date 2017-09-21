package com.printmagus.preflight.util;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

/**
 * Copied over code from SampledImageReader, modified to just return a Raster instead of BufferedImage
 *
 * @see org.apache.pdfbox.pdmodel.graphics.image.SampledImageReader
 */
final public class SampledRasterReader
{
    private SampledRasterReader()
    {
    }

    /**
     * Returns the content of the given image as an AWT buffered image with an RGB color space.
     * If a color key mask is provided then an ARGB image is returned instead.
     * This method never returns null.
     *
     * @param pdImage  the image to read
     * @param colorKey an optional color key mask
     * @return content of this image as an RGB buffered image
     * @throws IOException if the image cannot be read
     */
    public static Raster getRaster(PDImage pdImage, COSArray colorKey) throws IOException
    {
        if (pdImage.isEmpty()) {
            throw new IOException("Image stream is empty");
        }

        // get parameters, they must be valid or have been repaired
        final PDColorSpace colorSpace = pdImage.getColorSpace();
        final int numComponents = colorSpace.getNumberOfComponents();
        final int width = pdImage.getWidth();
        final int height = pdImage.getHeight();
        final int bitsPerComponent = pdImage.getBitsPerComponent();
        final float[] decode = getDecodeArray(pdImage);

        if (width <= 0 || height <= 0) {
            throw new IOException("image weight and height must be positive");
        }

        //
        // An AWT raster must use 8/16/32 bits per component. Images with < 8bpc
        // will be unpacked into a byte-backed raster. Images with 16bpc will be reduced
        // in depth to 8bpc as they will be drawn to TYPE_INT_RGB images anyway. All code
        // in PDColorSpace#toRGBImage expects and 8-bit range, i.e. 0-255.
        //
        WritableRaster raster = Raster.createBandedRaster(
            DataBuffer.TYPE_BYTE,
            width,
            height,
            numComponents,
            new Point(0, 0)
        );

        // convert image, faster path for non-decoded, non-colormasked 8-bit images
        final float[] defaultDecode = pdImage.getColorSpace().getDefaultDecode(8);
        if (bitsPerComponent == 8 && Arrays.equals(decode, defaultDecode) && colorKey == null) {
            from8bit(pdImage, raster);
        } else {
            if (bitsPerComponent == 1 && colorKey == null) {
                from1Bit(pdImage, raster);
            } else {
                fromAny(pdImage, raster, colorKey);
            }
        }

        return raster;
    }

    private static void from1Bit(PDImage pdImage, WritableRaster raster)
        throws IOException
    {
        final PDColorSpace colorSpace = pdImage.getColorSpace();
        final int width = pdImage.getWidth();
        final int height = pdImage.getHeight();
        final float[] decode = getDecodeArray(pdImage);
        byte[] output = ((DataBufferByte)raster.getDataBuffer()).getData();

        // read bit stream
        InputStream iis = null;
        try {
            // create stream
            iis = pdImage.createInputStream();
            final boolean isIndexed = colorSpace instanceof PDIndexed;

            int rowLen = width / 8;
            if (width % 8 > 0) {
                rowLen++;
            }

            // read stream
            byte value0;
            byte value1;
            if (isIndexed || decode[0] < decode[1]) {
                value0 = 0;
                value1 = (byte)255;
            } else {
                value0 = (byte)255;
                value1 = 0;
            }
            byte[] buff = new byte[rowLen];
            int idx = 0;
            for (int y = 0; y < height; y++) {
                int x = 0;
                int readLen = iis.read(buff);
                for (int r = 0; r < rowLen && r < readLen; r++) {
                    int value = buff[r];
                    int mask = 128;
                    for (int i = 0; i < 8; i++) {
                        int bit = value & mask;
                        mask >>= 1;
                        output[idx++] = bit == 0 ? value0 : value1;
                        x++;
                        if (x == width) {
                            break;
                        }
                    }
                }
                if (readLen != rowLen) {
                    break;
                }
            }
        } finally {
            if (iis != null) {
                iis.close();
            }
        }
    }

    // faster, 8-bit non-decoded, non-colormasked image conversion
    private static void from8bit(PDImage pdImage, WritableRaster raster)
        throws IOException
    {
        InputStream input = pdImage.createInputStream();
        try {
            // get the raster's underlying byte buffer
            byte[][] banks = ((DataBufferByte)raster.getDataBuffer()).getBankData();
            final int width = pdImage.getWidth();
            final int height = pdImage.getHeight();
            final int numComponents = pdImage.getColorSpace()
                                             .getNumberOfComponents();
            int max = width * height;
            byte[] tempBytes = new byte[numComponents];
            for (int i = 0; i < max; i++) {
                input.read(tempBytes);
                for (int c = 0; c < numComponents; c++) {
                    banks[c][i] = tempBytes[0 + c];
                }
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    // slower, general-purpose image conversion from any image format
    private static void fromAny(PDImage pdImage, WritableRaster raster, COSArray colorKey)
        throws IOException
    {
        final PDColorSpace colorSpace = pdImage.getColorSpace();
        final int numComponents = colorSpace.getNumberOfComponents();
        final int width = pdImage.getWidth();
        final int height = pdImage.getHeight();
        final int bitsPerComponent = pdImage.getBitsPerComponent();
        final float[] decode = getDecodeArray(pdImage);

        // read bit stream
        ImageInputStream iis = null;
        try {
            // create stream
            iis = new MemoryCacheImageInputStream(pdImage.createInputStream());
            final float sampleMax = (float)Math.pow(2, bitsPerComponent) - 1f;
            final boolean isIndexed = colorSpace instanceof PDIndexed;

            // init color key mask
            float[] colorKeyRanges = null;
            BufferedImage colorKeyMask = null;
            if (colorKey != null) {
                colorKeyRanges = colorKey.toFloatArray();
                colorKeyMask = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            }

            // calculate row padding
            int padding = 0;
            if (width * numComponents * bitsPerComponent % 8 > 0) {
                padding = 8 - (width * numComponents * bitsPerComponent % 8);
            }

            // read stream
            byte[] srcColorValues = new byte[numComponents];
            byte[] alpha = new byte[1];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    boolean isMasked = true;
                    for (int c = 0; c < numComponents; c++) {
                        int value = (int)iis.readBits(bitsPerComponent);

                        // color key mask requires values before they are decoded
                        if (colorKeyRanges != null) {
                            isMasked &= value >= colorKeyRanges[c * 2] &&
                                value <= colorKeyRanges[c * 2 + 1];
                        }

                        // decode array
                        final float dMin = decode[c * 2];
                        final float dMax = decode[(c * 2) + 1];

                        // interpolate to domain
                        float output = dMin + (value * ((dMax - dMin) / sampleMax));

                        if (isIndexed) {
                            // indexed color spaces get the raw value, because the TYPE_BYTE
                            // below cannot be reversed by the color space without it having
                            // knowledge of the number of bits per component
                            srcColorValues[c] = (byte)Math.round(output);
                        } else {
                            // interpolate to TYPE_BYTE
                            int outputByte = Math.round(((output - Math.min(dMin, dMax)) /
                                Math.abs(dMax - dMin)) * 255f);

                            srcColorValues[c] = (byte)outputByte;
                        }
                    }
                    raster.setDataElements(x, y, srcColorValues);

                    // set alpha channel in color key mask, if any
                    if (colorKeyMask != null) {
                        alpha[0] = (byte)(isMasked ? 255 : 0);
                        colorKeyMask.getRaster()
                                    .setDataElements(x, y, alpha);
                    }
                }

                // rows are padded to the nearest byte
                iis.readBits(padding);
            }
        } finally {
            if (iis != null) {
                iis.close();
            }
        }
    }

    // gets decode array from dictionary or returns default
    private static float[] getDecodeArray(PDImage pdImage) throws IOException
    {
        final COSArray cosDecode = pdImage.getDecode();
        float[] decode = null;

        if (cosDecode != null) {
            int numberOfComponents = pdImage.getColorSpace().getNumberOfComponents();
            if (cosDecode.size() != numberOfComponents * 2) {
                if (pdImage.isStencil() && cosDecode.size() >= 2
                    && cosDecode.get(0) instanceof COSNumber
                    && cosDecode.get(1) instanceof COSNumber) {
                    float decode0 = ((COSNumber)cosDecode.get(0)).floatValue();
                    float decode1 = ((COSNumber)cosDecode.get(1)).floatValue();
                    if (decode0 >= 0 && decode0 <= 1 && decode1 >= 0 && decode1 <= 1) {
                        return new float[] { decode0, decode1 };
                    }
                }
            } else {
                decode = cosDecode.toFloatArray();
            }
        }

        // use color space default
        if (decode == null) {
            return pdImage.getColorSpace().getDefaultDecode(pdImage.getBitsPerComponent());
        }

        return decode;
    }
}
