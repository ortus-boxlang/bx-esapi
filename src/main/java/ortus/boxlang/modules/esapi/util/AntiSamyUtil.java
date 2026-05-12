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
package ortus.boxlang.modules.esapi.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.owasp.validator.html.InternalPolicy;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.model.Property;
import org.owasp.validator.html.model.Tag;

import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

/**
 * Utility class for AntiSamy
 */
public class AntiSamyUtil {

	/**
	 * Default policy
	 */
	public static final String	DEFAULT_POLICY	= "ebay";

	/**
	 * Available Policies in the AntiSamy library
	 */
	public static final IStruct	POLICIES		= Struct.of(
	    "anythinggoes", getPolicy( "/antisamy-anythinggoes.xml" ),
	    "ebay", getPolicy( "/antisamy-ebay.xml" ),
	    "myspace", getPolicy( "/antisamy-myspace.xml" ),
	    "slashdot", getPolicy( "/antisamy-slashdot.xml" ),
	    "tinymce", getPolicy( "/antisamy-tinymce.xml" )
	);

	/**
	 * Validate that the incoming policy is not a local one or a file
	 *
	 * @param policy The policy to validate
	 *
	 * @throws BoxRuntimeException If the policy is invalid
	 */
	public static void validatePolicy( String policy ) {
		if ( !POLICIES.containsKey( policy ) && !new File( policy ).exists() ) {
			throw new BoxRuntimeException(
			    "Invalid Policy [" + policy + "]. Policy must be one of: " + POLICIES.keySet() + " or a valid policy file path."
			);
		}
	}

	/**
	 * Load the policy from the AntiSamy library or a custom one
	 *
	 * @param policy The policy to load
	 *
	 * @return The policy
	 */
	public static Policy loadPolicy( String policy ) {
		try {
			// Is this a core one or custom
			if ( POLICIES.containsKey( policy ) ) {
				return Policy.getInstance( ( URL ) POLICIES.get( Key.of( policy ) ) );
			}
			return Policy.getInstance( policy );
		} catch ( Exception e ) {
			throw new BoxRuntimeException( "Error loading policy [" + policy + "]", e );
		}
	}

	/**
	 * Returns a copy of the given policy with directive overrides applied from a struct.
	 * Keys must be the camelCase directive names used by AntiSamy (e.g. {@code maxInputSize},
	 * {@code nofollowAnchors}, {@code preserveComments}).
	 *
	 * @param base      The base policy to copy
	 * @param overrides A struct of directive key/value pairs to override
	 *
	 * @return A new policy with the overrides applied
	 */
	@SuppressWarnings( "unchecked" )
	public static Policy withDirectives( Policy base, IStruct overrides ) {
		try {
			Field directivesField = Policy.class.getDeclaredField( "directives" );
			directivesField.setAccessible( true );
			Map<String, String> directives = new HashMap<>( ( Map<String, String> ) directivesField.get( base ) );
			overrides.entrySet().forEach( entry -> directives.put( entry.getKey().getName(), entry.getValue().toString() ) );

			Field tagRulesField = Policy.class.getDeclaredField( "tagRules" );
			tagRulesField.setAccessible( true );

			Field cssRulesField = Policy.class.getDeclaredField( "cssRules" );
			cssRulesField.setAccessible( true );

			Constructor<InternalPolicy> ctor = InternalPolicy.class.getDeclaredConstructor(
			    Policy.class, Map.class, Map.class, Map.class );
			ctor.setAccessible( true );
			return ctor.newInstance( base,
			    directives,
			    ( Map<String, Tag> ) tagRulesField.get( base ),
			    ( Map<String, Property> ) cssRulesField.get( base ) );
		} catch ( Exception e ) {
			throw new BoxRuntimeException( "Error applying policy directive overrides", e );
		}
	}

	/**
	 * Get the policy URL from the JAR
	 *
	 * @param policy The policy name
	 *
	 * @return The URL of the policy
	 */
	private static URL getPolicy( String policy ) {
		return AntiSamyUtil.class.getResource( policy );
	}
}
