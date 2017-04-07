/*
 * Copyright 2015 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.ui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used to generate a text image, which is basically a colored
 * image where the color is determined based on the provided text and with the
 * first letter of the provided text on it. It can be used e.g. as placeholder
 * in a profile if no user image is available.
 *
 * @author mf6319
 */
public final class TextImage {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextImage.class);

    /**
     * The text used to create this text image.
     */
    private String text = "default";
    /**
     * Color generator using a pre-defined list of colors.
     */
    private ColorGenerator colorGen = ColorGenerator.MATERIAL;
    /**
     * Default color.
     */
    private Color color = new Color(colorGen.getColor(text));
    /**
     * Default image size.
     */
    private int size = 80;

    /**
     * Protected constructor.
     *
     * @param pText The test the image should represent.
     */
    protected TextImage(String pText) {
        if (pText != null) {
            text = pText;
            color = new Color(colorGen.getColor(text));
        }
    }

    /**
     * Create a new TextImage instance using the provided string.
     *
     * @param pText The text the image should represent.
     *
     * @return The text image instance.
     */
    public static TextImage from(String pText) {
        return new TextImage(pText);
    }

    /**
     * Create a new TextImage instance using the provided ColorGenerator.
     *
     * @param pColorGen The custom ColorGenerator.
     *
     * @return The text image instance.
     */
    public TextImage withColorGenerator(ColorGenerator pColorGen) {
        colorGen = pColorGen;
        color = new Color(colorGen.getColor(text));
        return this;
    }

    /**
     * Crate a new TextImage instance using the provided image size.
     *
     * @param pSize The custom image size.
     *
     * @return The text image instance.
     */
    public TextImage withSize(int pSize) {
        size = pSize;
        return this;
    }

    /**
     * Get the bytes of the final image.
     *
     * @return The byte array containing the bytes of the resulting image.
     *
     * @throws IOException if creating the image fails.
     */
    public byte[] getBytes() throws IOException {
        Image transparentImage = Toolkit.getDefaultToolkit().createImage(
                new FilteredImageSource(
                        new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB).getSource(),
                        new RGBImageFilter() {
                    @Override
                    public final int filterRGB(int x, int y, int rgb) {
                        return (rgb << 8) & 0xFF000000;
                    }
                }));

        //create the actual image and overlay it by the transparent background
        BufferedImage outputImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(transparentImage, 0, 0, null);
        //draw the remaining stuff
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setColor(color);
        g2d.fillRoundRect(0, 0, size, size, 20, 20);
        g2d.setColor(new Color(Math.round((float) color.getRed() * .9f), Math.round((float) color.getGreen() * .9f), Math.round((float) color.getBlue() * .9f)));
        g2d.drawRoundRect(0, 0, size - 1, size - 1, 20, 20);

        Font font = new Font("Dialog", Font.BOLD, size - 4);
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);

        String s = text.toUpperCase().substring(0, 1);
        FontMetrics fm = g2d.getFontMetrics();
        float x = ((float) size - (float) fm.stringWidth(s)) / 2f;
        float y = ((float) fm.getAscent() + (float) ((float) size - ((float) fm.getAscent() + (float) fm.getDescent())) / 2f) - 1f;
        g2d.drawString(s, x, y);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ImageIO.write(outputImage, "png", bout);
        g2d.dispose();
        return bout.toByteArray();
    }

    /**
     * Write the resulting image to the provided file.
     *
     * @param pFile The output file.
     *
     * @return The text image instance.
     *
     * @throws IOException if creating the image fails.
     */
    public TextImage writeToFile(File pFile) throws IOException {
        String ext = FilenameUtils.getExtension(pFile.getName());
        if (!ext.equals("png")) {
            LOGGER.warn("Expecting file extension 'png' but extension is '{}'. Output image might not be readable by all tools.", ext);
        }
        try (FileOutputStream fout = new FileOutputStream(pFile)) {
            fout.write(getBytes());
            fout.flush();
        }
        return this;
    }
}
