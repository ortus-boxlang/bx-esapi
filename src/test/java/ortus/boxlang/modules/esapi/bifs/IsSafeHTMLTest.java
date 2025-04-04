package ortus.boxlang.modules.esapi.bifs;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.modules.esapi.BaseIntegrationTest;

public class IsSafeHTMLTest extends BaseIntegrationTest {

	@DisplayName( "It can use verify if html is safe using the default policy" )
	@Test
	public void testIsSafeHTML() {
		runtime.executeSource(
		    """
		    	result = IsSafeHTML( "<b>hello</b>" );
		    """,
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( true );

		// Test with an unsafe html
		runtime.executeSource(
		    """
		    	result = IsSafeHTML( "<script>alert('hello');</script>" );
		    """,
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( false );
	}

	@DisplayName( "It can validate with a explicit policy" )
	@Test
	public void testIsSafeHTMLWithPolicy() {
		runtime.executeSource(
		    """
		    	result = IsSafeHTML( "<b>hello</b>", "myspace" );
		    """,
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( true );

		// Test with an unsafe html
		runtime.executeSource(
		    """
		    	result = IsSafeHTML( "<script>alert('hello');</script>", "myspace" );
		    """,
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( false );
	}

}
