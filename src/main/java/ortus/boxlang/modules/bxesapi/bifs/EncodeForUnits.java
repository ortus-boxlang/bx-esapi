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
import org.owasp.esapi.errors.EncodingException;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

@BoxBIF( alias = "encodeForCSS" )
@BoxBIF( alias = "encodeForDN" )
@BoxBIF( alias = "encodeForHTML" )
@BoxBIF( alias = "encodeForHTMLAttribute" )
@BoxBIF( alias = "encodeForJavaScript" )
@BoxBIF( alias = "encodeForLDAP" )
@BoxBIF( alias = "encodeForURL" )
@BoxBIF( alias = "encodeForXML" )
@BoxBIF( alias = "encodeForXMLAttribute" )
@BoxBIF( alias = "encodeForXPath" )
public class EncodeForUnits extends BIF {

	static final class BIFMethods {

		public static final Key	encodeForCSS			= Key.of( "encodeForCSS" );
		public static final Key	encodeForDN				= Key.of( "encodeForDN" );
		public static final Key	encodeForHTML			= Key.of( "encodeForHTML" );
		public static final Key	encodeForHTMLAttribute	= Key.of( "encodeForHTMLAttribute" );
		public static final Key	encodeForJavaScript		= Key.of( "encodeForJavaScript" );
		public static final Key	encodeForLDAP			= Key.of( "encodeForLDAP" );
		public static final Key	encodeForURL			= Key.of( "encodeForURL" );
		public static final Key	encodeForXML			= Key.of( "encodeForXML" );
		public static final Key	encodeForXMLAttribute	= Key.of( "encodeForXMLAttribute" );
		public static final Key	encodeForXPath			= Key.of( "encodeForXPath" );

	}

	/**
	 * Constructor
	 */
	public EncodeForUnits() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "string", Key.string ),
		    new Argument( true, "boolean", Key.canonicalize, false )
		};
	}

	/**
	 * Encodes the input string for safe output in the body of a HTML tag.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.string The string to encode.
	 *
	 * @argument.canonicalize Whether to canonicalize the string before encoding.
	 */
	public String _invoke( IBoxContext context, ArgumentsScope arguments ) {
		Key		bifMethodKey	= arguments.getAsKey( BIF.__functionName );
		String	str				= arguments.getAsString( Key.string );
		boolean	canonicalize	= arguments.getAsBoolean( Key.canonicalize );

		// Get the ESAPI encoder
		Encoder	encoder			= ESAPI.encoder();

		// Canonicalize the input string if requested
		if ( canonicalize ) {
			str = encoder.canonicalize( str );
		}

		if ( bifMethodKey.equals( BIFMethods.encodeForCSS ) ) {
			return encoder.encodeForCSS( str );
		}

		if ( bifMethodKey.equals( BIFMethods.encodeForDN ) ) {
			return encoder.encodeForDN( str );
		}

		if ( bifMethodKey.equals( BIFMethods.encodeForHTML ) ) {
			return encoder.encodeForHTML( str );
		}

		if ( bifMethodKey.equals( BIFMethods.encodeForHTMLAttribute ) ) {
			return encoder.encodeForHTMLAttribute( str );
		}

		if ( bifMethodKey.equals( BIFMethods.encodeForJavaScript ) ) {
			return encoder.encodeForJavaScript( str );
		}

		if ( bifMethodKey.equals( BIFMethods.encodeForLDAP ) ) {
			return encoder.encodeForLDAP( str );
		}

		if ( bifMethodKey.equals( BIFMethods.encodeForURL ) ) {
			try {
				return encoder.encodeForURL( str );
			} catch ( EncodingException e ) {
				throw new BoxRuntimeException( e.toString() );
			}
		}

		if ( bifMethodKey.equals( BIFMethods.encodeForXML ) ) {
			return encoder.encodeForXML( str );
		}

		if ( bifMethodKey.equals( BIFMethods.encodeForXMLAttribute ) ) {
			return encoder.encodeForXMLAttribute( str );
		}

		if ( bifMethodKey.equals( BIFMethods.encodeForXPath ) ) {
			return encoder.encodeForXPath( str );
		}

		return null;
	}

}