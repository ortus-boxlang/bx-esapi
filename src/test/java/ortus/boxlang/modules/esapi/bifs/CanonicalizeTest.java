package ortus.boxlang.modules.esapi.bifs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class CanonicalizeTest {

	static BoxRuntime	instance;
	IBoxContext			context;
	IScope				variables;
	static Key			result	= new Key( "result" );

	@BeforeAll
	public static void setUp() {
		instance = BoxRuntime.getInstance( true );
	}

	@BeforeEach
	public void setupEach() {
		context		= new ScriptingRequestBoxContext( instance.getRuntimeContext() );
		variables	= context.getScopeNearby( VariablesScope.name );
	}

	@DisplayName( "It can use canonicalize" )
	@Test
	public void testCanonicalize() {
		instance.executeSource(
		    "result = canonicalize( '&lt;', false, false )",
		    context
		);
		assertEquals( "<", variables.get( result ) );
	}

	@DisplayName( "It can use canonicalize and throw an exception" )
	@Test
	public void testCanonicalizeThrowException() {
		assertThrows( BoxRuntimeException.class, () -> {
			instance.executeSource(
			    "result = canonicalize( '%26lt; %26lt; %2526lt%253B %2526lt%253B %2526lt%253B', true, true, true )",
			    context
			);
		} );
	}

	@DisplayName( "Enforce multiple and mixed encoding detection" )
	@Test
	public void testCanonicalizeEnforceMultipleAndMixedEncodingDetection() {

		instance.executeSource(
		    "result = canonicalize('%26lt; %26lt; %2526lt%253B %2526lt%253B %2526lt%253B', true, true )",
		    context
		);
		assertEquals( "", variables.get( result ) );
	}

}
