/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.util;

/**
 *
 * @author jejkal
 */
public final class FormatUtils {

  private static final long K = 1024;
  private static final long M = K * K;
  private static final long G = M * K;
  private static final long T = G * K;

  /**
   * Hidden constuctor.
   */
  private FormatUtils() {
  }

  /**
   * Convert size in bytes to a human readable format. Code from
   * http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
   *
   * @param pSize The file size in bytes
   *
   * @return The string representation in a human readable format
   */
  public static String convertToStringRepresentation(final long pSize) {
    final long[] dividers = new long[]{T, G, M, K, 1};
    final String[] units = new String[]{"TB", "GB", "MB", "KB", "B"};
    if (pSize < 1) {
      return "0 B";
    }
    String result = null;
    for (int i = 0; i < dividers.length; i++) {
      final long divider = dividers[i];
      if (pSize >= divider) {
        result = format(pSize, divider, units[i]);
        break;
      }
    }
    return result;
  }

  /**
   * Helper method for convertToStringRepresentation(long)
   *
   * @param value The value to format.
   * @param divider The divider.
   * @param unit The unit to append.
   *
   * @return The formatted string.
   */
  private static String format(final long value,
          final long divider,
          final String unit) {
    final double result = divider > 1 ? (double) value / (double) divider : (double) value;
    return String.format("%.1f %s", Double.valueOf(result), unit);
  }
}
