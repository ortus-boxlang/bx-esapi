package ortus.boxlang.modules.esapi.bifs;

import java.util.List;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import ortus.boxlang.modules.esapi.util.KeyDirectory;
import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.bifs.BoxMember;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.BoxLangType;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

@BoxBIF
@BoxMember( type = BoxLangType.STRING )
public class SanitizeHTML extends BIF {

	private static final List<String>	AVAILABLE_POLICIES	= List.of(
	    "BLOCKS",
	    "FORMATTING",
	    "IMAGES",
	    "LINKS",
	    "STYLES",
	    "TABLES"
	);

	private static final PolicyFactory	POLICY_ALL			= Sanitizers.FORMATTING
	    .and( Sanitizers.BLOCKS )
	    .and( Sanitizers.STYLES )
	    .and( Sanitizers.LINKS )
	    .and( Sanitizers.TABLES )
	    .and( Sanitizers.IMAGES );

	/**
	 * Constructor
	 */
	public SanitizeHTML() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, Argument.STRING, Key.string ),
		    new Argument( false, Argument.STRING, KeyDirectory.policy, "" )
		};
	}

	/**
	 * Sanitizes unsafe HTML to protect against XSS attacks using the OWASP Java HTML Sanitizer.
	 * <p>
	 * The policy can be one of the following:
	 * <ul>
	 * <li>blocks</li>
	 * <li>formatting</li>
	 * <li>images</li>
	 * <li>links</li>
	 * <li>styles</li>
	 * <li>tables</li>
	 * </ul>
	 * <p>
	 * If no policy is provided, all policies are used.
	 * <p>
	 * You can also provide a OWASP {@link PolicyFactory} object to use a custom policy.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.string The string to sanitize.
	 *
	 * @argument.policy The policy to use for sanitization. If not provided, all policies are used. This can also be a ${link PolicyFactory} object.
	 *
	 * @throws BoxRuntimeException If the policy does not exist.
	 *
	 * @return The sanitized HTML string.
	 */
	public Object _invoke( IBoxContext context, ArgumentsScope arguments ) {
		String	unsafeString	= arguments.getAsString( Key.string ).trim();
		Object	policy			= arguments.getAsString( KeyDirectory.policy );

		// If it's a string, then try to match it
		if ( policy instanceof String castedPolicy ) {
			// Do we have a policy, else use it all
			if ( castedPolicy.isEmpty() ) {
				return POLICY_ALL.sanitize( unsafeString );
			}

			// Check if the policy is available
			if ( AVAILABLE_POLICIES.contains( castedPolicy ) ) {
				// Switch on the policy and sanitize
				switch ( castedPolicy.toUpperCase() ) {
					case "BLOCKS" :
						return Sanitizers.BLOCKS.sanitize( unsafeString );
					case "FORMATTING" :
						return Sanitizers.FORMATTING.sanitize( unsafeString );
					case "IMAGES" :
						return Sanitizers.IMAGES.sanitize( unsafeString );
					case "LINKS" :
						return Sanitizers.LINKS.sanitize( unsafeString );
					case "STYLES" :
						return Sanitizers.STYLES.sanitize( unsafeString );
					case "TABLES" :
						return Sanitizers.TABLES.sanitize( unsafeString );
					default :
						throw new BoxRuntimeException( "Policy '" + policy + "' does not exist. Available policies are: " + AVAILABLE_POLICIES );
				}
			}
		}
		// Else we passed a policy factory
		else if ( policy instanceof PolicyFactory ) {
			return ( ( PolicyFactory ) policy ).sanitize( unsafeString );
		}

		throw new BoxRuntimeException( "The policy you sent is invalid: " + policy.getClass().getName() );
	}

}
