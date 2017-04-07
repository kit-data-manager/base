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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author mf6319
 */
public class ColorGenerator {

    public static ColorGenerator DEFAULT;

    public static ColorGenerator MATERIAL;

    static {
        DEFAULT = create(Arrays.asList(
                0xfff16364,
                0xfff58559,
                0xfff9a43e,
                0xffe4c62e,
                0xff67bf74,
                0xff59a2be,
                0xff2093cd,
                0xffad62a7,
                0xff805781
        ));
        MATERIAL = create(Arrays.asList(
                0xffe57373,
                0xfff06292,
                0xffba68c8,
                0xff9575cd,
                0xff7986cb,
                0xff64b5f6,
                0xff4fc3f7,
                0xff4dd0e1,
                0xff4db6ac,
                0xff81c784,
                0xffaed581,
                0xffff8a65,
                0xffd4e157,
                0xffffd54f,
                0xffffb74d,
                0xffa1887f,
                0xff90a4ae
        ));
    }

    /**
     * Color list.
     */
    private final List<Integer> mColors;
    /**
     * Randomizer.
     */
    private final Random mRandom;

    /**
     * Create a new color generator instance supporting the provided list of
     * colors.
     *
     * @param colorList The list of supported colors.
     *
     * @return A new ColorGenerator instance.
     */
    public static ColorGenerator create(List<Integer> colorList) {
        return new ColorGenerator(colorList);
    }

    /**
     * Private constructor.
     *
     * @param colorList The list of supported colors.
     */
    private ColorGenerator(List<Integer> colorList) {
        mColors = colorList;
        mRandom = new Random(System.currentTimeMillis());
    }

    /**
     * Get a random color from the list of supported colors.
     *
     * @return The random color.
     */
    public int getRandomColor() {
        return mColors.get(mRandom.nextInt(mColors.size()));
    }

    /**
     * Get a color for a provided key. The key is used to determine the color
     * index via Math.abs(key.hashCode()) % supportedColors.size()
     *
     * @param key The key used to obtain a color.
     *
     * @return The color.
     */
    public int getColor(Object key) {
        return mColors.get(Math.abs(key.hashCode()) % mColors.size());
    }
}
