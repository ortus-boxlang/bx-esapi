package ortus.boxlang.modules.esapi.bifs;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;

public class IsSafeHTMLTest {

	static BoxRuntime	instance;
	IBoxContext			context;
	IScope				variables;
	static Key			result	= new Key( "result" );

	@BeforeAll
	public static void setUp() {
		instance = BoxRuntime.getInstance( true );
	}

	@BeforeEach
	public void setupEach() {
		context		= new ScriptingRequestBoxContext( instance.getRuntimeContext() );
		variables	= context.getScopeNearby( VariablesScope.name );
	}

	@DisplayName( "It can use verify if html is safe using the default policy" )
	@Test
	public void testIsSafeHTML() {
		instance.executeSource(
		    """
		    	result = IsSafeHTML( "<b>hello</b>" );
		    """,
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( true );

		// Test with an unsafe html
		instance.executeSource(
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
		instance.executeSource(
		    """
		    	result = IsSafeHTML( "<b>hello</b>", "myspace" );
		    """,
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( true );

		// Test with an unsafe html
		instance.executeSource(
		    """
		    	result = IsSafeHTML( "<script>alert('hello');</script>", "myspace" );
		    """,
		    context
		);

		assertThat( variables.get( result ) ).isEqualTo( false );
	}

}
