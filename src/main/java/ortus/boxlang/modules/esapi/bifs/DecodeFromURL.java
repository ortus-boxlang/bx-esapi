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

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.owasp.esapi.errors.EncodingException;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

@BoxBIF
public class DecodeFromURL extends BIF {

	/**
	 * Constructor
	 */
	public DecodeFromURL() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "string", Key.string )
		};
	}

	/**
	 * Decodes an encoded HTML URL string.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.string The string to decode.
	 */
	public String _invoke( IBoxContext context, ArgumentsScope arguments ) {
		String	str		= arguments.getAsString( Key.string );

		// Get the ESAPI encoder
		Encoder	encoder	= ESAPI.encoder();

		try {
			return encoder.decodeFromURL( str );
		} catch ( EncodingException e ) {
			throw new BoxRuntimeException( e.toString() );
		}
	}

}
