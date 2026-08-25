package com.magyen.platform.shared.infrastructure.pdf;

import org.junit.jupiter.api.Test;
import org.openpdf.text.Image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MagyenPdfHeaderLogoTest {

    @Test
    void loadsOfficialClasspathLogoWithoutDistortingAspectRatio() {
        Image logo = MagyenPdfHeaderLogo.createImage();
        assertNotNull(logo);
        float originalRatio = logo.getWidth() / logo.getHeight();
        logo.scaleToFit(MagyenPdfHeaderLogo.MAX_SIZE_POINTS, MagyenPdfHeaderLogo.MAX_SIZE_POINTS);
        assertTrue(logo.getScaledHeight() <= MagyenPdfHeaderLogo.MAX_SIZE_POINTS);
        assertTrue(logo.getScaledWidth() <= MagyenPdfHeaderLogo.MAX_SIZE_POINTS);
        assertEquals(originalRatio, logo.getScaledWidth() / logo.getScaledHeight(), 0.001f);
    }
}
