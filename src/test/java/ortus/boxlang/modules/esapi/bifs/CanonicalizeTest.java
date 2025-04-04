package ortus.boxlang.modules.esapi.bifs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.modules.esapi.BaseIntegrationTest;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class CanonicalizeTest extends BaseIntegrationTest {

	@DisplayName( "It can use canonicalize" )
	@Test
	public void testCanonicalize() {
		runtime.executeSource(
		    "result = canonicalize( '&lt;', false, false )",
		    context
		);
		assertEquals( "<", variables.get( result ) );
	}

	@DisplayName( "It can use canonicalize and throw an exception" )
	@Test
	public void testCanonicalizeThrowException() {
		assertThrows( BoxRuntimeException.class, () -> {
			runtime.executeSource(
			    "result = canonicalize( '%26lt; %26lt; %2526lt%253B %2526lt%253B %2526lt%253B', true, true, true )",
			    context
			);
		} );
	}

	@DisplayName( "Enforce multiple and mixed encoding detection" )
	@Test
	public void testCanonicalizeEnforceMultipleAndMixedEncodingDetection() {

		runtime.executeSource(
		    "result = canonicalize('%26lt; %26lt; %2526lt%253B %2526lt%253B %2526lt%253B', true, true )",
		    context
		);
		assertEquals( "", variables.get( result ) );
	}

}
