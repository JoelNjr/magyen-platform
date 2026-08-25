package com.magyen.platform.production.application;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class ProductionReferenceImageFixtures {

    private ProductionReferenceImageFixtures() {
    }

    public static byte[] jpegBytes() throws IOException {
        return imageBytes("jpeg");
    }

    public static byte[] pngBytes() throws IOException {
        return imageBytes("png");
    }

    private static byte[] imageBytes(String format) throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
    }
}
