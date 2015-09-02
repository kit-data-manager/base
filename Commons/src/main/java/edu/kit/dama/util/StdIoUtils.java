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
package edu.kit.dama.util;

import java.io.IOException;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some methods for convenient io-operations.
 * @author hartmann-v
 */
public class StdIoUtils {
  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(StdIoUtils.class);
  /**
   * Separator between outputs.
   */
  public static final String separator = "------------------------------------" 
          + "-------------------------------------";

  /**
   * Read integer from stdin.
   * Allowed values are 1 to pMaxValue.
   * If value is not parseable or outside of range
   * 1 will be returned.
   * @param pMaxValue maximal allowed value
   * @return number [1 - pMaxValue]
   */
  public static int readIntFromStdInput(int pMaxValue) {
    return readIntFromStdInput(pMaxValue, 1);
  }

  /**
   * Read integer from stdin.
   * Allowed values are 1 to pMaxValue.
   * If (default)value is not parseable or outside of range
   * 1 will be returned.
   * @param pMaxValue maximal allowed value
   * @param pDefault defaultValue if nothing is selected or input is invalid.
   * @return number [1 - pMaxValue]
   */
  public static int readIntFromStdInput(int pMaxValue, int pDefault) {
    String numberString = readStdInput();
    int parseInt = pDefault;
    try {
      parseInt = Integer.parseInt(numberString);
    } catch (NumberFormatException nfe) {
      LOGGER.error("No valid input! Input has to be a number.");
    }
    if ((parseInt < 1) || (pMaxValue < parseInt)) {
      parseInt = 1;
    }
    return parseInt;
  }

  /**
   * Read from stdin.
   * @return input as string.
   */
  public static String readStdInput() {
    return readStdInput(new String());
  }

  /**
   * Read from stdin.
   * @param pDefault if no input this string will be returned.
   * @return input as string.
   */
  public static String readStdInput(String pDefault) {
    final int CR = 13;
    byte[] input = new byte[64];
    String inputString = pDefault;
    try {
      int noOfBytes = System.in.read(input);
      if ((noOfBytes > 1) && (input[0] != CR)) {
        inputString = new String(input).trim();
      }
    } catch (IOException ex) {
      LOGGER.error(null, ex);
    }
    return inputString;
  }

  /**
   * Print summary of query with a given layout.
   *
   * @param pPrintStream Stream to print to.
   * @param pMessage Message to print.
   */
  public static void printSummary(PrintStream pPrintStream, String pMessage) {
    pPrintStream.println(pMessage);
    pPrintStream.println(separator);
    pPrintStream.println();
  }
  
}
