package ortus.boxlang.modules.esapi.bifs;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.modules.esapi.BaseIntegrationTest;
import ortus.boxlang.modules.esapi.util.AntiSamyUtil;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;

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

	@DisplayName( "It can validate with a struct policy using basePolicy" )
	@Test
	public void testIsSafeHTMLWithStructPolicy() {
		// Safe HTML with a struct policy based on ebay
		runtime.executeSource(
		    """
		    	result = IsSafeHTML( "<b>hello</b>", {
		    		basePolicy: "ebay"
		    	} );
		    """,
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( true );

		// Unsafe HTML should still be detected
		runtime.executeSource(
		    """
		    	result = IsSafeHTML( "<script>alert('hello');</script>", {
		    		basePolicy: "ebay"
		    	} );
		    """,
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( false );
	}

	@DisplayName( "It can validate with a struct policy using override mode" )
	@Test
	public void testIsSafeHTMLWithOverridePolicy() {
		// Override ebay's tag rules to only allow <b> — <b> should be safe
		runtime.executeSource(
		    """
		    	result = IsSafeHTML( "<b>hello</b>", {
		    		overrideMode: "override",
		    		tagRules: { "b": "validate" }
		    	} );
		    """,
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( true );

		// <i> is not in our override so it should be unsafe
		runtime.executeSource(
		    """
		    	result = IsSafeHTML( "<i>hello</i>", {
		    		overrideMode: "override",
		    		tagRules: { "b": "validate" }
		    	} );
		    """,
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( false );
	}

	@DisplayName( "It can force struct policy recreation by evicting cache" )
	@Test
	public void testForceStructPolicyRecreation() {
		IStruct policyConfig = Struct.of(
		    "basePolicy", "ebay",
		    "directives", Struct.of( "maxInputSize", "100000" )
		);
		AntiSamyUtil.clearPolicyCache();
		assertThat( AntiSamyUtil.getPolicyCacheSize() ).isEqualTo( 0 );
		AntiSamyUtil.buildPolicyFromStruct( policyConfig );
		assertThat( AntiSamyUtil.getPolicyCacheSize() ).isEqualTo( 1 );

		variables.put( Key.of( "policyConfig" ), policyConfig );
		runtime.executeSource(
		    "result = isSafeHTML( '<b>hello</b>', policyConfig, true );",
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( true );
		assertThat( AntiSamyUtil.getPolicyCacheSize() ).isEqualTo( 1 );
	}

}
