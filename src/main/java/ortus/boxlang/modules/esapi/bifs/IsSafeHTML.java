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
import org.owasp.validator.html.Policy;

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
import ortus.boxlang.runtime.types.IStruct;
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
		    new Argument( false, Argument.ANY, KeyDirectory.policy, "" ),
		    new Argument( false, Argument.BOOLEAN, KeyDirectory.force, false )
		};
	}

	/**
	 * Verifies if the HTML is safe using antisamy policy rules.
	 * <p>
	 * The policy can be a string name of a built-in policy, a file path to a custom policy XML file,
	 * or a struct for programmatic policy configuration. See {@code getSafeHTML()} for full struct documentation.
	 * <p>
	 * Built-in policies: anythinggoes, ebay (default), myspace, slashdot, tinymce
	 *
	 * @param context   The current Box context
	 * @param arguments The arguments passed to the function
	 *
	 * @arguments.string The HTML to validate
	 *
	 * @arguments.policy The policy to use: a string name, file path, or a struct for programmatic configuration
	 *
	 * @arguments.force When true and using a struct policy, evicts it from cache and rebuilds it
	 *
	 * @return True if the HTML is safe, false otherwise
	 */
	public Object _invoke( IBoxContext context, ArgumentsScope arguments ) {
		String	input		= arguments.getAsString( Key.string ).trim();
		Object	policyArg	= arguments.get( KeyDirectory.policy );
		boolean	force		= arguments.getAsBoolean( KeyDirectory.force );

		try {
			Policy			loadedPolicy	= resolvePolicy( policyArg, force );
			CleanResults	results			= new AntiSamy().scan( input, loadedPolicy );
			return results.getNumberOfErrors() == 0;
		} catch ( BoxRuntimeException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new BoxRuntimeException( "Error while checking HTML safety: " + e.getMessage(), e );
		}
	}

	/**
	 * Resolve a policy argument to a Policy object.
	 */
	private Policy resolvePolicy( Object policyArg, boolean force ) {
		if ( policyArg instanceof IStruct structPolicy ) {
			if ( force ) {
				AntiSamyUtil.removePolicyFromCache( structPolicy );
			}
			return AntiSamyUtil.buildPolicyFromStruct( structPolicy );
		}

		String policy = policyArg.toString().trim();
		if ( policy.isEmpty() ) {
			policy = AntiSamyUtil.DEFAULT_POLICY;
		}

		AntiSamyUtil.validatePolicy( policy );
		return AntiSamyUtil.loadPolicy( policy );
	}

}
