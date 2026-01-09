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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.modules.esapi.BaseIntegrationTest;

public class ESAPITest extends BaseIntegrationTest {

	@DisplayName( "It can encode for CSS" )
	@Test
	public void testEncodeForCSS() {
		runtime.executeSource( "result = encodeForCSS( '<test>', false )", context );
		assertEquals( "\\3c test\\3e ", variables.get( result ) );
	}

	@DisplayName( "It can encode for DN" )
	@Test
	public void testEncodeForDN() {
		runtime.executeSource( "result = encodeForDN( 'x,y', false )", context );
		assertEquals( "x\\,y", variables.get( result ) );
	}

	@DisplayName( "It can encode for HTML" )
	@Test
	public void testEncodeForHTML() {
		runtime.executeSource( "result = encodeForHTML( '<test>', false )", context );
		assertEquals( "&lt;test&gt;", variables.get( result ) );
	}

	@DisplayName( "EncodeForX methods return nulls unmodified" )
	@Test
	public void testEncodeForHTMLWithNull() {
		runtime.executeSource( "result = encodeForHTML( null )", context );
		assertNull( variables.get( result ) );
	}

	@DisplayName( "It can encode for HTML attribute" )
	@Test
	public void testEncodeForHTMLAttribute() {
		runtime.executeSource( "result = encodeForHTMLAttribute( '<test>', false )", context );
		assertEquals( "&lt;test&gt;", variables.get( result ) );

		runtime.executeSource( "result = '<test>'.encodeForHTMLAttribute()", context );
		assertEquals( "&lt;test&gt;", variables.get( result ) );
	}

	@DisplayName( "It can encode for JavaScript" )
	@Test
	public void testEncodeForJavaScript() {
		runtime.executeSource( "result = encodeForJavaScript( 'foo()', false )", context );
		assertEquals( "foo\\x28\\x29", variables.get( result ) );
	}

	@DisplayName( "It can encode for LDAP" )
	@Test
	public void testEncodeForLDAP() {
		runtime.executeSource( "result = encodeForLDAP('grant) (| (password = * ) )')", context );
		assertEquals( "grant\\29 \\28| \\28password = \\2a \\29 \\29", variables.get( result ) );
	}

	@DisplayName( "It can encode for URL" )
	@Test
	public void testEncodeForURL() {
		runtime.executeSource( "result = encodeForURL( 'http://www.example.com/?foo=bar', false )", context );
		assertEquals( "http%3A%2F%2Fwww.example.com%2F%3Ffoo%3Dbar", variables.get( result ) );
	}

	@DisplayName( "It can encode for URL using the Lucee alias" )
	@Test
	public void testURLEncode() {
		runtime.executeSource( "result = URLEncode( 'http://www.example.com/?foo=bar', false )", context );
		assertEquals( "http%3A%2F%2Fwww.example.com%2F%3Ffoo%3Dbar", variables.get( result ) );
	}

	@DisplayName( "It can encode for XML" )
	@Test
	public void testEncodeForXML() {
		runtime.executeSource( "result = encodeForXML( '<test>', false )", context );
		assertEquals( "&#x3c;test&#x3e;", variables.get( result ) );
	}

	@DisplayName( "It can encode for XML attribute" )
	@Test
	public void testEncodeForXMLAttribute() {
		runtime.executeSource( "result = encodeForXMLAttribute(\"It's for use in attribute values\")", context );
		assertEquals( "It&#x27;s for use in attribute values", variables.get( result ) );
	}

	@DisplayName( "It can encode for XPath" )
	@Test
	public void testEncodeForXPath() {
		runtime.executeSource( "result = encodeForXPath( \"' or 1=1\" )", context );
		assertEquals( "&#x27; or 1&#x3d;1", variables.get( result ) );
	}

	@DisplayName( "It can encode for SQL" )
	@Test
	public void testEncodeForSQL() {
		runtime.executeSource( "result = encodeForSQL( \"' or '1'='1\", 'mysql', false )", context );
		assertEquals( "\\' or \\'1\\'\\=\\'1", variables.get( result ) );
	}

	@DisplayName( "It can encodeFor" )
	@Test
	public void testEncodeFor() {
		runtime.executeSource( "result = encodeFor( 'html', '<test>' )", context );
		assertEquals( "&lt;test&gt;", variables.get( result ) );
	}

	@DisplayName( "It can encodeFor with an empty value" )
	@Test
	public void testEncodeForWithEmptyValue() {
		runtime.executeSource( "result = encodeFor( 'html', '' )", context );
		assertEquals( "", variables.get( result ) );
	}

	@DisplayName( "It can decodeFromURL" )
	@Test
	public void testDecodeFromURL() {
		runtime.executeSource( "result = decodeFromURL( 'http%3A%2F%2Fwww.example.com%2F%3Ffoo%3Dbar' )", context );
		assertEquals( "http://www.example.com/?foo=bar", variables.get( result ) );
	}

	@DisplayName( "It can decodeForHTML" )
	@Test
	public void testDecodeForHTML() {
		runtime.executeSource( "result = decodeForHTML( '&lt;test&gt;' )", context );
		assertEquals( "<test>", variables.get( result ) );
	}

	@DisplayName( "It can decode for json " )
	@Test
	public void testDecodeForJSON() {
		runtime.executeSource( "result = decodeForJSON( '{\"test\":\"value\"}' )", context );
		assertEquals( "{\"test\":\"value\"}", variables.get( result ) );
	}

	@DisplayName( "It can decode for Base 64" )
	@Test
	public void testDecodeForBase64() {
		runtime.executeSource( "result = decodeForBase64( 'VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGluZw==' )", context );
		assertEquals( "This is a base64 encoding", variables.get( result ) );
	}

	@DisplayName( "It can encode an empty string" )
	@Test
	public void testEncodeEmptyString() {
		runtime.executeSource( "result = esapiEncode( 'html', '' )", context );
		assertEquals( "", variables.get( result ) );
	}

}
