/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package ortus.boxlang.modules.esapi.bifs;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;

import ortus.boxlang.modules.esapi.util.AntiSamyUtil;
import ortus.boxlang.modules.esapi.util.KeyDirectory;
import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.bifs.BoxMember;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.BoxLangType;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

@BoxBIF
@BoxMember( type = BoxLangType.STRING )
public class IsSafeHTML extends BIF {

	/**
	 * Constructor
	 */
	public IsSafeHTML() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, Argument.STRING, Key.string ),
		    new Argument( false, Argument.STRING, KeyDirectory.policy, "" )
		};
	}

	/**
	 * Verifies if the HTML is safe using antisamy policy rules.
	 * If no policy is provided, the default policy is used which is the eBay policy.
	 * <p>
	 * Available policies are:
	 * <ul>
	 * <li>anythinggoes</li>
	 * <li>ebay</li>
	 * <li>myspace</li>
	 * <li>slashdot</li>
	 * <li>tinymce</li>
	 * </ul>
	 * <p>
	 * If a policy is not one of the above, it is assumed to be an absolute path to a custom policy file.
	 *
	 * @param context   The current Box context
	 * @param arguments The arguments passed to the function
	 *
	 * @arguments.string The HTML to sanitize
	 *
	 * @arguments.policy The policy to use for sanitization
	 *
	 * @return True if the HTML is safe, false otherwise
	 */
	public Object _invoke( IBoxContext context, ArgumentsScope arguments ) {
		// Get the input and policy
		String	input	= arguments.getAsString( Key.string ).trim();
		String	policy	= arguments.getAsString( KeyDirectory.policy ).trim();

		// Set default policy if not set
		if ( policy.isEmpty() ) {
			policy = AntiSamyUtil.DEFAULT_POLICY;
		}

		// Try to see if the passed policy is in our list of policies
		// Else, it must be a path to a policy file, verify it exists, else throw an exception
		AntiSamyUtil.validatePolicy( policy );

		try {
			// Scan the input and get clean results
			CleanResults results = new AntiSamy().scan( input, AntiSamyUtil.loadPolicy( policy ) );
			return results.getNumberOfErrors() == 0;
		} catch ( Exception e ) {
			// Handle exceptions (e.g., policy file not found, AntiSamy initialization error)
			throw new BoxRuntimeException( "Error while checking HTML safety: " + e.getMessage(), e );
		}
	}

}
