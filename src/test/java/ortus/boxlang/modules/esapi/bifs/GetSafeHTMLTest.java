package ortus.boxlang.modules.esapi.bifs;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.modules.esapi.BaseIntegrationTest;

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

}
