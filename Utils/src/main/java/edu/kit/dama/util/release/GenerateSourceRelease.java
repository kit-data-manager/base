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
package edu.kit.dama.util.release;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 *
 * @author mf6319
 */
public class GenerateSourceRelease {

  private final static String[] sourceFolders = new String[]{"Authorization", "Commons", "Core", "DataOrganization", "DataWorkflow", "Documentation", "MetaDataManagement", "RestInterfaces", "Staging", "UserInterface", "Utils"};

  private final static String[] foldersToIgnore = new String[]{"target"};
  private final static String[] filesToIgnore = new String[]{"nbactions*.xml", "nb-configuration.xml", "dependency-reduced-pom.xml"};

  public static void main(String[] args) {

    //copy src folders
    //copy pom.xml
    //copy libs 
    //check license headers
    File src = new File("D:\\GRID\\src\\Libraries\\KDM\\trunk");
    File dest = new File("D:\\GRID\\src\\Libraries\\KDM\\trunk\\assembly\\source\\");
    dest.mkdirs();

    for (String folder : sourceFolders) {
      try {
        new File(dest, folder).mkdirs();
        FileUtils.copyDirectory(new File(src, folder), new File(dest, folder), new FileFilter() {

          @Override
          public boolean accept(File pathname) {
            //check folders to ignore
            for (String folder : foldersToIgnore) {
              if (pathname.isDirectory() && folder.equals(pathname.getName())) {
                return false;
              }
            }

            //check files to ignore
            for (String file : filesToIgnore) {
              if (pathname.isFile() && new WildcardFileFilter(file).accept(pathname)) {
                return false;
              }
            }

            return true;
          }
        });
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    try {
      FileUtils.copyFile(new File(src, "pom.xml"), new File(dest, "pom.xml"));
      new File(dest, "libs").mkdirs();
      FileUtils.copyDirectory(new File(src, "libs"), new File(dest, "libs"));
    } catch (IOException ex) {
      System.err.println("Failed to copy source code.");
      ex.printStackTrace();
      System.exit(1);
    }

    //check license headers
    Collection<File> files = FileUtils.listFiles(dest, new String[]{"java", "xml"}, true);
    for (File f : files) {
      boolean haveLicense = false;
      try {
        BufferedReader bf = new BufferedReader(new FileReader(f));
        String line;
        while ((line = bf.readLine()) != null) {
          if (line.contains("Licensed under the Apache License")) {
            haveLicense = true;
            break;
          }
        }
      } catch (IOException ex) {
        System.err.println("Failed to check license headers.");
        ex.printStackTrace();
        System.exit(1);
      }
      if (!haveLicense) {
        System.out.println("File  " + f + " seems to have no/an invalid license header.");
      }
    }

    System.out.println("Generating Release finished.");
    System.out.println("Please manually check pom.xml:");
    System.out.println(" - Remove internal modules (e.g. FunctionalTests)");
    System.out.println(" - Remove internal profiles");
    System.out.println(" - Update links to SCM, ciManagement and internal repositories");

  }
}
