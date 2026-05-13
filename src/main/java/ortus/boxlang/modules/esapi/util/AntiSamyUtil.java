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
package ortus.boxlang.modules.esapi.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.owasp.validator.html.Policy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ortus.boxlang.runtime.dynamic.casters.ArrayCaster;
import ortus.boxlang.runtime.dynamic.casters.StringCaster;
import ortus.boxlang.runtime.dynamic.casters.StructCaster;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

/**
 * Utility class for AntiSamy
 */
public class AntiSamyUtil {

	/**
	 * Default policy
	 */
	public static final String								DEFAULT_POLICY				= "ebay";

	/**
	 * The default override mode
	 */
	public static final String								DEFAULT_OVERRIDE_MODE		= "merge";

	// Struct keys used for policy struct configuration
	private static final Key								KEY_BASE_POLICY				= Key.of( "basePolicy" );
	private static final Key								KEY_OVERRIDE_MODE			= Key.of( "overrideMode" );
	private static final Key								KEY_DIRECTIVES				= Key.of( "directives" );
	private static final Key								KEY_ALLOW_TAGS				= Key.of( "allowTags" );
	private static final Key								KEY_TAG_RULES				= Key.of( "tagRules" );
	private static final Key								KEY_GLOBAL_ATTRIBUTES		= Key.of( "globalAttributes" );
	private static final Key								KEY_DYNAMIC_ATTRIBUTES		= Key.of( "dynamicAttributes" );
	private static final Key								KEY_CSS_RULES				= Key.of( "cssRules" );
	private static final Key								KEY_ALLOWED_EMPTY_TAGS		= Key.of( "allowedEmptyTags" );
	private static final Key								KEY_REQUIRE_CLOSING_TAGS	= Key.of( "requireClosingTags" );
	private static final Key								KEY_TAGS_TO_ENCODE			= Key.of( "tagsToEncode" );
	private static final Key								KEY_ACTION					= Key.of( "action" );
	private static final Key								KEY_ATTRIBUTES				= Key.of( "attributes" );
	private static final Key								KEY_REGEXPS					= Key.of( "regexps" );
	private static final Key								KEY_ALLOWED_VALUES			= Key.of( "allowedValues" );
	private static final Key								KEY_ON_INVALID				= Key.of( "onInvalid" );
	private static final Key								KEY_DESCRIPTION				= Key.of( "description" );
	private static final Key								KEY_SHORTHAND_REFS			= Key.of( "shorthandRefs" );

	/**
	 * Top-level config keys in deterministic order for cache key generation.
	 */
	private static final Key[]								CONFIG_KEYS					= new Key[] {
	    KEY_BASE_POLICY,
	    KEY_OVERRIDE_MODE,
	    KEY_DIRECTIVES,
	    KEY_ALLOW_TAGS,
	    KEY_TAG_RULES,
	    KEY_GLOBAL_ATTRIBUTES,
	    KEY_DYNAMIC_ATTRIBUTES,
	    KEY_CSS_RULES,
	    KEY_ALLOWED_EMPTY_TAGS,
	    KEY_REQUIRE_CLOSING_TAGS,
	    KEY_TAGS_TO_ENCODE
	};

	/**
	 * Cache for policies built from struct configs.
	 */
	private static final ConcurrentHashMap<String, Policy>	POLICY_CACHE				= new ConcurrentHashMap<>();

	/**
	 * Available Policies in the AntiSamy library
	 */
	public static final IStruct								POLICIES					= Struct.of(
	    "anythinggoes", getPolicyURL( "/antisamy-anythinggoes.xml" ),
	    "ebay", getPolicyURL( "/antisamy-ebay.xml" ),
	    "myspace", getPolicyURL( "/antisamy-myspace.xml" ),
	    "slashdot", getPolicyURL( "/antisamy-slashdot.xml" ),
	    "tinymce", getPolicyURL( "/antisamy-tinymce.xml" )
	);

	/**
	 * Validate that the incoming policy string is a known named policy or a valid file path.
	 *
	 * @param policy The policy to validate
	 *
	 * @throws BoxRuntimeException If the policy is invalid
	 */
	public static void validatePolicy( String policy ) {
		if ( !POLICIES.containsKey( policy ) && !new File( policy ).exists() ) {
			throw new BoxRuntimeException(
			    "Invalid Policy [" + policy + "]. Policy must be one of: " + POLICIES.keySet() + " or a valid policy file path."
			);
		}
	}

	/**
	 * Load a policy from a string name (named policy or file path).
	 *
	 * @param policy The policy to load
	 *
	 * @return The policy
	 */
	public static Policy loadPolicy( String policy ) {
		try {
			if ( POLICIES.containsKey( policy ) ) {
				return Policy.getInstance( ( URL ) POLICIES.get( Key.of( policy ) ) );
			}
			return Policy.getInstance( policy );
		} catch ( Exception e ) {
			throw new BoxRuntimeException( "Error loading policy [" + policy + "]", e );
		}
	}

	/**
	 * Build a Policy from a struct configuration. The struct may contain:
	 * <ul>
	 * <li>{@code basePolicy} - A named policy or file path to start from (defaults to "ebay"). Use "none" for a blank policy.</li>
	 * <li>{@code overrideMode} - "merge" (default) or "override" - controls how struct keys are applied to the base</li>
	 * <li>{@code directives} - Struct of directive key/value pairs</li>
	 * <li>{@code allowTags} - Array of tag names to allow with "validate" action (sugar for tagRules)</li>
	 * <li>{@code tagRules} - Struct of tag name to action string or config struct</li>
	 * <li>{@code globalAttributes} - Struct of attribute name to attribute config</li>
	 * <li>{@code dynamicAttributes} - Struct of attribute name pattern to attribute config</li>
	 * <li>{@code cssRules} - Struct of CSS property name to config</li>
	 * <li>{@code allowedEmptyTags} - Array of tag names</li>
	 * <li>{@code requireClosingTags} - Array of tag names</li>
	 * <li>{@code tagsToEncode} - Array of tag names</li>
	 * </ul>
	 *
	 * @param config The struct configuration
	 *
	 * @return The built Policy
	 */
	public static Policy buildPolicyFromStruct( IStruct config ) {
		String	cacheKey		= buildConfigCacheKey( config );
		Policy	cachedPolicy	= POLICY_CACHE.get( cacheKey );
		if ( cachedPolicy != null ) {
			return cachedPolicy;
		}

		String	basePolicy		= StringCaster.cast( config.getOrDefault( KEY_BASE_POLICY, DEFAULT_POLICY ) );
		String	overrideMode	= StringCaster.cast( config.getOrDefault( KEY_OVERRIDE_MODE, DEFAULT_OVERRIDE_MODE ) );
		boolean	isMerge			= overrideMode.equalsIgnoreCase( "merge" );

		try {
			Policy builtPolicy;

			// "none" means build a blank policy from scratch
			if ( basePolicy.equalsIgnoreCase( "none" ) ) {
				Document doc = buildDocumentFromStruct( config );
				builtPolicy = parsePolicyFromDocument( doc );
			} else {
				validatePolicy( basePolicy );
				Document doc = loadPolicyAsDocument( basePolicy );
				applyStructToDocument( doc, config, isMerge );
				builtPolicy = parsePolicyFromDocument( doc );
			}

			Policy existingPolicy = POLICY_CACHE.putIfAbsent( cacheKey, builtPolicy );
			return existingPolicy != null ? existingPolicy : builtPolicy;
		} catch ( BoxRuntimeException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new BoxRuntimeException( "Error building policy from struct", e );
		}
	}

	/**
	 * Clear cached struct-based policies.
	 */
	public static void clearPolicyCache() {
		POLICY_CACHE.clear();
	}

	/**
	 * Remove a specific struct-based policy from the cache.
	 *
	 * @param config The struct policy configuration to evict
	 */
	public static void removePolicyFromCache( IStruct config ) {
		POLICY_CACHE.remove( buildConfigCacheKey( config ) );
	}

	/**
	 * Build a deterministic cache key for a policy config.
	 */
	private static String buildConfigCacheKey( IStruct config ) {
		StringBuilder sb = new StringBuilder();
		for ( Key key : CONFIG_KEYS ) {
			if ( config.containsKey( key ) ) {
				appendLengthPrefixed( sb, key.getName() );
				appendDeterministicValue( sb, config.get( key ) );
			}
		}
		return sb.toString();
	}

	/**
	 * Append a deterministic representation of nested values used in policy config.
	 */
	private static void appendDeterministicValue( StringBuilder sb, Object value ) {
		if ( value == null ) {
			sb.append( 'N' );
			return;
		}

		if ( value instanceof IStruct structValue ) {
			sb.append( 'S' ).append( '{' );
			List<Key> sortedKeys = new ArrayList<>( structValue.keySet() );
			sortedKeys.sort( Comparator.comparing( Key::getName, String.CASE_INSENSITIVE_ORDER ).thenComparing( Key::getName ) );
			for ( Key key : sortedKeys ) {
				appendLengthPrefixed( sb, key.getName() );
				appendDeterministicValue( sb, structValue.get( key ) );
			}
			sb.append( '}' );
			return;
		}

		if ( value instanceof Array arrayValue ) {
			sb.append( 'A' ).append( '[' );
			for ( Object item : arrayValue ) {
				appendDeterministicValue( sb, item );
			}
			sb.append( ']' );
			return;
		}

		sb.append( 'V' );
		appendLengthPrefixed( sb, StringCaster.cast( value ) );
	}

	/**
	 * Append length-prefixed text to avoid key collisions in concatenated cache keys.
	 */
	private static void appendLengthPrefixed( StringBuilder sb, String value ) {
		sb.append( value.length() ).append( ':' ).append( value ).append( ';' );
	}

	// ==========================================
	// XML DOM Manipulation Methods
	// ==========================================

	/**
	 * Load a named or file-based policy as an XML DOM Document.
	 * Opens the policy XML from the JAR (for named policies) or from the filesystem (for file paths)
	 * and parses it into a DOM Document for programmatic manipulation.
	 *
	 * @param policy The named policy or file path to load
	 *
	 * @return The parsed XML Document
	 *
	 * @throws Exception If the policy cannot be loaded or parsed
	 */
	private static Document loadPolicyAsDocument( String policy ) throws Exception {
		InputStream is;
		if ( POLICIES.containsKey( policy ) ) {
			URL url = ( URL ) POLICIES.get( Key.of( policy ) );
			is = url.openStream();
		} else {
			is = new java.io.FileInputStream( policy );
		}
		try ( is ) {
			return parseXmlStream( is );
		}
	}

	/**
	 * Parse an InputStream into an XML Document with secure XML parsing settings.
	 * Disables external entities and DTD loading to prevent XXE attacks.
	 *
	 * @param is The InputStream containing the XML content
	 *
	 * @return The parsed XML Document
	 *
	 * @throws ParserConfigurationException If the parser cannot be configured
	 * @throws SAXException                 If a SAX parsing error occurs
	 * @throws IOException                  If an I/O error occurs
	 */
	private static Document parseXmlStream( InputStream is ) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature( "http://xml.org/sax/features/external-general-entities", false );
		dbf.setFeature( "http://xml.org/sax/features/external-parameter-entities", false );
		dbf.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", true );
		dbf.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse( is );
	}

	/**
	 * Serialize a DOM Document to XML bytes and parse it into an AntiSamy Policy.
	 * This converts the in-memory DOM representation back into XML text and feeds it
	 * to AntiSamy's standard {@code Policy.getInstance(InputStream)} factory method.
	 *
	 * @param doc The DOM Document representing the policy
	 *
	 * @return The parsed AntiSamy Policy
	 *
	 * @throws Exception If serialization or policy parsing fails
	 */
	private static Policy parsePolicyFromDocument( Document doc ) throws Exception {
		TransformerFactory		tf			= TransformerFactory.newInstance();
		Transformer				transformer	= tf.newTransformer();
		java.io.StringWriter	writer		= new java.io.StringWriter();
		transformer.transform( new DOMSource( doc ), new StreamResult( writer ) );
		byte[] xmlBytes = writer.toString().getBytes( StandardCharsets.UTF_8 );
		return Policy.getInstance( new ByteArrayInputStream( xmlBytes ) );
	}

	/**
	 * Apply struct overrides to an existing policy XML document.
	 * Processes each supported struct key (directives, tagRules, allowTags, globalAttributes,
	 * dynamicAttributes, cssRules, allowedEmptyTags, requireClosingTags, tagsToEncode)
	 * and applies them to the DOM according to the merge/override mode.
	 *
	 * @param doc     The policy XML document to modify
	 * @param config  The struct containing the overrides
	 * @param isMerge If true, merge overrides into existing sections; if false, replace entire sections
	 */
	private static void applyStructToDocument( Document doc, IStruct config, boolean isMerge ) {
		Element root = doc.getDocumentElement();

		// Directives
		if ( config.containsKey( KEY_DIRECTIVES ) ) {
			IStruct directives = StructCaster.cast( config.get( KEY_DIRECTIVES ) );
			applyDirectives( doc, root, directives, isMerge );
		}

		// Tag rules (including allowTags sugar)
		boolean hasTagRules = config.containsKey( KEY_TAG_RULES ) || config.containsKey( KEY_ALLOW_TAGS );
		if ( hasTagRules ) {
			IStruct tagRules = config.containsKey( KEY_TAG_RULES )
			    ? StructCaster.cast( config.get( KEY_TAG_RULES ) )
			    : new Struct();
			// Merge allowTags into tagRules — allowTags are "validate" with no attribute restrictions
			if ( config.containsKey( KEY_ALLOW_TAGS ) ) {
				Array allowTags = ArrayCaster.cast( config.get( KEY_ALLOW_TAGS ) );
				for ( Object tagName : allowTags ) {
					Key tagKey = Key.of( StringCaster.cast( tagName ) );
					// tagRules wins over allowTags
					if ( !tagRules.containsKey( tagKey ) ) {
						tagRules.put( tagKey, "validate" );
					}
				}
			}
			applyTagRules( doc, root, tagRules, isMerge );
		}

		// Global attributes
		if ( config.containsKey( KEY_GLOBAL_ATTRIBUTES ) ) {
			IStruct globalAttrs = StructCaster.cast( config.get( KEY_GLOBAL_ATTRIBUTES ) );
			applyAttributes( doc, root, "global-tag-attributes", globalAttrs, isMerge );
		}

		// Dynamic attributes
		if ( config.containsKey( KEY_DYNAMIC_ATTRIBUTES ) ) {
			IStruct dynamicAttrs = StructCaster.cast( config.get( KEY_DYNAMIC_ATTRIBUTES ) );
			applyAttributes( doc, root, "dynamic-tag-attributes", dynamicAttrs, isMerge );
		}

		// CSS rules
		if ( config.containsKey( KEY_CSS_RULES ) ) {
			IStruct cssRules = StructCaster.cast( config.get( KEY_CSS_RULES ) );
			applyCssRules( doc, root, cssRules, isMerge );
		}

		// Allowed empty tags
		if ( config.containsKey( KEY_ALLOWED_EMPTY_TAGS ) ) {
			Array tags = ArrayCaster.cast( config.get( KEY_ALLOWED_EMPTY_TAGS ) );
			applyLiteralList( doc, root, "allowed-empty-tags", tags, isMerge );
		}

		// Require closing tags
		if ( config.containsKey( KEY_REQUIRE_CLOSING_TAGS ) ) {
			Array tags = ArrayCaster.cast( config.get( KEY_REQUIRE_CLOSING_TAGS ) );
			applyLiteralList( doc, root, "require-closing-tags", tags, isMerge );
		}

		// Tags to encode
		if ( config.containsKey( KEY_TAGS_TO_ENCODE ) ) {
			Array tags = ArrayCaster.cast( config.get( KEY_TAGS_TO_ENCODE ) );
			applyTagsToEncode( doc, root, tags, isMerge );
		}
	}

	/**
	 * Apply directive overrides to the {@code <directives>} section of the document.
	 * In merge mode, existing directives are updated and new ones are appended.
	 * In override mode, all existing directives are removed before adding the new ones.
	 *
	 * @param doc        The policy XML document
	 * @param root       The root element of the policy document
	 * @param directives The struct of directive name/value pairs to apply
	 * @param isMerge    If true, merge into existing directives; if false, replace all directives
	 */
	private static void applyDirectives( Document doc, Element root, IStruct directives, boolean isMerge ) {
		Element section = getOrCreateSection( doc, root, "directives" );
		if ( !isMerge ) {
			removeAllChildren( section );
		}
		for ( var entry : directives.entrySet() ) {
			String		name	= entry.getKey().getName();
			String		value	= StringCaster.cast( entry.getValue() );
			// Try to find existing directive to update
			boolean		found	= false;
			NodeList	nodes	= section.getElementsByTagName( "directive" );
			for ( int i = 0; i < nodes.getLength(); i++ ) {
				Element directive = ( Element ) nodes.item( i );
				if ( directive.getAttribute( "name" ).equals( name ) ) {
					directive.setAttribute( "value", value );
					found = true;
					break;
				}
			}
			if ( !found ) {
				Element directive = doc.createElement( "directive" );
				directive.setAttribute( "name", name );
				directive.setAttribute( "value", value );
				section.appendChild( directive );
			}
		}
	}

	/**
	 * Apply tag rules to the {@code <tag-rules>} section of the document.
	 * Each tag can be a simple action string ("validate", "filter", "remove", "truncate")
	 * or a struct with "action" and optional "attributes".
	 * In merge mode, individual tag rules are added/replaced while existing ones are preserved.
	 * In override mode, all existing tag rules are removed first.
	 *
	 * @param doc      The policy XML document
	 * @param root     The root element of the policy document
	 * @param tagRules The struct of tag rules to apply
	 * @param isMerge  If true, merge into existing tag rules; if false, replace all tag rules
	 */
	private static void applyTagRules( Document doc, Element root, IStruct tagRules, boolean isMerge ) {
		Element section = getOrCreateSection( doc, root, "tag-rules" );
		if ( !isMerge ) {
			removeAllChildren( section );
		}
		for ( var entry : tagRules.entrySet() ) {
			String	tagName		= entry.getKey().getName();
			Object	tagConfig	= entry.getValue();

			// Remove existing tag rule if present (we're replacing it)
			removeChildByAttribute( section, "tag", "name", tagName );

			String	action		= "validate";
			IStruct	attributes	= null;

			if ( tagConfig instanceof String configStr ) {
				action = configStr;
			} else {
				IStruct configStruct = StructCaster.cast( tagConfig );
				action		= StringCaster.cast( configStruct.getOrDefault( KEY_ACTION, "validate" ) );
				attributes	= configStruct.containsKey( KEY_ATTRIBUTES )
				    ? StructCaster.cast( configStruct.get( KEY_ATTRIBUTES ) )
				    : null;
			}

			Element tagElement = doc.createElement( "tag" );
			tagElement.setAttribute( "name", tagName );
			tagElement.setAttribute( "action", action );

			if ( attributes != null ) {
				appendAttributeElements( doc, tagElement, attributes );
			}
			section.appendChild( tagElement );
		}
	}

	/**
	 * Apply global or dynamic attributes to the document.
	 * These need to also be registered in {@code <common-attributes>} for AntiSamy to resolve them.
	 * In merge mode, individual attributes are added/replaced while existing ones are preserved.
	 * In override mode, all existing attributes in the section are removed first.
	 *
	 * @param doc         The policy XML document
	 * @param root        The root element of the policy document
	 * @param sectionName The XML section name (e.g. "global-tag-attributes" or "dynamic-tag-attributes")
	 * @param attrs       The struct of attribute definitions to apply
	 * @param isMerge     If true, merge into existing attributes; if false, replace all attributes
	 */
	private static void applyAttributes( Document doc, Element root, String sectionName, IStruct attrs, boolean isMerge ) {
		Element	section			= getOrCreateSection( doc, root, sectionName );
		Element	commonSection	= getOrCreateSection( doc, root, "common-attributes" );
		if ( !isMerge ) {
			removeAllChildren( section );
		}
		for ( var entry : attrs.entrySet() ) {
			String	attrName	= entry.getKey().getName();
			Object	attrConfig	= entry.getValue();

			// Remove existing attribute if present
			removeChildByAttribute( section, "attribute", "name", attrName );

			Element attrElement = doc.createElement( "attribute" );
			attrElement.setAttribute( "name", attrName );
			section.appendChild( attrElement );

			// Also make sure the attribute definition exists in common-attributes
			if ( findChildByAttribute( commonSection, "attribute", "name", attrName ) == null ) {
				Element commonAttr = buildAttributeDefinition( doc, attrName, attrConfig );
				commonSection.appendChild( commonAttr );
			}
		}
	}

	/**
	 * Apply CSS rules to the {@code <css-rules>} section of the document.
	 * Each CSS property can define allowed regular expressions, literal values, shorthand references,
	 * and a description. In merge mode, individual properties are added/replaced.
	 * In override mode, all existing CSS rules are removed first.
	 *
	 * @param doc      The policy XML document
	 * @param root     The root element of the policy document
	 * @param cssRules The struct of CSS property rules to apply
	 * @param isMerge  If true, merge into existing CSS rules; if false, replace all CSS rules
	 */
	private static void applyCssRules( Document doc, Element root, IStruct cssRules, boolean isMerge ) {
		Element section = getOrCreateSection( doc, root, "css-rules" );
		if ( !isMerge ) {
			removeAllChildren( section );
		}
		for ( var entry : cssRules.entrySet() ) {
			String	propName	= entry.getKey().getName();
			Object	propConfig	= entry.getValue();

			// Remove existing property if present
			removeChildByAttribute( section, "property", "name", propName );

			Element propElement = doc.createElement( "property" );
			propElement.setAttribute( "name", propName );

			if ( propConfig instanceof IStruct configStruct ) {
				if ( configStruct.containsKey( KEY_REGEXPS ) ) {
					appendRegexpList( doc, propElement, ArrayCaster.cast( configStruct.get( KEY_REGEXPS ) ) );
				}
				if ( configStruct.containsKey( KEY_ALLOWED_VALUES ) ) {
					appendLiteralList( doc, propElement, ArrayCaster.cast( configStruct.get( KEY_ALLOWED_VALUES ) ) );
				}
				if ( configStruct.containsKey( KEY_SHORTHAND_REFS ) ) {
					Element	shList	= doc.createElement( "shorthand-list" );
					Array	refs	= ArrayCaster.cast( configStruct.get( KEY_SHORTHAND_REFS ) );
					for ( Object ref : refs ) {
						Element sh = doc.createElement( "shorthand" );
						sh.setAttribute( "name", StringCaster.cast( ref ) );
						shList.appendChild( sh );
					}
					propElement.appendChild( shList );
				}
				if ( configStruct.containsKey( KEY_DESCRIPTION ) ) {
					propElement.setAttribute( "description", StringCaster.cast( configStruct.get( KEY_DESCRIPTION ) ) );
				}
			}
			section.appendChild( propElement );
		}
	}

	/**
	 * Apply a literal-list section such as {@code <allowed-empty-tags>} or {@code <require-closing-tags>}.
	 * In merge mode, new values are appended to the existing list (avoiding duplicates).
	 * In override mode, the existing list is cleared before adding new values.
	 *
	 * @param doc         The policy XML document
	 * @param root        The root element of the policy document
	 * @param sectionName The XML section name (e.g. "allowed-empty-tags" or "require-closing-tags")
	 * @param tags        The array of tag names to add
	 * @param isMerge     If true, merge into existing list; if false, replace the entire list
	 */
	private static void applyLiteralList( Document doc, Element root, String sectionName, Array tags, boolean isMerge ) {
		Element section = getOrCreateSection( doc, root, sectionName );
		if ( !isMerge ) {
			removeAllChildren( section );
		}
		Element litList = ( Element ) section.getElementsByTagName( "literal-list" ).item( 0 );
		if ( litList == null ) {
			litList = doc.createElement( "literal-list" );
			section.appendChild( litList );
		}
		for ( Object tag : tags ) {
			String value = StringCaster.cast( tag );
			// Avoid duplicates when merging
			if ( !isMerge || findChildByAttribute( litList, "literal", "value", value ) == null ) {
				Element literal = doc.createElement( "literal" );
				literal.setAttribute( "value", value );
				litList.appendChild( literal );
			}
		}
	}

	/**
	 * Apply the {@code <tags-to-encode>} section of the document.
	 * Tags listed here will be entity-encoded rather than removed by AntiSamy.
	 * In merge mode, new tags are appended. In override mode, existing tags are cleared first.
	 *
	 * @param doc     The policy XML document
	 * @param root    The root element of the policy document
	 * @param tags    The array of tag names to encode
	 * @param isMerge If true, merge into existing tags; if false, replace all tags
	 */
	private static void applyTagsToEncode( Document doc, Element root, Array tags, boolean isMerge ) {
		Element section = getOrCreateSection( doc, root, "tags-to-encode" );
		if ( !isMerge ) {
			removeAllChildren( section );
		}
		for ( Object tag : tags ) {
			String	value		= StringCaster.cast( tag );
			Element	tagElement	= doc.createElement( "tag" );
			tagElement.setTextContent( value );
			section.appendChild( tagElement );
		}
	}

	// ==========================================
	// From-scratch XML Document Building
	// ==========================================

	/**
	 * Build a complete policy XML document from a struct with no base policy.
	 * Creates all required XML sections in the order mandated by the AntiSamy XSD schema.
	 * Sections not specified in the struct are created as empty elements.
	 *
	 * @param config The struct configuration to build the policy from
	 *
	 * @return The complete policy XML Document
	 *
	 * @throws ParserConfigurationException If the XML document builder cannot be created
	 */
	private static Document buildDocumentFromStruct( IStruct config ) throws ParserConfigurationException {
		DocumentBuilderFactory	dbf		= DocumentBuilderFactory.newInstance();
		DocumentBuilder			db		= dbf.newDocumentBuilder();
		Document				doc		= db.newDocument();

		Element					root	= doc.createElement( "anti-samy-rules" );
		root.setAttribute( "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
		root.setAttribute( "xsi:noNamespaceSchemaLocation", "antisamy.xsd" );
		doc.appendChild( root );

		// The XSD requires these sections in order, even if empty
		buildDirectivesSection( doc, root, config );
		buildSection( doc, root, "common-regexps" );
		buildCommonAttributesSection( doc, root, config );
		buildGlobalAttributesSection( doc, root, config, "global-tag-attributes", KEY_GLOBAL_ATTRIBUTES );
		buildDynamicAttributesSection( doc, root, config );
		buildTagsToEncodeSection( doc, root, config );
		buildTagRulesSection( doc, root, config );
		buildCssRulesSection( doc, root, config );
		buildLiteralListSection( doc, root, config, "allowed-empty-tags", KEY_ALLOWED_EMPTY_TAGS );
		buildLiteralListSection( doc, root, config, "require-closing-tags", KEY_REQUIRE_CLOSING_TAGS );

		return doc;
	}

	/**
	 * Build the {@code <directives>} section of a from-scratch policy document.
	 *
	 * @param doc    The policy XML document
	 * @param root   The root element of the policy document
	 * @param config The struct configuration containing the directives
	 */
	private static void buildDirectivesSection( Document doc, Element root, IStruct config ) {
		Element section = buildSection( doc, root, "directives" );
		if ( config.containsKey( KEY_DIRECTIVES ) ) {
			IStruct directives = StructCaster.cast( config.get( KEY_DIRECTIVES ) );
			for ( var entry : directives.entrySet() ) {
				Element directive = doc.createElement( "directive" );
				directive.setAttribute( "name", entry.getKey().getName() );
				directive.setAttribute( "value", StringCaster.cast( entry.getValue() ) );
				section.appendChild( directive );
			}
		}
	}

	/**
	 * Build common-attributes section from all attribute definitions found in
	 * tagRules, globalAttributes, and dynamicAttributes.
	 */
	private static void buildCommonAttributesSection( Document doc, Element root, IStruct config ) {
		Element section = buildSection( doc, root, "common-attributes" );

		// Collect attributes from tagRules
		if ( config.containsKey( KEY_TAG_RULES ) ) {
			IStruct tagRules = StructCaster.cast( config.get( KEY_TAG_RULES ) );
			for ( var entry : tagRules.entrySet() ) {
				Object tagConfig = entry.getValue();
				if ( tagConfig instanceof IStruct configStruct && configStruct.containsKey( KEY_ATTRIBUTES ) ) {
					IStruct attrs = StructCaster.cast( configStruct.get( KEY_ATTRIBUTES ) );
					for ( var attrEntry : attrs.entrySet() ) {
						String attrName = attrEntry.getKey().getName();
						if ( findChildByAttribute( section, "attribute", "name", attrName ) == null ) {
							section.appendChild( buildAttributeDefinition( doc, attrName, attrEntry.getValue() ) );
						}
					}
				}
			}
		}

		// Collect attributes from globalAttributes
		if ( config.containsKey( KEY_GLOBAL_ATTRIBUTES ) ) {
			IStruct globalAttrs = StructCaster.cast( config.get( KEY_GLOBAL_ATTRIBUTES ) );
			for ( var entry : globalAttrs.entrySet() ) {
				String attrName = entry.getKey().getName();
				if ( findChildByAttribute( section, "attribute", "name", attrName ) == null ) {
					section.appendChild( buildAttributeDefinition( doc, attrName, entry.getValue() ) );
				}
			}
		}

		// Collect attributes from dynamicAttributes
		if ( config.containsKey( KEY_DYNAMIC_ATTRIBUTES ) ) {
			IStruct dynamicAttrs = StructCaster.cast( config.get( KEY_DYNAMIC_ATTRIBUTES ) );
			for ( var entry : dynamicAttrs.entrySet() ) {
				String attrName = entry.getKey().getName();
				if ( findChildByAttribute( section, "attribute", "name", attrName ) == null ) {
					section.appendChild( buildAttributeDefinition( doc, attrName, entry.getValue() ) );
				}
			}
		}
	}

	/**
	 * Build a global attributes section ({@code <global-tag-attributes>}) for a from-scratch policy.
	 * These are attribute references that apply to all allowed tags. The actual attribute
	 * definitions must exist in {@code <common-attributes>}.
	 *
	 * @param doc         The policy XML document
	 * @param root        The root element of the policy document
	 * @param config      The struct configuration
	 * @param sectionName The XML section name to create
	 * @param configKey   The struct key to read attribute definitions from
	 */
	private static void buildGlobalAttributesSection( Document doc, Element root, IStruct config, String sectionName, Key configKey ) {
		Element section = buildSection( doc, root, sectionName );
		if ( config.containsKey( configKey ) ) {
			IStruct attrs = StructCaster.cast( config.get( configKey ) );
			for ( var entry : attrs.entrySet() ) {
				Element attrElement = doc.createElement( "attribute" );
				attrElement.setAttribute( "name", entry.getKey().getName() );
				section.appendChild( attrElement );
			}
		}
	}

	/**
	 * Build the {@code <dynamic-tag-attributes>} section for a from-scratch policy.
	 * Dynamic attributes use wildcard patterns (e.g. "data-*") to match attribute names.
	 *
	 * @param doc    The policy XML document
	 * @param root   The root element of the policy document
	 * @param config The struct configuration containing dynamic attribute definitions
	 */
	private static void buildDynamicAttributesSection( Document doc, Element root, IStruct config ) {
		Element section = buildSection( doc, root, "dynamic-tag-attributes" );
		if ( config.containsKey( KEY_DYNAMIC_ATTRIBUTES ) ) {
			IStruct attrs = StructCaster.cast( config.get( KEY_DYNAMIC_ATTRIBUTES ) );
			for ( var entry : attrs.entrySet() ) {
				Element attrElement = doc.createElement( "attribute" );
				attrElement.setAttribute( "name", entry.getKey().getName() );
				section.appendChild( attrElement );
			}
		}
	}

	/**
	 * Build the {@code <tags-to-encode>} section for a from-scratch policy.
	 * Tags listed here will be entity-encoded rather than removed.
	 *
	 * @param doc    The policy XML document
	 * @param root   The root element of the policy document
	 * @param config The struct configuration containing tags to encode
	 */
	private static void buildTagsToEncodeSection( Document doc, Element root, IStruct config ) {
		Element section = buildSection( doc, root, "tags-to-encode" );
		if ( config.containsKey( KEY_TAGS_TO_ENCODE ) ) {
			Array tags = ArrayCaster.cast( config.get( KEY_TAGS_TO_ENCODE ) );
			for ( Object tag : tags ) {
				Element tagElement = doc.createElement( "tag" );
				tagElement.setTextContent( StringCaster.cast( tag ) );
				section.appendChild( tagElement );
			}
		}
	}

	/**
	 * Build the {@code <tag-rules>} section for a from-scratch policy.
	 * Processes both the {@code allowTags} sugar (array of tag names defaulting to "validate" action)
	 * and the explicit {@code tagRules} struct. When a tag appears in both, {@code tagRules} wins.
	 *
	 * @param doc    The policy XML document
	 * @param root   The root element of the policy document
	 * @param config The struct configuration containing tag rules and/or allowTags
	 */
	private static void buildTagRulesSection( Document doc, Element root, IStruct config ) {
		Element section = buildSection( doc, root, "tag-rules" );

		// Process allowTags sugar
		if ( config.containsKey( KEY_ALLOW_TAGS ) ) {
			Array	allowTags	= ArrayCaster.cast( config.get( KEY_ALLOW_TAGS ) );
			IStruct	tagRules	= config.containsKey( KEY_TAG_RULES )
			    ? StructCaster.cast( config.get( KEY_TAG_RULES ) )
			    : new Struct();
			for ( Object tagName : allowTags ) {
				Key tagKey = Key.of( StringCaster.cast( tagName ) );
				if ( !tagRules.containsKey( tagKey ) ) {
					Element tagElement = doc.createElement( "tag" );
					tagElement.setAttribute( "name", StringCaster.cast( tagName ) );
					tagElement.setAttribute( "action", "validate" );
					section.appendChild( tagElement );
				}
			}
		}

		// Process explicit tagRules
		if ( config.containsKey( KEY_TAG_RULES ) ) {
			IStruct tagRules = StructCaster.cast( config.get( KEY_TAG_RULES ) );
			for ( var entry : tagRules.entrySet() ) {
				String	tagName		= entry.getKey().getName();
				Object	tagConfig	= entry.getValue();

				String	action		= "validate";
				IStruct	attributes	= null;

				if ( tagConfig instanceof String configStr ) {
					action = configStr;
				} else {
					IStruct configStruct = StructCaster.cast( tagConfig );
					action		= StringCaster.cast( configStruct.getOrDefault( KEY_ACTION, "validate" ) );
					attributes	= configStruct.containsKey( KEY_ATTRIBUTES )
					    ? StructCaster.cast( configStruct.get( KEY_ATTRIBUTES ) )
					    : null;
				}

				Element tagElement = doc.createElement( "tag" );
				tagElement.setAttribute( "name", tagName );
				tagElement.setAttribute( "action", action );

				if ( attributes != null ) {
					appendAttributeElements( doc, tagElement, attributes );
				}
				section.appendChild( tagElement );
			}
		}
	}

	/**
	 * Build the {@code <css-rules>} section for a from-scratch policy.
	 * Each CSS property can define allowed regular expressions, literal values,
	 * shorthand references, and a description.
	 *
	 * @param doc    The policy XML document
	 * @param root   The root element of the policy document
	 * @param config The struct configuration containing CSS rules
	 */
	private static void buildCssRulesSection( Document doc, Element root, IStruct config ) {
		Element section = buildSection( doc, root, "css-rules" );
		if ( config.containsKey( KEY_CSS_RULES ) ) {
			IStruct cssRules = StructCaster.cast( config.get( KEY_CSS_RULES ) );
			for ( var entry : cssRules.entrySet() ) {
				String	propName	= entry.getKey().getName();
				Object	propConfig	= entry.getValue();

				Element	propElement	= doc.createElement( "property" );
				propElement.setAttribute( "name", propName );

				if ( propConfig instanceof IStruct configStruct ) {
					if ( configStruct.containsKey( KEY_REGEXPS ) ) {
						appendRegexpList( doc, propElement, ArrayCaster.cast( configStruct.get( KEY_REGEXPS ) ) );
					}
					if ( configStruct.containsKey( KEY_ALLOWED_VALUES ) ) {
						appendLiteralList( doc, propElement, ArrayCaster.cast( configStruct.get( KEY_ALLOWED_VALUES ) ) );
					}
					if ( configStruct.containsKey( KEY_SHORTHAND_REFS ) ) {
						Element	shList	= doc.createElement( "shorthand-list" );
						Array	refs	= ArrayCaster.cast( configStruct.get( KEY_SHORTHAND_REFS ) );
						for ( Object ref : refs ) {
							Element sh = doc.createElement( "shorthand" );
							sh.setAttribute( "name", StringCaster.cast( ref ) );
							shList.appendChild( sh );
						}
						propElement.appendChild( shList );
					}
					if ( configStruct.containsKey( KEY_DESCRIPTION ) ) {
						propElement.setAttribute( "description", StringCaster.cast( configStruct.get( KEY_DESCRIPTION ) ) );
					}
				}
				section.appendChild( propElement );
			}
		}
	}

	/**
	 * Build a literal-list section (e.g. {@code <allowed-empty-tags>} or {@code <require-closing-tags>})
	 * for a from-scratch policy.
	 *
	 * @param doc         The policy XML document
	 * @param root        The root element of the policy document
	 * @param config      The struct configuration
	 * @param sectionName The XML section name to create
	 * @param configKey   The struct key to read the array of tag names from
	 */
	private static void buildLiteralListSection( Document doc, Element root, IStruct config, String sectionName, Key configKey ) {
		Element section = buildSection( doc, root, sectionName );
		if ( config.containsKey( configKey ) ) {
			Element	litList	= doc.createElement( "literal-list" );
			Array	tags	= ArrayCaster.cast( config.get( configKey ) );
			for ( Object tag : tags ) {
				Element literal = doc.createElement( "literal" );
				literal.setAttribute( "value", StringCaster.cast( tag ) );
				litList.appendChild( literal );
			}
			section.appendChild( litList );
		}
	}

	// ==========================================
	// XML DOM Helper Methods
	// ==========================================

	/**
	 * Build an {@code <attribute>} definition element for the {@code <common-attributes>} section.
	 * The definition can include regular expressions, literal allowed values, an onInvalid action,
	 * and a description. These definitions are referenced by tag rules and global/dynamic attributes.
	 *
	 * @param doc    The policy XML document
	 * @param name   The attribute name
	 * @param config The attribute configuration (an IStruct with regexps, allowedValues, onInvalid, description)
	 *
	 * @return The constructed attribute Element
	 */
	private static Element buildAttributeDefinition( Document doc, String name, Object config ) {
		Element attr = doc.createElement( "attribute" );
		attr.setAttribute( "name", name );

		if ( config instanceof IStruct configStruct ) {
			if ( configStruct.containsKey( KEY_REGEXPS ) ) {
				appendRegexpList( doc, attr, ArrayCaster.cast( configStruct.get( KEY_REGEXPS ) ) );
			}
			if ( configStruct.containsKey( KEY_ALLOWED_VALUES ) ) {
				appendLiteralList( doc, attr, ArrayCaster.cast( configStruct.get( KEY_ALLOWED_VALUES ) ) );
			}
			if ( configStruct.containsKey( KEY_ON_INVALID ) ) {
				attr.setAttribute( "onInvalid", StringCaster.cast( configStruct.get( KEY_ON_INVALID ) ) );
			}
			if ( configStruct.containsKey( KEY_DESCRIPTION ) ) {
				attr.setAttribute( "description", StringCaster.cast( configStruct.get( KEY_DESCRIPTION ) ) );
			}
		}

		return attr;
	}

	/**
	 * Append {@code <attribute>} child elements to a {@code <tag>} element in {@code <tag-rules>}.
	 * Each attribute can optionally include inline regexp-list and literal-list definitions.
	 * These reference or extend attribute definitions from {@code <common-attributes>}.
	 *
	 * @param doc        The policy XML document
	 * @param tagElement The parent tag element to append attributes to
	 * @param attributes The struct of attribute name to attribute configuration
	 */
	private static void appendAttributeElements( Document doc, Element tagElement, IStruct attributes ) {
		for ( var entry : attributes.entrySet() ) {
			String	attrName	= entry.getKey().getName();
			Object	attrConfig	= entry.getValue();

			Element	attrElement	= doc.createElement( "attribute" );
			attrElement.setAttribute( "name", attrName );

			// If the attribute has inline regexps/literals, add them directly
			if ( attrConfig instanceof IStruct configStruct ) {
				if ( configStruct.containsKey( KEY_REGEXPS ) ) {
					appendRegexpList( doc, attrElement, ArrayCaster.cast( configStruct.get( KEY_REGEXPS ) ) );
				}
				if ( configStruct.containsKey( KEY_ALLOWED_VALUES ) ) {
					appendLiteralList( doc, attrElement, ArrayCaster.cast( configStruct.get( KEY_ALLOWED_VALUES ) ) );
				}
				if ( configStruct.containsKey( KEY_ON_INVALID ) ) {
					attrElement.setAttribute( "onInvalid", StringCaster.cast( configStruct.get( KEY_ON_INVALID ) ) );
				}
			}

			tagElement.appendChild( attrElement );
		}
	}

	/**
	 * Append a {@code <regexp-list>} element containing {@code <regexp>} children to a parent element.
	 * Each regexp value is added as an inline pattern (not a named reference).
	 *
	 * @param doc     The policy XML document
	 * @param parent  The parent element to append the regexp-list to
	 * @param regexps The array of regular expression pattern strings
	 */
	private static void appendRegexpList( Document doc, Element parent, Array regexps ) {
		Element regexpList = doc.createElement( "regexp-list" );
		for ( Object regexp : regexps ) {
			Element regexpElement = doc.createElement( "regexp" );
			regexpElement.setAttribute( "value", StringCaster.cast( regexp ) );
			regexpList.appendChild( regexpElement );
		}
		parent.appendChild( regexpList );
	}

	/**
	 * Append a {@code <literal-list>} element containing {@code <literal>} children to a parent element.
	 * Each value is added as an allowed literal string.
	 *
	 * @param doc    The policy XML document
	 * @param parent The parent element to append the literal-list to
	 * @param values The array of allowed literal value strings
	 */
	private static void appendLiteralList( Document doc, Element parent, Array values ) {
		Element litList = doc.createElement( "literal-list" );
		for ( Object value : values ) {
			Element literal = doc.createElement( "literal" );
			literal.setAttribute( "value", StringCaster.cast( value ) );
			litList.appendChild( literal );
		}
		parent.appendChild( litList );
	}

	/**
	 * Get an existing section element by tag name, or create a new one if it doesn't exist.
	 * Used when applying overrides to an existing policy document.
	 *
	 * @param doc         The policy XML document
	 * @param root        The root element to search under
	 * @param sectionName The XML element name to find or create
	 *
	 * @return The existing or newly created section Element
	 */
	private static Element getOrCreateSection( Document doc, Element root, String sectionName ) {
		NodeList nodes = root.getElementsByTagName( sectionName );
		if ( nodes.getLength() > 0 ) {
			return ( Element ) nodes.item( 0 );
		}
		Element section = doc.createElement( sectionName );
		root.appendChild( section );
		return section;
	}

	/**
	 * Create an empty section element and append it to the root element.
	 * Used when building a policy document from scratch.
	 *
	 * @param doc         The policy XML document
	 * @param root        The root element to append to
	 * @param sectionName The XML element name to create
	 *
	 * @return The newly created section Element
	 */
	private static Element buildSection( Document doc, Element root, String sectionName ) {
		Element section = doc.createElement( sectionName );
		root.appendChild( section );
		return section;
	}

	/**
	 * Remove all child nodes (elements, text, etc.) from an element.
	 * Used in override mode to clear a section before repopulating it.
	 *
	 * @param element The element whose children should be removed
	 */
	private static void removeAllChildren( Element element ) {
		while ( element.getFirstChild() != null ) {
			element.removeChild( element.getFirstChild() );
		}
	}

	/**
	 * Remove the first child element matching a specific tag name and attribute value.
	 * Used when replacing an existing rule (e.g. replacing a tag rule for "script").
	 * The comparison is case-insensitive.
	 *
	 * @param parent    The parent element to search within
	 * @param tagName   The XML tag name to match (e.g. "tag", "attribute", "property")
	 * @param attrName  The attribute name to match on (e.g. "name")
	 * @param attrValue The attribute value to match (case-insensitive)
	 */
	private static void removeChildByAttribute( Element parent, String tagName, String attrName, String attrValue ) {
		NodeList nodes = parent.getElementsByTagName( tagName );
		for ( int i = 0; i < nodes.getLength(); i++ ) {
			Element el = ( Element ) nodes.item( i );
			if ( el.getAttribute( attrName ).equalsIgnoreCase( attrValue ) ) {
				parent.removeChild( el );
				return;
			}
		}
	}

	/**
	 * Find the first child element matching a specific tag name and attribute value.
	 * The comparison is case-insensitive.
	 *
	 * @param parent    The parent element to search within
	 * @param tagName   The XML tag name to match (e.g. "tag", "attribute", "property")
	 * @param attrName  The attribute name to match on (e.g. "name")
	 * @param attrValue The attribute value to match (case-insensitive)
	 *
	 * @return The matching Element, or null if not found
	 */
	private static Element findChildByAttribute( Element parent, String tagName, String attrName, String attrValue ) {
		NodeList nodes = parent.getElementsByTagName( tagName );
		for ( int i = 0; i < nodes.getLength(); i++ ) {
			Element el = ( Element ) nodes.item( i );
			if ( el.getAttribute( attrName ).equalsIgnoreCase( attrValue ) ) {
				return el;
			}
		}
		return null;
	}

	/**
	 * Get the policy URL from the JAR
	 *
	 * @param policy The policy name
	 *
	 * @return The URL of the policy
	 */
	private static URL getPolicyURL( String policy ) {
		return AntiSamyUtil.class.getResource( policy );
	}
}
