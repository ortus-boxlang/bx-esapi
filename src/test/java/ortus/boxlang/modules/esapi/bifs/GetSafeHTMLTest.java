package ortus.boxlang.modules.esapi.bifs;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.modules.esapi.BaseIntegrationTest;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class GetSafeHTMLTest extends BaseIntegrationTest {

	@DisplayName( "It can sanitize html using the default policy" )
	@Test
	public void testDefaultPolicy() {
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

	@DisplayName( "It returns clean HTML silently when throwOnError is false" )
	@Test
	public void testThrowOnErrorFalse() {
		runtime.executeSource(
		    """
		    	result = getSafeHTML( "<b>hello</b><script>alert('xss')</script>", "ebay", false );
		    """,
		    context
		);

		assertThat( variables.getAsString( result ) ).isEqualTo( "<b>hello</b>" );
	}

	@DisplayName( "It throws when throwOnError is true and violations exist" )
	@Test
	public void testThrowOnErrorTrue() {
		assertThrows( BoxRuntimeException.class, () -> runtime.executeSource(
		    """
		    	result = getSafeHTML( "<script>alert('xss')</script>", "ebay", true );
		    """,
		    context
		) );
	}

	@DisplayName( "It does not throw when throwOnError is true and HTML is clean" )
	@Test
	public void testThrowOnErrorTrueCleanInput() {
		runtime.executeSource(
		    """
		    	result = getSafeHTML( "<b>hello</b>", "ebay", true );
		    """,
		    context
		);

		assertThat( variables.getAsString( result ) ).isEqualTo( "<b>hello</b>" );
	}

	@DisplayName( "It can use throwOnError with a struct policy" )
	@Test
	public void testThrowOnErrorWithStructPolicy() {
		assertThrows( BoxRuntimeException.class, () -> runtime.executeSource(
		    """
		    	result = getSafeHTML( "<script>alert('xss')</script>", {
		    		basePolicy: "ebay"
		    	}, true );
		    """,
		    context
		) );
	}

	@DisplayName( "It can override directives via a struct policy with basePolicy" )
	@Test
	public void testStructPolicyWithDirectiveOverrides() {
		// Build a safe HTML string just over the 20,000-character default
		String largeInput = "<b>" + "a".repeat( 20_001 ) + "</b>";

		// Without override it should throw due to the default 20k limit
		variables.put( Key.of( "input" ), largeInput );
		assertThrows( BoxRuntimeException.class, () -> runtime.executeSource(
		    "result = getSafeHTML( input );",
		    context
		) );

		// With maxInputSize raised via a struct policy it should succeed
		runtime.executeSource(
		    """
		    	result = getSafeHTML( input, {
		    		basePolicy: "ebay",
		    		directives: { maxInputSize: 100000 }
		    	} );
		    """,
		    context
		);

		assertThat( variables.getAsString( result ) ).isEqualTo( largeInput );
	}

	@DisplayName( "It can merge tag rules into a base policy" )
	@Test
	public void testStructPolicyMergeTagRules() {
		// The ebay policy allows <b> but removes <script>
		// We'll override <b> to "remove" and verify it gets removed
		runtime.executeSource(
		    """
		    	result = getSafeHTML( "<b>hello</b>", {
		    		basePolicy: "ebay",
		    		tagRules: { "b": "remove" }
		    	} );
		    """,
		    context
		);

		assertThat( variables.getAsString( result ) ).isEqualTo( "" );
	}

	@DisplayName( "It can build a policy from scratch with a struct" )
	@Test
	public void testStructPolicyFromScratch() {
		// From scratch: only allow <b> and <i> tags
		runtime.executeSource(
		    """
		    	result = getSafeHTML( "<b>bold</b> <i>italic</i> <script>evil</script> <div>content</div>", {
		    		directives: { maxInputSize: 100000 },
		    		allowTags: [ "b", "i" ]
		    	} );
		    """,
		    context
		);

		String output = variables.getAsString( result );
		assertThat( output ).contains( "<b>bold</b>" );
		assertThat( output ).contains( "<i>italic</i>" );
		assertThat( output ).doesNotContain( "<script>" );
		assertThat( output ).doesNotContain( "<div>" );
	}

	@DisplayName( "It can use allowTags sugar alongside explicit tagRules" )
	@Test
	public void testAllowTagsWithTagRules() {
		// allowTags adds "validate", tagRules wins for overlapping tags
		runtime.executeSource(
		    """
		    	result = getSafeHTML( "<b>bold</b> <i>italic</i> <u>underline</u>", {
		    		allowTags: [ "b", "i", "u" ],
		    		tagRules: { "u": "remove" }
		    	} );
		    """,
		    context
		);

		String output = variables.getAsString( result );
		assertThat( output ).contains( "<b>bold</b>" );
		assertThat( output ).contains( "<i>italic</i>" );
		assertThat( output ).doesNotContain( "<u>" );
	}

	@DisplayName( "It can configure tag attributes in a struct policy" )
	@Test
	public void testStructPolicyWithTagAttributes() {
		runtime.executeSource(
		    """
		    	result = getSafeHTML( '<a href="https://example.com">link</a>', {
		    		tagRules: {
		    			"a": {
		    				action: "validate",
		    				attributes: {
		    					"href": {
		    						regexps: [ "https?://[^\\s]*" ]
		    					}
		    				}
		    			}
		    		}
		    	} );
		    """,
		    context
		);

		String output = variables.getAsString( result );
		assertThat( output ).contains( "href" );
		assertThat( output ).contains( "example.com" );
	}

	@DisplayName( "It can use override mode to replace entire sections" )
	@Test
	public void testOverrideMode() {
		// Override mode: only the tags we specify exist, base policy tags are gone
		runtime.executeSource(
		    """
		    	result = getSafeHTML( "<b>bold</b> <i>italic</i>", {
		    		basePolicy: "ebay",
		    		overrideMode: "override",
		    		tagRules: { "b": "validate" }
		    	} );
		    """,
		    context
		);

		String output = variables.getAsString( result );
		assertThat( output ).contains( "<b>bold</b>" );
		// <i> should be removed since we overrode the entire tag-rules section
		assertThat( output ).doesNotContain( "<i>" );
	}

	@DisplayName( "Struct policy matches slashdot XML policy output" )
	@Test
	public void testStructParityWithSlashdot() {
		String input = "<b>bold</b> <i>italic</i> <em>emphasis</em> <strong>strong</strong> "
		    + "<a href=\"https://example.com\">link</a> <script>alert('xss')</script> "
		    + "<div>content</div> <p>paragraph</p> <ul><li>item</li></ul> "
		    + "<blockquote>quote</blockquote> <br> <tt>teletype</tt> "
		    + "<style>body{color:red}</style> <iframe src=\"evil\"></iframe> "
		    + "<img src=\"photo.jpg\"> <table><tr><td>cell</td></tr></table>";

		variables.put( Key.of( "input" ), input );

		// Run with the named XML policy
		runtime.executeSource(
		    "xmlResult = getSafeHTML( input, 'slashdot' );",
		    context
		);
		String xmlResult = variables.getAsString( Key.of( "xmlResult" ) );

		// Run with the struct equivalent
		runtime.executeSource(
		    """
		    structResult = getSafeHTML( input, {
		    	basePolicy: "slashdot",
		    	directives: {
		    		omitXmlDeclaration: "true",
		    		omitDoctypeDeclaration: "true",
		    		maxInputSize: 5000,
		    		formatOutput: "true",
		    		embedStyleSheets: "false"
		    	}
		    } );
		    """,
		    context
		);
		String structResult = variables.getAsString( Key.of( "structResult" ) );

		assertThat( structResult ).isEqualTo( xmlResult );
	}

	@DisplayName( "Struct policy matches ebay XML policy for basic HTML" )
	@Test
	public void testStructParityWithEbay() {
		String input = "<b>bold</b> <i>italic</i> <em>emphasis</em> <strong>strong</strong> "
		    + "<script>alert('xss')</script> <h1>heading</h1> <h2>heading2</h2> "
		    + "<p>paragraph</p> <br> <hr> <pre>preformatted</pre> <code>code</code> "
		    + "<ul><li>item</li></ul> <ol><li>item</li></ol> "
		    + "<blockquote>quote</blockquote> <sub>sub</sub> <sup>sup</sup> "
		    + "<u>underline</u> <strike>struck</strike> <center>centered</center>";

		variables.put( Key.of( "input" ), input );

		// Run with the named XML policy
		runtime.executeSource(
		    "xmlResult = getSafeHTML( input, 'ebay' );",
		    context
		);
		String xmlResult = variables.getAsString( Key.of( "xmlResult" ) );

		// Run with struct that just references the base policy (no overrides)
		runtime.executeSource(
		    """
		    structResult = getSafeHTML( input, {
		    	basePolicy: "ebay"
		    } );
		    """,
		    context
		);
		String structResult = variables.getAsString( Key.of( "structResult" ) );

		assertThat( structResult ).isEqualTo( xmlResult );
	}

	@DisplayName( "Struct policy matches ebay XML policy for tables and images" )
	@Test
	public void testStructParityWithEbayTablesAndImages() {
		String input = "<table border=\"1\"><thead><tr><th>Header</th></tr></thead>"
		    + "<tbody><tr><td>Cell</td></tr></tbody></table> "
		    + "<img src=\"https://example.com/photo.jpg\" alt=\"A photo\" width=\"100\" height=\"50\"> "
		    + "<a href=\"https://example.com\" rel=\"nofollow\">link</a> "
		    + "<font color=\"red\" size=\"3\" face=\"Arial\">styled</font> "
		    + "<div align=\"center\">centered div</div> <span>span</span>";

		variables.put( Key.of( "input" ), input );

		// Run with the named XML policy
		runtime.executeSource(
		    "xmlResult = getSafeHTML( input, 'ebay' );",
		    context
		);
		String xmlResult = variables.getAsString( Key.of( "xmlResult" ) );

		// Run with struct that just references the base policy (no overrides)
		runtime.executeSource(
		    """
		    structResult = getSafeHTML( input, {
		    	basePolicy: "ebay"
		    } );
		    """,
		    context
		);
		String structResult = variables.getAsString( Key.of( "structResult" ) );

		assertThat( structResult ).isEqualTo( xmlResult );
	}

	@DisplayName( "Struct policy matches slashdot XML policy for links with attributes" )
	@Test
	public void testStructParityWithSlashdotLinks() {
		String input = "<a href=\"https://example.com\" rel=\"nofollow\">safe link</a> "
		    + "<a href=\"javascript:alert('xss')\">bad link</a> "
		    + "<b>bold</b> <i>italic</i> <em>emphasis</em> "
		    + "<p align=\"center\">centered paragraph</p>";

		variables.put( Key.of( "input" ), input );

		// Run with the named XML policy
		runtime.executeSource(
		    "xmlResult = getSafeHTML( input, 'slashdot' );",
		    context
		);
		String xmlResult = variables.getAsString( Key.of( "xmlResult" ) );

		// Run with struct that just references the base policy (no overrides)
		runtime.executeSource(
		    """
		    structResult = getSafeHTML( input, {
		    	basePolicy: "slashdot"
		    } );
		    """,
		    context
		);
		String structResult = variables.getAsString( Key.of( "structResult" ) );

		assertThat( structResult ).isEqualTo( xmlResult );
	}

}
