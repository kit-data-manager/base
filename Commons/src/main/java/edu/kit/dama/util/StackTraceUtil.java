/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 * Taken from http://www.javapractices.com/topic/TopicAction.do?Id=78
 *
 * @author jejkal
 */
public final class StackTraceUtil {

  /**
   * Hidden constuctor.
   */
  private StackTraceUtil() {
  }

  /**
   * Get the stacktrace of the provided throwable as string.
   *
   * @param aThrowable The throwable.
   *
   * @return The string representation of the stacktrace.
   */
  public static String getStackTrace(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }

  /**
   * Defines a custom format for the stack trace as String.
   *
   * @param aThrowable The throwable to print.
   * @param pHtmlOutput Indicates HTML output. In this case, line breaks will be
   * replaced by &lt;br/&gt; and tabs by three HTML whitespaces.
   *
   * @return The throwable as string.
   */
  public static String getCustomStackTrace(Throwable aThrowable, boolean pHtmlOutput) {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    aThrowable.printStackTrace(new PrintStream(bout));

    if (pHtmlOutput) {
      return bout.toString().replaceAll(System.getProperty("line.separator"), "<br/>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;");
    }
    return bout.toString();
  }
}
