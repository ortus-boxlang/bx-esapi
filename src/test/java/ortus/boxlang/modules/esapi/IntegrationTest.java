package ortus.boxlang.modules.esapi;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.modules.ModuleRecord;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;
import ortus.boxlang.runtime.services.FunctionService;
import ortus.boxlang.runtime.services.ModuleService;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;

/**
 * This loads the module and runs an integration test on the module.
 */
public class IntegrationTest {

	@DisplayName( "Test the module loads in BoxLang" )
	@Test
	public void testModuleLoads() {
		// Given
		Key				moduleName		= new Key( "bx-esapi" );
		String			physicalPath	= Paths.get( "./build/module" ).toAbsolutePath().toString();
		ModuleRecord	moduleRecord	= new ModuleRecord( physicalPath );
		IBoxContext		context			= new ScriptingRequestBoxContext();
		BoxRuntime		runtime			= BoxRuntime.getInstance( true );
		ModuleService	moduleService	= runtime.getModuleService();
		FunctionService	functionService	= runtime.getFunctionService();
		IScope			variables		= context.getScopeNearby( VariablesScope.name );

		// When
		moduleRecord
		    .loadDescriptor( context )
		    .register( context )
		    .activate( context );

		moduleService.getRegistry().put( moduleName, moduleRecord );

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

		// @formatter:off
		runtime.executeSource(
		    """
			// Testing code here
			""",
		    context
		);
		// @formatter:on

		// Asserts here

	}
}
