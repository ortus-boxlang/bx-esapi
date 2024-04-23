/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package ortus.boxlang.modules.esapi.bifs;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

public class ESAPITest {

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

	@DisplayName( "It can encode for CSS" )
	@Test
	public void testEncodeForCSS() {
		instance.executeSource( "result = encodeForCSS( '<test>', false )", context );
		assertEquals( "\\3c test\\3e ", variables.get( result ) );
	}

	@DisplayName( "It can encode for DN" )
	@Test
	public void testEncodeForDN() {
		instance.executeSource( "result = encodeForDN( 'x,y', false )", context );
		assertEquals( "x\\,y", variables.get( result ) );
	}

	@DisplayName( "It can encode for HTML" )
	@Test
	public void testEncodeForHTML() {
		instance.executeSource( "result = encodeForHTML( '<test>', false )", context );
		assertEquals( "&lt;test&gt;", variables.get( result ) );
	}

	@DisplayName( "It can encode for HTML attribute" )
	@Test
	public void testEncodeForHTMLAttribute() {
		instance.executeSource( "result = encodeForHTMLAttribute( '<test>', false )", context );
		assertEquals( "&lt;test&gt;", variables.get( result ) );
	}

	@DisplayName( "It can encode for JavaScript" )
	@Test
	public void testEncodeForJavaScript() {
		instance.executeSource( "result = encodeForJavaScript( 'foo()', false )", context );
		assertEquals( "foo\\x28\\x29", variables.get( result ) );
	}

	@DisplayName( "It can encode for LDAP" )
	@Test
	public void testEncodeForLDAP() {
		instance.executeSource( "result = encodeForLDAP('grant) (| (password = * ) )')", context );
		assertEquals( "grant\\29 \\28| \\28password = \\2a \\29 \\29", variables.get( result ) );
	}

	@DisplayName( "It can encode for URL" )
	@Test
	public void testEncodeForURL() {
		instance.executeSource( "result = encodeForURL( 'http://www.example.com/?foo=bar', false )", context );
		assertEquals( "http%3A%2F%2Fwww.example.com%2F%3Ffoo%3Dbar", variables.get( result ) );
	}

	@DisplayName( "It can encode for XML" )
	@Test
	public void testEncodeForXML() {
		instance.executeSource( "result = encodeForXML( '<test>', false )", context );
		assertEquals( "&#x3c;test&#x3e;", variables.get( result ) );
	}

	@DisplayName( "It can encode for XML attribute" )
	@Test
	public void testEncodeForXMLAttribute() {
		instance.executeSource( "result = encodeForXMLAttribute(\"It's for use in attribute values\")", context );
		assertEquals( "It&#x27;s for use in attribute values", variables.get( result ) );
	}

	@DisplayName( "It can encode for XPath" )
	@Test
	public void testEncodeForXPath() {
		instance.executeSource( "result = encodeForXPath( \"' or 1=1\" )", context );
		assertEquals( "&#x27; or 1&#x3d;1", variables.get( result ) );
	}

	@DisplayName( "It can encode for SQL" )
	@Test
	public void testEncodeForSQL() {
		instance.executeSource( "result = encodeForSQL( \"' or '1'='1\", 'mysql', false )", context );
		assertEquals( "\\' or \\'1\\'\\=\\'1", variables.get( result ) );
	}

	@DisplayName( "It can encodeFor" )
	@Test
	public void testEncodeFor() {
		instance.executeSource( "result = encodeFor( 'html', '<test>' )", context );
		assertEquals( "&lt;test&gt;", variables.get( result ) );
	}
}
