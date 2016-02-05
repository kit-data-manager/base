/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for zipping and unzipping files or directories.
 *
 * @author jejkal
 */
public final class ZipUtils {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtils.class);

    /**
     * Hidden constuctor.
     */
    private ZipUtils() {
    }

    /**
     * Write all files located in pDirectory into a zip file specified by
     * pZipOut. All entries within the zip file will be structured relative to
     * pDirectory.
     *
     * @param pDirectory The directory containing the input files
     * @param pZipOut The zip output file
     * @throws IOException If something goes wrong, in most cases if there are
     * problems with reading the input files or writing into the output file
     */
    public static void zip(File pDirectory, File pZipOut) throws IOException {
        zip(pDirectory, pDirectory.getCanonicalPath(), pZipOut);
    }

    /**
     * Write all files located in pDirectory into a zip file specified by
     * pZipOut. The provided base path is used to keep the structure defined by
     * pDirectory within the zip file.
     *
     * Due to the fact, that we have only one single base path, pDirectory must
     * start with pBasePath to avoid unexpected zip file entries.
     *
     * @param pDirectory The directory containing the input files
     * @param pBasePath The base path the will be removed from all file paths
     * before creating a new zip entry
     * @param pZipOut The zip output file
     * @throws IOException If something goes wrong, in most cases if there are
     * problems with reading the input files or writing into the output file
     */
    public static void zip(File pDirectory, String pBasePath, File pZipOut) throws IOException {
        LOGGER.info("Zipping directory '{}'", pDirectory.getPath());
        zip(pDirectory.listFiles(), pBasePath, pZipOut);
    }

    /**
     * Write all files located in pDirectory into a zip file specified by
     * pZipOut. The provided base path is used to keep the structure defined by
     * pDirectory within the zip file.
     *
     * Due to the fact, that we have only one single base path, pDirectory must
     * start with pBasePath to avoid unexpected zip file entries.
     *
     * @param pFiles The directory containing the input files
     * @param pBasePath The base path the will be removed from all file paths
     * before creating a new zip entry
     * @param pZipOut The zip output file
     * @throws IOException If something goes wrong, in most cases if there are
     * problems with reading the input files or writing into the output file
     */
    public static void zip(File[] pFiles, String pBasePath, File pZipOut) throws IOException {
        ZipOutputStream zipOut = null;
        try {
            zipOut = new ZipOutputStream(new FileOutputStream(pZipOut));
            zip(pFiles, pBasePath, zipOut);
            zipOut.finish();
            zipOut.flush();
        } finally {
            try {
                if (zipOut != null) {
                    zipOut.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Compress all files of one directory with given extensions to a single zip
     * file.
     *
     * @param pZipFile resulting zip File. Existing file will be overwritten!
     * @param pDirectory directory to explore.
     * @param pExtension allowed file name extensions of files to compress
     * @return success or not.
     */
    public static boolean zipDirectory(final File pZipFile, final File pDirectory, final String... pExtension) {
        boolean success = false;
        File[] files = pDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean accept = false;
                String lowerCase = name.toLowerCase();
                if (pExtension != null) {
                    for (String extension : pExtension) {
                        if (lowerCase.endsWith(extension.toLowerCase())) {
                            accept = true;
                            break;
                        }
                    }
                } else {
                    accept = true;
                }
                return accept;
            }
        });
        if (LOGGER.isDebugEnabled()) {
            String fileSeparator = ", ";
            LOGGER.debug("Zip files to " + pZipFile.getName());
            StringBuilder sb = new StringBuilder("Selected files: ");
            for (File file : files) {
                sb.append(file.getName()).append(fileSeparator);
            }
            int lastIndex = sb.lastIndexOf(fileSeparator);
            sb.delete(lastIndex, lastIndex + fileSeparator.length());
            LOGGER.debug(sb.toString());
        }
        try {
            zip(files, pDirectory.getPath(), pZipFile);
            success = true;
        } catch (IOException ex) {
            LOGGER.error("Error while zipping files!", ex);
        }
        return success;
    }

    /**
     * Zip a single file to target directory. zip file add the '.zip' extension
     * to filename.
     *
     * @param pTargetDir Directory to store the zip file.
     * @param pSourceFile Source file.
     * @throws IOException If there are any IO errors.
     */
    public static void zipSingleFile(File pTargetDir, File pSourceFile) throws IOException {
        File zipFile = new File(pTargetDir.getAbsolutePath() + File.separator + pSourceFile.getName() + ".zip");
        zip(new File[]{pSourceFile}, pSourceFile.getParent(), zipFile);
    }

    /**
     * Write a list of files into a ZipOutputStream. The provided base path is
     * used to keep the structure defined by pFileList within the zip file.<BR/>
     * Due to the fact, that we have only one single base path, all files within
     * 'pFileList' must have in the same base directory. Adding files from a
     * higher level will cause unexpected zip entries.
     *
     * @param pFileList The list of input files
     * @param pBasePath The base path the will be removed from all file paths
     * before creating a new zip entry
     * @param pZipOut The zip output stream
     * @throws IOException If something goes wrong, in most cases if there are
     * problems with reading the input files or writing into the output file
     */
    private static void zip(File[] pFileList, String pBasePath, ZipOutputStream pZipOut) throws IOException {
        // Create a buffer for reading the files
        byte[] buf = new byte[1024];
        try {
            // Compress the files
            LOGGER.debug("Adding {} files to archive", pFileList.length);

            for (File pFileList1 : pFileList) {
                String entryName = pFileList1.getPath().replaceAll(Pattern.quote(pBasePath), "");
                if (entryName.startsWith(File.separator)) {
                    entryName = entryName.substring(1);
                }
                
                if (pFileList1.isDirectory()) {
                    LOGGER.debug("Adding directory entry");
                    //add empty folders, too
                    pZipOut.putNextEntry(new ZipEntry((entryName + File.separator).replaceAll("\\\\", "/")));
                    pZipOut.closeEntry();
                    File[] fileList = pFileList1.listFiles();
                    if (fileList.length != 0) {
                        LOGGER.debug("Adding directory content recursively");
                        zip(fileList, pBasePath, pZipOut);
                    } else {
                        LOGGER.debug("Skipping recursive call due to empty directory");
                    }
                //should we close the entry here??
                    //pZipOut.closeEntry();
                } else {
                    LOGGER.debug("Adding file entry");
                    try (final FileInputStream in = new FileInputStream(pFileList1)) {
                        // Add ZIP entry to output stream.
                        pZipOut.putNextEntry(new ZipEntry(entryName.replaceAll("\\\\", "/")));
                        // Transfer bytes from the file to the ZIP file
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            pZipOut.write(buf, 0, len);
                        }
                        // Complete the entry
                        LOGGER.debug("Closing file entry");
                        pZipOut.closeEntry();
                    }
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("Aborting zip process due to an IOException caused by any zip stream (FileInput or ZipOutput)", ioe);
            throw ioe;
        } catch (RuntimeException e) {
            LOGGER.error("Aborting zip process due to an unexpected exception", e);
            throw new IOException("Unexpected exception during zip operation", e);
        }
    }

    /**
     * Unzip file in 'actual' directory and delete zip file.
     *
     * @param fSourceZip zip file to extract.
     * @param delete delete source file or not.
     */
    public static void unzip(File fSourceZip, boolean delete) {
        File zipPath = fSourceZip.getAbsoluteFile().getParentFile();
        unzip(fSourceZip, zipPath, delete);
    }

    /**
     * Extract a zipped file to the current working directory
     *
     * @param pZipFile The zip file to extract
     * @throws IOException If something goes wrong, in most cases if pZipFile
     * does not exist or the destination directory is not writeable
     */
    public static void unzip(File pZipFile) throws IOException {
        unzip(pZipFile, ".");
    }

    /**
     * Extract a zipped file into the provided destination directory
     *
     * @param pZipFile The zip file to extract
     * @param pDestination The destination directory
     * @throws IOException If something goes wrong, in most cases if pZipFile
     * does not exist or the destination directory is not writeable
     */
    public static void unzip(File pZipFile, File pDestination) throws IOException {
        unzip(pZipFile, pDestination, false);
    }

    /**
     * Extract a zipped file into the provided destination directory defined by
     * a path in string format
     *
     * @param pZipFile The zip file to extract
     * @param pDestination The path to the destination directory
     * @throws IOException If something goes wrong, in most cases if pZipFile
     * does not exist or the destination directory is not writeable
     */
    public static void unzip(File pZipFile, String pDestination) throws IOException {
        unzip(new File(pDestination), pZipFile);
    }

    /**
     * Unzip file in output directory and delete zip file.
     *
     * @param fSourceZip zip file to extract.
     * @param outputDir directory where the files should be extracted.
     * @param delete delete source file or not.
     */
    public static void unzip(File fSourceZip, File outputDir, boolean delete) {
        try {
            if (outputDir.mkdirs()) {
                LOGGER.debug("Create directory '{}'", outputDir.getAbsolutePath());
            }
            try (ZipFile zipFile = new ZipFile(fSourceZip)) {
                Enumeration e = zipFile.entries();

                while (e.hasMoreElements()) {
                    extractEntry(outputDir, zipFile, (ZipEntry) e.nextElement());
                }
            }

      // delete zip file in case of success
            //deletion must be outside of "try-with-resource" block as fSourceZip is in use inside
            if (delete && fSourceZip != null && fSourceZip.exists() && !org.apache.commons.io.FileUtils.deleteQuietly(fSourceZip)) {
                LOGGER.error("Error deleting '{}'!?", fSourceZip.getAbsolutePath());
            }
        } catch (IOException ioe) {
            LOGGER.error("Error while unzipping file!", ioe);
        }

    }

    /**
     * Extract single entry of zip file.
     *
     * @param pOutputDir where to store the unzipped entry.
     * @param pZipFile file containing zipped files.
     * @param pEntry one entry to extract.
     * @throws IOException missing access rights?
     */
    private static void extractEntry(File pOutputDir, ZipFile pZipFile, ZipEntry pEntry) throws IOException {
        File destinationFilePath = new File(pOutputDir, pEntry.getName());
        if (pEntry.isDirectory()) {
      // The following command is neccessary to create also
            // empty directories.
            if (destinationFilePath.mkdirs()) {
                LOGGER.trace("create directory: '{}'", destinationFilePath.getPath());
            }
        } else {
            //create directories if required.
            if (destinationFilePath.getParentFile().mkdirs()) {
                LOGGER.trace("create directory: '{}'", destinationFilePath.getPath());
            }
        }
        //if the entry is directory, leave it. Otherwise extract it.
        if (!pEntry.isDirectory()) {
            LOGGER.debug("Extracting " + destinationFilePath);

            /*
             * Get the InputStream for current entry
             * of the zip file using
             *
             * InputStream getInputStream(Entry entry) method.
             */
            int noOfBytes;
            byte buffer[] = new byte[1024];

            try (BufferedInputStream bis = new BufferedInputStream(pZipFile.getInputStream(pEntry));
                    FileOutputStream fos = new FileOutputStream(destinationFilePath);
                    BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);) {
                /*
                 * read the current entry from the zip file, extract it
                 * and write the extracted file.
                 */
                while ((noOfBytes = bis.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, noOfBytes);
                }
                //flush the output stream.
                bos.flush();
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        ZipUtils.zip(new File("/Users/jejkal/Downloads/omf2097/"), new File("/Users/jejkal/Downloads/omf2097.zip"));
    }
}
