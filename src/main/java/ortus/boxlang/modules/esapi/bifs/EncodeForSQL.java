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
import org.owasp.esapi.codecs.Codec;
import org.owasp.esapi.codecs.DB2Codec;
import org.owasp.esapi.codecs.MySQLCodec;
import org.owasp.esapi.codecs.MySQLCodec.Mode;
import org.owasp.esapi.codecs.OracleCodec;

import ortus.boxlang.modules.esapi.util.KeyDirectory;
import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;

@BoxBIF
public class EncodeForSQL extends BIF {

	/**
	 * Constructor
	 */
	public EncodeForSQL() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "string", Key.string ),
		    new Argument( true, "string", KeyDirectory.dialect ),
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
		String	str				= arguments.getAsString( Key.string );
		String	dialect			= arguments.getAsString( KeyDirectory.dialect );
		boolean	canonicalize	= arguments.getAsBoolean( Key.canonicalize );

		// Get the ESAPI encoder
		Encoder	encoder			= ESAPI.encoder();

		// Canonicalize the input string if requested
		if ( canonicalize ) {
			str = encoder.canonicalize( str );
		}

		return encoder.encodeForSQL( getCodec( dialect ), str );
	}

	/**
	 * Get the codec for the specified dialect.
	 *
	 * @param dialect The dialect for which to get the codec.
	 *
	 * @return The codec for the specified dialect.
	 */
	private Codec<?> getCodec( String dialect ) {

		Key dialectKey = Key.of( dialect );

		if ( dialectKey.equals( KeyDirectory.mysql_ansi ) ) {
			return new MySQLCodec( Mode.ANSI );
		}

		if ( dialectKey.equals( KeyDirectory.mysql ) ) {
			return new MySQLCodec( Mode.STANDARD );
		}

		if ( dialectKey.equals( KeyDirectory.oracle ) ) {
			return new OracleCodec();
		}

		if ( dialectKey.equals( KeyDirectory.db2 ) ) {
			return new DB2Codec();
		}

		return null;
	}
}
