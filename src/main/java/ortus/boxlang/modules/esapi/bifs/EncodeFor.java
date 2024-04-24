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

import ortus.boxlang.modules.esapi.util.KeyDirectory;
import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

@BoxBIF
public class EncodeFor extends BIF {

	/**
	 * Constructor
	 */
	public EncodeFor() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "string", Key.type ),
		    new Argument( false, "string", Key.value )
		};
	}

	/**
	 * Encodes a given string for safe output in the specified context. The encoding is meant to mitigate Cross Site Scripting (XSS) attacks.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.type The type of encoding to use. Valid values are: "css", "dn", "html", "htmlAttribute", "javascript", "ldap", "sql", "url", "xml",
	 *                "xmlAttribute", "xpath".
	 *
	 * @argument.value The string to encode.
	 */
	public String _invoke( IBoxContext context, ArgumentsScope arguments ) {
		Key		typeKey	= Key.of( arguments.getAsString( Key.type ) );
		String	str		= arguments.getAsString( Key.value );

		// Get the ESAPI encoder
		Encoder	encoder	= ESAPI.encoder();

		if ( typeKey.equals( KeyDirectory.css ) ) {
			return encoder.encodeForCSS( str );
		}

		if ( typeKey.equals( KeyDirectory.dn ) ) {
			return encoder.encodeForDN( str );
		}

		if ( typeKey.equals( KeyDirectory.html ) ) {
			return encoder.encodeForHTML( str );
		}

		if ( typeKey.equals( KeyDirectory.htmlAttribute ) ) {
			return encoder.encodeForHTMLAttribute( str );
		}

		if ( typeKey.equals( KeyDirectory.javascript ) ) {
			return encoder.encodeForJavaScript( str );
		}

		if ( typeKey.equals( KeyDirectory.ldap ) ) {
			return encoder.encodeForLDAP( str );
		}

		if ( typeKey.equals( KeyDirectory.url ) ) {
			try {
				return encoder.encodeForURL( str );
			} catch ( Exception e ) {
				throw new BoxRuntimeException( e.toString() );
			}
		}

		if ( typeKey.equals( KeyDirectory.xml ) ) {
			return encoder.encodeForXML( str );
		}

		if ( typeKey.equals( KeyDirectory.xmlAttribute ) ) {
			return encoder.encodeForXMLAttribute( str );
		}

		if ( typeKey.equals( KeyDirectory.xPath ) ) {
			return encoder.encodeForXPath( str );
		}

		return null;
	}

}
