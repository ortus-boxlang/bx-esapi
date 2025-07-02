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
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

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
		    new Argument( false, "boolean", Key.canonicalize, false )
		};
	}

	/**
	 * Encodes a string for safe inclusion in SQL statements to help prevent SQL injection attacks.
	 *
	 * <p>
	 * This function uses the OWASP ESAPI encoder to escape potentially dangerous characters
	 * based on the specified SQL dialect. Optionally, the input string can be canonicalized before encoding,
	 * which helps normalize any obfuscated attacks (e.g., double encoding).
	 * </p>
	 *
	 * <p>
	 * <strong>Note:</strong> Use this only when absolutely necessary and you fully understand the implications.
	 * OWASP ESAPI has deprecated this method by default due to common misuse. Prefer using prepared statements
	 * or parameterized queries to avoid SQL injection altogether. If you still wish to use this method,
	 * make sure to explicitly allow it in your ESAPI.properties via
	 * <code>ESAPI.dangerouslyAllowUnsafeMethods.methodNames</code>.
	 * </p>
	 *
	 * @param context   The current BoxLang context in which this BIF is being invoked.
	 * @param arguments The argument scope passed to the BIF.
	 *
	 * @return The SQL-encoded version of the input string.
	 *
	 * @argument.string The input string to encode.
	 * 
	 * @argument.dialect The SQL dialect (e.g., "mysql", "oracle", "mssql") to determine which codec to use.
	 * 
	 * @argument.canonicalize Whether to canonicalize the input before encoding (true/false).
	 *
	 * @throws org.owasp.esapi.errors.NotConfiguredByDefaultException
	 *                                                                if encodeForSQL is not explicitly enabled in the ESAPI configuration.
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
		} else if ( dialectKey.equals( KeyDirectory.mysql ) ) {
			return new MySQLCodec( Mode.STANDARD );
		} else if ( dialectKey.equals( KeyDirectory.oracle ) ) {
			return new OracleCodec();
		} else if ( dialectKey.equals( KeyDirectory.db2 ) ) {
			return new DB2Codec();
		} else {
			throw new BoxRuntimeException( "Invalid dialect: " + dialect );
		}
	}
}
