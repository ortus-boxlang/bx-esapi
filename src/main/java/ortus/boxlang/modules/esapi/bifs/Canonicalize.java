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
public class Canonicalize extends BIF {

	/**
	 * Constructor
	 */
	public Canonicalize() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, Argument.STRING, Key.input ),
		    new Argument( true, Argument.BOOLEAN, KeyDirectory.restrictMultiple ),
		    new Argument( true, Argument.BOOLEAN, KeyDirectory.restrictMixed ),
		    new Argument( false, Argument.BOOLEAN, Key.throwOnError, false ),
		};
	}

	/**
	 * Canonicalize or decode the input string. Canonicalization is simply the operation of reducing a possibly encoded string down to its simplest form.
	 * This is important because attackers frequently use encoding to change their input in a way that will bypass validation filters,
	 * but still be interpreted properly by the target of the attack.
	 * <p>
	 * Note that data encoded more than once is not something that a normal user would generate and should be regarded as an attack.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.input The input string to be canonicalized.
	 *
	 * @argument.restrictMultiple If set to true, multiple encoding is restricted. This argument can be set to true to restrict the input if multiple or
	 *                            nested encoding is detected. If this argument is set to true, and the given input is multiple or nested encoded using
	 *                            one encoding scheme an error will be thrown.
	 *
	 * @argument.restrictMixed If set to true, mixed encoding is restricted. This argument can be set to true to restrict the input if mixed encoding is
	 *
	 * @argument.throwOnError If set to true, an error will be thrown if the input is not valid. If set to false, the input will be returned as is.
	 *
	 * @return The canonicalized string.
	 */
	public Object _invoke( IBoxContext context, ArgumentsScope arguments ) {
		String	input				= arguments.getAsString( Key.input ).trim();
		boolean	restrictMultiple	= arguments.getAsBoolean( KeyDirectory.restrictMultiple );
		boolean	restrictMixed		= arguments.getAsBoolean( KeyDirectory.restrictMixed );
		boolean	throwOnError		= arguments.getAsBoolean( Key.throwOnError );
		String	result				= "";
		Encoder	encoder				= ESAPI.encoder();

		// Check input length,
		if ( input.isEmpty() ) {
			return result;
		}

		try {
			result = encoder.canonicalize( input, restrictMultiple, restrictMixed );
		} catch ( Exception e ) {
			if ( throwOnError ) {
				throw new BoxRuntimeException( "Error canonicalizing input: " + e.getMessage(), e );
			}
		}

		return result;
	}
}
