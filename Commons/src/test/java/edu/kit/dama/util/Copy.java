package edu.kit.dama.util;
/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.nio.file.*;
import static java.nio.file.StandardCopyOption.*;
import java.nio.file.attribute.*;
import static java.nio.file.FileVisitResult.*;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Modified code to copy directory structures skipping '.svn' folders.
 */
public class Copy {

  /**
   * Copy source file to target location. If {@code prompt} is true then prompt
   * user to overwrite target if it exists. The {@code preserve} parameter
   * determines if file attributes should be copied/preserved.
   */
  static void copyFile(Path source, Path target, boolean prompt, boolean preserve) {
    CopyOption[] options = (preserve)
            ? new CopyOption[]{COPY_ATTRIBUTES, REPLACE_EXISTING}
            : new CopyOption[]{REPLACE_EXISTING};
    if (!prompt || Files.notExists(target)) {
      try {
        Files.copy(source, target, options);
      } catch (IOException x) {
        System.err.format("Unable to copy: %s: %s%n", source, x);
      }
    }
  }

  /**
   * A {@code FileVisitor} that copies a file-tree ("cp -r")
   */
  static class SvnTreeCopier implements FileVisitor<Path> {

    private final Path source;
    private final Path target;
    private final boolean prompt;
    private final boolean preserve;

    SvnTreeCopier(Path source, Path target, boolean prompt, boolean preserve) {
      this.source = source;
      this.target = target;
      this.prompt = prompt;
      this.preserve = preserve;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            // skip directories containing svn files
      if (dir.endsWith(".svn")) {
        return SKIP_SUBTREE;
      }
      CopyOption[] options = (preserve)
              ? new CopyOption[]{COPY_ATTRIBUTES} : new CopyOption[0];

      Path newdir = target.resolve(source.relativize(dir));
      try {
        Files.copy(dir, newdir, options);
      } catch (FileAlreadyExistsException x) {
        // ignore
      } catch (IOException x) {
        System.err.format("Unable to create: %s: %s%n", newdir, x);
        return SKIP_SUBTREE;
      }
      return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
      copyFile(file, target.resolve(source.relativize(file)),
              prompt, preserve);
      return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
      // fix up modification time of directory when done
      if (exc == null && preserve) {
        Path newdir = target.resolve(source.relativize(dir));
        try {
          FileTime time = Files.getLastModifiedTime(dir);
          Files.setLastModifiedTime(newdir, time);
        } catch (IOException x) {
          System.err.format("Unable to copy all attributes to: %s: %s%n", newdir, x);
        }
      }
      return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
      if (exc instanceof FileSystemLoopException) {
        System.err.println("cycle detected: " + file);
      } else {
        System.err.format("Unable to copy: %s: %s%n", file, exc);
      }
      return CONTINUE;
    }
  }

  public static void main(String[] args) throws IOException {
    Path source = Paths.get("src/test/resources/ziptest/");
    
    Path dest = Paths.get("target/test/sources/filtered/");
    
    boolean prompt = false;
    boolean preserve = true;
    EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
    SvnTreeCopier tc = new SvnTreeCopier(source, dest, prompt, preserve);
    Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);
  }
}
