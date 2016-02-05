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
package edu.kit.dama.staging.interfaces;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.entities.StagingFile;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;

/**
 * Storage Virtualization Service interface. This interface defines the
 * implementation of a storage virtualization adapter used for transferring a
 * directory structure from some caching location to the final storage location.
 * Normally, this should be an site-internal process without security but with
 * high-performance. For special use cases requiering security measures external
 * initialization of security is necessary.
 *
 * This interface basically defines two main features: storing a directory
 * structure and restoring it. For storing a directory, the ingest entity, the
 * source folder and the owner have to be provided. The destination will be
 * chosen by the implementation as it depends on the structure of the
 * underlaying storage infrastructure and its organization.
 *
 * For restoring a directory tree within the cache, the restore functionality is
 * used. There the actual file tree is provided including the user who requested
 * the download. The location where the file structure is restored is again
 * chosen by the adapter implementation. The digital object ID is extracted from
 * the provided file tree. However, the file tree does not have to represent the
 * entire file structure of a digital object. It can also contains a selection
 * of the object. However, the selection itself has to be managed by higher
 * level implementations.
 *
 * The final feature provided by this interface is the validation by hashing the
 * content of files. The availability of this feature highly depends on the
 * implementation of this adapter interface and on the storage technology. To
 * check whether a hash type is supported or not, *
 * isHashTypeSupported(IStorageVirtualizationServiceAdapter.HASH_TYPE type) can
 * be used.
 *
 * @author Thomas Jejkal <a>mailto:support@kitdatamanager.net</a>
 */
public interface IStorageVirtualizationServiceAdapter extends IConfigurableAdapter {

  /**
   * Possible checksum algorithms
   */
  enum HASH_TYPE {

    MD5, SHA, SHA256, SHA384, SHA512;
  }

  /**
   * Stores the data withing pContainer depending on the StorageVirtualization
   * implementation.
   *
   * @param pContainer The container to store. This container contains the
   * FileTree and all necessary transfer information.
   * @param pContext The authorization context containing the owner of the
   * transfer.
   *
   * @return The file tree representing the staged file structure
   */
  IFileTree store(TransferTaskContainer pContainer, IAuthorizationContext pContext);

  /**
   * Restores a given file tree into a given StagingFile.
   *
   * @param pDownload The download entity containing all information needed for
   * performing the download
   * @param pFileTree The tree to restore.
   * @param pDownloadDestination The destination that will be accessible by the
   * download client.
   *
   * @return TRUE if all files in pFileTree could be restored at
   * pDownloadDestination
   */
  boolean restore(DownloadInformation pDownload, IFileTree pFileTree, StagingFile pDownloadDestination);

  /**
   * Returns the checksum of the file referenced by given logical file name.
   *
   * @param pFile File which should be checksummed
   * @param type Checksum algorithm
   * @return The checksum
   */
  String calculateChecksum(StagingFile pFile, HASH_TYPE type);

  /**
   * Returns {@code true} if the given checksum algorithm is supported by this
   * Storage Virtualization implementation. Otherwise, returns {@code false}.
   *
   * @param type Checksum algorithm
   * @return If given checksum algorithm is supported or not.
   */
  boolean isHashTypeSupported(HASH_TYPE type);
}
