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
package ortus.boxlang.modules.bxesapi.bifs;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

@BoxBIF
public class EncodeFor extends BIF {

	static final class BIFKeys {

		public static final Key	css				= Key.of( "css" );
		public static final Key	dn				= Key.of( "dn" );
		public static final Key	html			= Key.of( "html" );
		public static final Key	htmlAttribute	= Key.of( "htmlAttribute" );
		public static final Key	javascript		= Key.of( "javascript" );
		public static final Key	ldap			= Key.of( "ldap" );
		public static final Key	url				= Key.of( "url" );
		public static final Key	xml				= Key.of( "xml" );
		public static final Key	xmlAttribute	= Key.of( "xmlAttribute" );
		public static final Key	xPath			= Key.of( "xPath" );

	}

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

		if ( typeKey.equals( BIFKeys.css ) ) {
			return encoder.encodeForCSS( str );
		}

		if ( typeKey.equals( BIFKeys.dn ) ) {
			return encoder.encodeForDN( str );
		}

		if ( typeKey.equals( BIFKeys.html ) ) {
			return encoder.encodeForHTML( str );
		}

		if ( typeKey.equals( BIFKeys.htmlAttribute ) ) {
			return encoder.encodeForHTMLAttribute( str );
		}

		if ( typeKey.equals( BIFKeys.javascript ) ) {
			return encoder.encodeForJavaScript( str );
		}

		if ( typeKey.equals( BIFKeys.ldap ) ) {
			return encoder.encodeForLDAP( str );
		}

		if ( typeKey.equals( BIFKeys.url ) ) {
			try {
				return encoder.encodeForURL( str );
			} catch ( Exception e ) {
				throw new BoxRuntimeException( e.toString() );
			}
		}

		if ( typeKey.equals( BIFKeys.xml ) ) {
			return encoder.encodeForXML( str );
		}

		if ( typeKey.equals( BIFKeys.xmlAttribute ) ) {
			return encoder.encodeForXMLAttribute( str );
		}

		if ( typeKey.equals( BIFKeys.xPath ) ) {
			return encoder.encodeForXPath( str );
		}

		return null;
	}

}