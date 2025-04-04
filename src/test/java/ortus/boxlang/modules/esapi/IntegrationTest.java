package ortus.boxlang.modules.esapi;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.services.FunctionService;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;

/**
 * This loads the module and runs an integration test on the module.
 */
public class IntegrationTest extends BaseIntegrationTest {

	@DisplayName( "Test the module loads in BoxLang" )
	@Test
	public void testModuleLoads() {
		FunctionService functionService = BoxRuntime.getInstance().getFunctionService();

		// Then
		assertThat( moduleService.getRegistry().containsKey( moduleName ) ).isTrue();

		// Verify things got registered
		IStruct map = Struct.of(
		    "EncodeFor", true,
		    "EncodeForSQL", true,
		    "encodeForCSS", true,
		    "encodeForDN", true,
		    "encodeForHTML", true,
		    "encodeForHTMLAttribute", true,
		    "encodeForJavaScript", true,
		    "encodeForLDAP", true,
		    "encodeForURL", true,
		    "encodeForXML", true,
		    "encodeForXMLAttribute", true,
		    "encodeForXPath", true
		);

		// Iterate over the map and assert
		map.forEach( ( key, value ) -> {
			assertThat( functionService.hasGlobalFunction( Key.of( key ) ) ).isTrue();
		} );

	}
}
