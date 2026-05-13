package ortus.boxlang.modules.esapi.util;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.owasp.validator.html.Policy;

import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;

public class AntiSamyUtilCacheTest {

	@AfterEach
	public void cleanupCache() {
		AntiSamyUtil.clearPolicyCache();
	}

	@DisplayName( "buildPolicyFromStruct caches and reuses a policy for the same struct instance" )
	@Test
	public void testCacheHitForSameStructInstance() {
		IStruct config = Struct.of(
		    "basePolicy", "ebay",
		    "directives", Struct.of(
		        "maxInputSize", "100000",
		        "embedStyleSheets", "false"
		    )
		);

		Policy first = AntiSamyUtil.buildPolicyFromStruct( config );
		Policy second = AntiSamyUtil.buildPolicyFromStruct( config );

		assertThat( second ).isSameInstanceAs( first );
	}

	@DisplayName( "buildPolicyFromStruct caches equivalent configs even when nested key ordering differs" )
	@Test
	public void testCacheHitForEquivalentStructsWithDifferentOrdering() {
		IStruct tagRuleAttrsA = Struct.of(
		    "href", Struct.of(
		        "description", "link href",
		        "onInvalid", "removeAttribute"
		    )
		);
		IStruct tagRuleAttrsB = Struct.of(
		    "href", Struct.of(
		        "onInvalid", "removeAttribute",
		        "description", "link href"
		    )
		);

		IStruct configA = Struct.of(
		    "basePolicy", "ebay",
		    "overrideMode", "merge",
		    "directives", Struct.of(
		        "maxInputSize", "100000",
		        "embedStyleSheets", "false"
		    ),
		    "tagRules", Struct.of(
		        "a", Struct.of(
		            "action", "validate",
		            "attributes", tagRuleAttrsA
		        ),
		        "b", "validate"
		    )
		);

		IStruct configB = Struct.of(
		    "tagRules", Struct.of(
		        "b", "validate",
		        "a", Struct.of(
		            "attributes", tagRuleAttrsB,
		            "action", "validate"
		        )
		    ),
		    "directives", Struct.of(
		        "embedStyleSheets", "false",
		        "maxInputSize", "100000"
		    ),
		    "overrideMode", "merge",
		    "basePolicy", "ebay"
		);

		Policy first = AntiSamyUtil.buildPolicyFromStruct( configA );
		Policy second = AntiSamyUtil.buildPolicyFromStruct( configB );

		assertThat( second ).isSameInstanceAs( first );
	}

	@DisplayName( "clearPolicyCache forces policy rebuild" )
	@Test
	public void testClearPolicyCacheForcesRebuild() {
		IStruct config = Struct.of(
		    "basePolicy", "ebay",
		    "directives", Struct.of(
		        "maxInputSize", "100000"
		    )
		);

		Policy first = AntiSamyUtil.buildPolicyFromStruct( config );
		AntiSamyUtil.clearPolicyCache();
		Policy second = AntiSamyUtil.buildPolicyFromStruct( config );

		assertThat( second ).isNotSameInstanceAs( first );
	}
}
