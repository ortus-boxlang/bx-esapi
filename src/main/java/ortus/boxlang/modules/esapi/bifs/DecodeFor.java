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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import ortus.boxlang.modules.esapi.util.KeyDirectory;
import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;
import ortus.boxlang.runtime.validation.Validator;

@BoxBIF
@BoxBIF( alias = "esapiDecode" )
public class DecodeFor extends BIF {

	private static final List<String> DECODING_TYPES = List.of(
	    "url", "html", "json", "base64"
	);

	/**
	 * Constructor
	 */
	public DecodeFor() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, Argument.STRING, Key.type, Validator.valueOneOf( DECODING_TYPES.toArray( new String[ 0 ] ) ) ),
		    new Argument( true, Argument.STRING, Key.value, Set.of( Validator.NON_EMPTY ) )
		};
	}

	/**
	 * Decodes a string that has been encoded with ESAPIEncode with a specified encoding type.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.type The type of decoding to use. Valid values are: "url", "html", "json", "base64".
	 *
	 * @argument.value The string to decode.
	 */
	public String _invoke( IBoxContext context, ArgumentsScope arguments ) {
		Key		typeKey	= Key.of( arguments.getAsString( Key.type ) );
		String	str		= arguments.getAsString( Key.value ).trim();

		// Get the ESAPI encoder
		Encoder	encoder	= ESAPI.encoder();

		try {
			if ( typeKey.equals( KeyDirectory.html ) ) {
				return encoder.decodeForHTML( str );
			} else if ( typeKey.equals( KeyDirectory.url ) ) {
				return encoder.decodeFromURL( str );
			} else if ( typeKey.equals( KeyDirectory.json ) ) {
				return encoder.decodeFromJSON( str );
			} else if ( typeKey.equals( KeyDirectory.base64 ) ) {
				return new String( encoder.decodeFromBase64( str ), StandardCharsets.UTF_8 );
			}
		} catch ( Exception e ) {
			throw new BoxRuntimeException( e.toString() );
		}

		return str;
	}

}
