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
package edu.kit.dama.staging.exceptions;

/**
 *
 * @author Tobias Schmidt <a>mailto:tobias.schmidt@kit.edu</a>
 */
public class ServiceConfigurationFailedException extends Exception {

	/**
	 * Creates a new instance of <code>ServiceConfigurationFailedException</code> 
	 * without detail message.
	 */
	public ServiceConfigurationFailedException() {
		super();
	}

	/**
	 * Constructs an instance of <code>ServiceConfigurationFailedException</code> 
	 * with the specified detail message.
	 * @param msg the detail message.
	 */
	public ServiceConfigurationFailedException(String msg) {
		super(msg);
	}

	/**
	 * Constructs an instance of <code>ServiceConfigurationFailedException</code> 
	 * with the specified detail message and a thrown exception.
	 * @param msg the detail message.
	 * @param throwable A already thrown exception
	 */
	public ServiceConfigurationFailedException(String msg, Throwable throwable) {
		super(msg, throwable);
	}
}
