package ortus.boxlang.modules.esapi.bifs;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.modules.esapi.BaseIntegrationTest;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class GetSafeHTMLTest extends BaseIntegrationTest {

	@DisplayName( "It can use verify if html is safe using the default policy" )
	@Test
	public void testIsSafeHTML() {
		runtime.executeSource(
		    """
		    	result = getSafeHTML( "<b>hello</b>" );
		    """,
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( "<b>hello</b>" );

		// Test with an unsafe html
		runtime.executeSource(
		    """
		    	result = getSafeHTML( "<script>alert('hello');</script>" );
		    """,
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( "" );
	}

	@DisplayName( "It respects directive overrides passed as a struct" )
	@Test
	public void testGetSafeHTMLWithDirectives() {
		// Build a safe HTML string just over the 20,000-character default
		String largeInput = "<b>" + "a".repeat( 20_001 ) + "</b>";

		// Without directives override it should throw due to the default 20k limit
		variables.put( Key.of( "input" ), largeInput );
		assertThrows( BoxRuntimeException.class, () -> runtime.executeSource(
		    "result = getSafeHTML( input );",
		    context
		) );

		// With maxInputSize raised via a directives struct it should succeed
		runtime.executeSource(
		    """
		    	result = getSafeHTML( input, "ebay", { maxInputSize: 100000 } );
		    """,
		    context
		);

		assertThat( variables.getAsString( result ) ).isEqualTo( largeInput );
	}

}
