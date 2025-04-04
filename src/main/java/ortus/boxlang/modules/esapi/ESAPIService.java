/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ortus.boxlang.modules.esapi;

import java.io.OutputStream;
import java.io.PrintStream;

import org.owasp.esapi.ESAPI;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.services.BaseService;

/**
 * The ESAPIService is a service that provides access to the ESAPI library.
 * It is used to perform various security-related tasks, such as encoding and
 * decoding data, generating random numbers, and validating input.
 *
 * @author Ortus Solutions, Corp.
 */
public class ESAPIService extends BaseService {

	/**
	 * --------------------------------------------------------------------------
	 * Constants
	 * --------------------------------------------------------------------------
	 */

	private static final Key SERVICE_NAME = new Key( "ESAPIService" );

	static {
		// Load the ESAPI System Properties
		System.setProperty( "org.owasp.esapi.opsteam", "" );
		System.setProperty( "org.owasp.esapi.devteam", "" );
	}

	/**
	 * --------------------------------------------------------------------------
	 * Constructors
	 * --------------------------------------------------------------------------
	 */

	/**
	 * public no-arg constructor for the ServiceProvider
	 */
	public ESAPIService() {
		this( BoxRuntime.getInstance(), SERVICE_NAME );
	}

	/**
	 * Constructor for the ESAPIService
	 *
	 * @param runtime the BoxRuntime instance
	 * @param name    the name of the service
	 */
	public ESAPIService( BoxRuntime runtime, Key name ) {
		super( runtime, name );
		PrintStream originalOut = System.out;
		try {
			System.setOut( new PrintStream( OutputStream.nullOutputStream() ) );
			ESAPI.securityConfiguration(); // triggers config loading
		} finally {
			System.setOut( originalOut );
		}
	}

	@Override
	public void onConfigurationLoad() {
	}

	@Override
	public void onShutdown( Boolean arg0 ) {
	}

	@Override
	public void onStartup() {
	}

}
