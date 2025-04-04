package ortus.boxlang.modules.esapi.bifs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.modules.esapi.BaseIntegrationTest;

public class SanitizeHTMLTest extends BaseIntegrationTest {

	@DisplayName( "It sanitize using all policies" )
	@Test
	public void testAllPolicies() {
		runtime.executeSource(
		    """
		    result = sanitizeHTML( '<p>a <strong>link</strong> <a href="https://www.example.com" onClick="doSomethingBad()">test</a></p>' )
		          """,
		    context
		);
		assertEquals(
		    "<p>a <strong>link</strong> <a href=\"https://www.example.com\" rel=\"nofollow\">test</a></p>",
		    variables.get( result )
		);
	}

}
