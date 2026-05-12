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
public class GetSafeHTML extends BIF {

	/**
	 * Constructor
	 */
	public GetSafeHTML() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, Argument.STRING, Key.string ),
		    new Argument( false, Argument.ANY, KeyDirectory.policy, "" ),
		    new Argument( false, Argument.BOOLEAN, KeyDirectory.throwOnError, false )
		};
	}

	/**
	 * Sanitizes HTML using antisamy policy rules.
	 * <p>
	 * The policy can be a string name of a built-in policy, a file path to a custom policy XML file,
	 * or a struct for programmatic policy configuration.
	 * <p>
	 * Built-in policies: anythinggoes, ebay (default), myspace, slashdot, tinymce
	 * <p>
	 * When passing a struct, the following keys are supported:
	 * <ul>
	 * <li>{@code basePolicy} - Start from a named policy and override specific parts</li>
	 * <li>{@code overrideMode} - "merge" (default) or "override" - controls how overrides are applied</li>
	 * <li>{@code directives} - Struct of directive key/value pairs (e.g. { maxInputSize: 200000 })</li>
	 * <li>{@code allowTags} - Array of tag names to allow with "validate" action</li>
	 * <li>{@code tagRules} - Struct of tag rules (tag name to action string or config struct)</li>
	 * <li>{@code globalAttributes} - Struct of attributes valid on all tags</li>
	 * <li>{@code dynamicAttributes} - Struct of wildcard attributes (e.g. data-*)</li>
	 * <li>{@code cssRules} - Struct of CSS property rules</li>
	 * <li>{@code allowedEmptyTags} - Array of self-closing tag names</li>
	 * <li>{@code requireClosingTags} - Array of tag names requiring end tags</li>
	 * <li>{@code tagsToEncode} - Array of tag names to entity-encode</li>
	 * </ul>
	 *
	 * @param context   The current Box context
	 * @param arguments The arguments passed to the function
	 *
	 * @arguments.string The HTML to sanitize
	 *
	 * @arguments.policy The policy to use: a string name, file path, or a struct for programmatic configuration
	 *
	 * @return The sanitized HTML
	 */
	public Object _invoke( IBoxContext context, ArgumentsScope arguments ) {
		String	input			= arguments.getAsString( Key.string ).trim();
		Object	policyArg		= arguments.get( KeyDirectory.policy );
		boolean	throwOnError	= arguments.getAsBoolean( KeyDirectory.throwOnError );

		try {
			Policy			loadedPolicy	= resolvePolicy( policyArg );
			CleanResults	results			= new AntiSamy().scan( input, loadedPolicy );

			if ( throwOnError && !results.getErrorMessages().isEmpty() ) {
				throw new BoxRuntimeException(
				    "HTML sanitization policy violations found: " + String.join( "; ", results.getErrorMessages() )
				);
			}

			return results.getCleanHTML();
		} catch ( BoxRuntimeException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new BoxRuntimeException( "Error while sanitizing HTML: " + e.getMessage(), e );
		}
	}

	/**
	 * Resolve a policy argument to a Policy object.
	 */
	private Policy resolvePolicy( Object policyArg ) {
		if ( policyArg instanceof IStruct structPolicy ) {
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
