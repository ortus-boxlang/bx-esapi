# ⚡︎ BoxLang Module: ESAPI & Antisamy Module

```
|:------------------------------------------------------:|
| ⚡︎ B o x L a n g ⚡︎
| Dynamic : Modular : Productive
|:------------------------------------------------------:|
```

<blockquote>
	Copyright Since 2023 by Ortus Solutions, Corp
	<br>
	<a href="https://www.boxlang.io">www.boxlang.io</a> |
	<a href="https://www.ortussolutions.com">www.ortussolutions.com</a>
</blockquote>

<p>&nbsp;</p>

This module provides ESAPI functionality for stronger, more secure applications in BoxLang based on the OWASP ESAPI project: https://owasp.org/www-project-enterprise-security-api and OWASP AntiSamy project: https://owasp.org/www-project-antisamy/

It includes encoding, decoding, and sanitization functions to help protect your application from common security vulnerabilities.  It also includes the AntiSamy library for cleaning up HTML content.

```bash
# For Operating Systems using our Quick Installer.
install-bx-module bx-esapi

# Using CommandBox to install for web servers.
box install bx-esapi
```

Once installed, several built-in-functions and components will be available for use in your BoxLang code.

## BIF Overview

Here are the built-in-functions (BIFs) that are available in this module.  You can also check out the module API documentation for more details: https://apidocs.ortussolutions.com/boxlang-modules/bx-esapi/1.1.0/ortus/boxlang/modules/esapi/bifs/package-summary.html

### Encoding

This module contributes the following ESAPI encoding BIFs:

* `encodeFor( context, value )` - Encode for a specific context: HTML, XML, URL, etc.
* `encodeForCSS( value, [canonicalize=false] )` - Encode for CSS contexts
* `encodeForDN( value, [canonicalize=false] )` - Encode for Distinguished Name contexts
* `encodeForHTML( value, [canonicalize=false] )` - Encode for HTML contexts
* `encodeForHTMLAttribute( value, [canonicalize=false] )` - Encode for HTML attribute contexts
* `encodeForJavaScript( value, [canonicalize=false] )` - Encode for JavaScript contexts
* `encodeForLDAP( value, [canonicalize=false] )` - Encode for LDAP contexts
* `encodeForSQL( string, dialect, [canonicalize=false] )` - Encode for SQL contexts
* `encodeForURL( value, [canonicalize=false])` - Encode for URL contexts
* `encodeForXML( value, [canonicalize=false])` - Encode for XML contexts
* `encodeForXMLAttribute( value, [canonicalize=false] )` - Encode for XML attribute contexts
* `encodeForXPath( value, [canonicalize=false])` - Encode for XPath contexts

The available contexts for encoding are:

* `CSS`
* `DN`
* `HTML`
* `HTMLAttribute`
* `JavaScript`
* `LDAP`
* `SQL`
* `URL`
* `XML`
* `XMLAttribute`
* `XPath`

> Canonicalize: If set to true, canonicalization happens before encoding. If set to false, the given input string will just be encoded. The default value for canonicalize is false.

#### Examples

```html
<bx:output>
	<h2>#encodeFor( "HTML", book.title )#</h2>
	<a href="#encodeFor( "HTMLAttribute", book.goodreadsURL )#">Read on Goodreads</a>
</bx:output>

<bx:output>
	<script>
		var user = #encodeFor( "JavaScript", user.name )#;
	</script>
</bx:output>

<bx:output>
	<cfquery name="qBooks" datasource="myDSN">
		SELECT * FROM books WHERE title = #encodeFor( "SQL", form.title )#
	</cfquery>
</bx:output>

<bx:output>
	<a href="search?term=#encodeForURL( form.searchTerm, true )#">Search</a>
</bx:output>
```

### Decoding

This module contributes the following ESAPI decoding BIFs:

* `canonicalize( input, restrictMultiple, restrictMixed, [throwOnError=false])` - Canonicalize or decode the input string. Canonicalization is simply the operation of reducing a possibly encoded string down to its simplest form. This is important, as attackers frequently use encoding to change their input in a way that will bypass validation filters.
  * `input` - The input string to canonicalize
  * `restrictMultiple` - If true, multiple encoding is restricted. This argument can be set to true to restrict the input if multiple or nested encoding is detected. If this argument is set to true, and the given input is multiple or nested encoded using one encoding scheme an error will be thrown
  * `restrictMixed` - If true, mixed encoding is restricted. This argument can be set to true to restrict the input if mixed encoding is detected. If this argument is set to true, and the given input is mixed encoded using multiple encoding schemes an error will be thrown
  * `throwOnError` - If true, an error will be thrown if the input is invalid. If this argument is set to true, and the given input is invalid, an error will be thrown. Default is false.
* `decodeFor( type, value )` - Decode for a specific context: HTML, XML, URL, etc.
* `decodeForBase64( string )` - Decodes a Base64 string
* `decodeForHTML( string )` - Decodes an HTML string
* `decodeForJSON( string )` - Decodes a JSON string
* `decodeFromURL( string )` - Decodes a URL-encoded string

#### Examples

```html
<bx:output>#canonicalize( "&lt;", false, false )#</bx:output>

<bx:output>
	<h2>#canonicalize( book.title, true, true )#</h2>
</bx:output>

<bx:output>
	<script>
		var user = #decodeFor( "JavaScript", user.name )#;
	</script>
</bx:output>

<bx:output>
	<a href="search?term=#decodeFromURL( form.searchTerm )#">Search</a>
</bx:output>
```

### Antisamy + Sanitization

This module contributes these remaining ESAPI BIFs:

* `getSafeHTML( string, [policy='ebay'], [throwOnError=false] )` - Sanitize HTML content using the AntiSamy library
  * `string` - The HTML string to sanitize
  * `policy` - The policy to use for sanitization.  Can be a **string** (named policy or file path) or a **struct** for programmatic policy configuration.  The default is `'ebay'`.
    * **String values**: `anythinggoes`, `ebay`, `myspace`, `slashdot`, `tinymce`, or an absolute path to a custom XML policy file.
    * **Struct values**: See [Struct Policy Configuration](#struct-policy-configuration) below.
  * `throwOnError` - If `true`, throws an exception when the HTML violates the policy rules.  If `false` (default), silently returns the sanitized HTML.
* `isSafeHTML( string, [policy='ebay'] )` - Validate HTML content using the AntiSamy library
  * `string` - The HTML string to validate
  * `policy` - Same as `getSafeHTML()` above.  Can be a string or struct.
* `sanitizeHTML( string, [policy='ALL'] )` - Sanitizes unsafe HTML to protect against XSS attacks (uses the OWASP Java HTML Sanitizer, not AntiSamy)
  * `string` - The HTML string to sanitize
  * `policy` - The policy to use for sanitization. The default is 'ALL', which is the most restrictive policy. The available policies are: `BLOCKS, FORMATTING, IMAGES, LINKS, STYLES, TABLES`.  You can also pass a `PolicyFactory` object to use a custom policy (https://javadoc.io/static/com.googlecode.owasp-java-html-sanitizer/owasp-java-html-sanitizer/20191001.1/org/owasp/html/PolicyFactory.html)

#### Examples

```html
<bx:output>
	#sanitizeHTML( book.description )#
</bx:output>

<bx:if isSafeHTML( form.comment )>
	<bx:output>
		#form.comment#
	</bx:output>
<bx:else>
	<bx:output>
		<p>Comment contains unsafe HTML</p>
	</bx:output>
</bx:if>

<bx:output>
	#sanitizeHTML( "<h1>Hello World</h1><script>alert('XSS')</script>" )#
</bx:output>

<bx:script>
	// Use a named policy
	comment = getSafeHTML( form.comment, "myspace" );

	// Use a custom XML file
	comment = getSafeHTML( form.comment, "C:/path/to/policy.xml" );

	// Override a built-in policy's directives
	comment = getSafeHTML( form.comment, {
		basePolicy: "ebay",
		directives: { maxInputSize: 500000 }
	} );

	// Override tag rules on a built-in policy (merge mode is default)
	comment = getSafeHTML( form.comment, {
		basePolicy: "ebay",
		tagRules: {
			"b": "remove",
			"iframe": "validate"
		}
	} );

	// Build a policy entirely from scratch - no base policy
	comment = getSafeHTML( form.comment, {
		directives: {
			maxInputSize: 100000,
			omitXmlDeclaration: "true",
			omitDoctypeDeclaration: "true",
			formatOutput: "true"
		},
		allowTags: [ "b", "i", "em", "strong", "p", "br", "ul", "ol", "li" ]
	} );

	// Validate HTML with a struct policy
	if( isSafeHTML( form.comment, { basePolicy: "slashdot" } ) ) {
		// comment is safe
	}
</bx:script>
```

### Struct Policy Configuration

Instead of writing XML policy files, you can pass a struct to `getSafeHTML()` or `isSafeHTML()` to configure an AntiSamy policy programmatically.  You can either override an existing built-in policy or build an entire policy from scratch.

#### Struct Keys

| Key | Type | Description |
|-----|------|-------------|
| `basePolicy` | String | Name of a built-in policy (`ebay`, `slashdot`, etc.) or file path to use as the starting point.  If omitted, a blank policy is created from scratch. |
| `overrideMode` | String | `"merge"` (default) or `"override"`.  In merge mode, your struct keys are layered on top of the base policy.  In override mode, entire sections are replaced. |
| `directives` | Struct | Key/value pairs for AntiSamy directives like `maxInputSize`, `omitXmlDeclaration`, `formatOutput`, etc. |
| `allowTags` | Array | Shorthand list of tag names to allow with a `"validate"` action.  Sugar for `tagRules`. |
| `tagRules` | Struct | Tag name to either an action string (`"validate"`, `"filter"`, `"remove"`, `"truncate"`) or a struct with `action` and `attributes`. |
| `globalAttributes` | Struct | Attribute definitions that apply to all allowed tags.  Each key is an attribute name, each value is a config struct with `regexps`, `allowedValues`, `onInvalid`, `description`. |
| `dynamicAttributes` | Struct | Attribute patterns (e.g. `"data-.*"`) with the same config as `globalAttributes`. |
| `cssRules` | Struct | CSS property name to config struct with `regexps`, `allowedValues`, `shorthandRefs`, `description`. |
| `allowedEmptyTags` | Array | Tag names that are allowed to be self-closing / empty (e.g. `["br", "hr", "img"]`). |
| `requireClosingTags` | Array | Tag names that require explicit closing tags. |
| `tagsToEncode` | Array | Tag names that should be HTML-entity-encoded rather than removed. |

#### Tag Rule Struct Format

When a tag rule value is a struct instead of a simple action string, it can define per-tag attribute rules:

```javascript
tagRules: {
	"a": {
		action: "validate",
		attributes: {
			"href": {
				regexps: [ "https?://[^\\s]*" ],
				onInvalid: "removeTag"
			},
			"rel": {
				allowedValues: [ "nofollow", "noopener" ]
			}
		}
	},
	"img": {
		action: "validate",
		attributes: {
			"src": {
				regexps: [ "https?://[^\\s]*" ],
				onInvalid: "removeTag"
			},
			"alt": {
				regexps: [ "[\\p{L}\\p{N}\\s\\-_',\\.]*" ]
			}
		}
	},
	"script": "remove",
	"b": "validate"
}
```

#### Struct Equivalents of Built-In Policies

Below are struct representations of two of the built-in AntiSamy policies.  These produce equivalent behavior to using the named string policy and can serve as a starting point for your own custom policies.

##### Slashdot Policy (`"slashdot"`)

The Slashdot policy is very restrictive - basic formatting, links, and lists only.  No CSS, no images.

```javascript
slashdotPolicy = {
	directives: {
		omitXmlDeclaration: "true",
		omitDoctypeDeclaration: "true",
		maxInputSize: 5000,
		formatOutput: "true",
		embedStyleSheets: "false"
	},
	globalAttributes: {
		"title": {
			regexps: [ "[\\p{L}\\p{N}\\s\\-_',:\\[\\]!\\./\\\\\\(\\)&]*" ]
		},
		"lang": {
			regexps: [ "[a-zA-Z0-9-]{2,20}" ]
		}
	},
	tagRules: {
		"script": "remove",
		"noscript": "remove",
		"iframe": "remove",
		"frameset": "remove",
		"frame": "remove",
		"noframes": "remove",
		"style": "remove",
		"p": {
			action: "validate",
			attributes: {
				"align": {
					allowedValues: [ "center", "left", "right", "justify", "char" ]
				}
			}
		},
		"div": "validate",
		"i": "validate",
		"b": "validate",
		"em": "validate",
		"blockquote": "validate",
		"tt": "validate",
		"strong": "validate",
		"br": "truncate",
		"quote": "validate",
		"ecode": "validate",
		"a": {
			action: "validate",
			attributes: {
				"href": {
					regexps: [
						"^(?!//)(?![\\p{L}\\p{N}\\\\\\.\\#@\\$%\\+&;\\-_~,\\?=/!]*(&colon))[\\p{L}\\p{N}\\\\\\.\\#@\\$%\\+&;\\-_~,\\?=/!]*",
						"(\\s)*((ht|f)tp(s?)://|mailto:)[\\p{L}\\p{N}]+[~\\p{L}\\p{N}\\p{Zs}\\-_\\.@\\#\\$%&;:,\\?=/\\+!\\(\\)]*(\s)*"
					],
					onInvalid: "filterTag"
				},
				"nohref": {
					allowedValues: [ "nohref", "" ]
				},
				"rel": {
					allowedValues: [ "nofollow" ]
				}
			}
		},
		"ul": "validate",
		"ol": "validate",
		"li": "validate"
	},
	tagsToEncode: [ "g", "grin" ],
	allowedEmptyTags: [
		"br", "hr", "a", "img", "link", "iframe", "script", "object",
		"applet", "frame", "base", "param", "meta", "input", "textarea",
		"embed", "basefont", "col", "div"
	]
};

// Usage:
comment = getSafeHTML( form.comment, slashdotPolicy );
```

##### eBay Policy (`"ebay"`)

The eBay policy is more permissive - allows formatting, tables, images, links, CSS styling, and many more tags.

```javascript
ebayPolicy = {
	directives: {
		omitXmlDeclaration: "true",
		omitDoctypeDeclaration: "true",
		maxInputSize: 20000,
		formatOutput: "true",
		embedStyleSheets: "false"
	},
	globalAttributes: {
		"id": {
			regexps: [ "[a-zA-Z0-9:\\-_\\.]+" ]
		},
		"style": {},
		"title": {
			regexps: [ "[\\p{L}\\p{N}\\s\\-_',:\\[\\]!\\./\\\\\\(\\)&]*" ]
		},
		"class": {
			regexps: [ "[a-zA-Z0-9\\s,\\-_]+" ]
		},
		"lang": {
			regexps: [ "[a-zA-Z0-9-]{2,20}" ]
		}
	},
	tagRules: {
		"script": "remove",
		"noscript": "validate",
		"iframe": "remove",
		"frameset": "remove",
		"frame": "remove",
		"label": {
			action: "validate",
			attributes: {
				"for": {
					regexps: [ "[a-zA-Z0-9:\\-_\\.]+" ]
				}
			}
		},
		"h1": "validate",
		"h2": "validate",
		"h3": "validate",
		"h4": "validate",
		"h5": "validate",
		"h6": "validate",
		"p": {
			action: "validate",
			attributes: {
				"align": {
					allowedValues: [ "center", "middle", "left", "right", "justify", "char" ]
				}
			}
		},
		"i": "validate",
		"b": "validate",
		"u": "validate",
		"strong": "validate",
		"em": "validate",
		"small": "validate",
		"big": "validate",
		"pre": "validate",
		"code": "validate",
		"cite": "validate",
		"samp": "validate",
		"sub": "validate",
		"sup": "validate",
		"strike": "validate",
		"center": "validate",
		"blockquote": "validate",
		"hr": "validate",
		"br": "validate",
		"font": {
			action: "validate",
			attributes: {
				"color": {
					regexps: [
						"(aqua|black|blue|fuchsia|gray|grey|green|lime|maroon|navy|olive|purple|red|silver|teal|white|yellow)",
						"(#([0-9a-fA-F]{6}|[0-9a-fA-F]{3}))"
					]
				},
				"face": {
					regexps: [ "[\\w;, \\-]+" ]
				},
				"size": {
					regexps: [ "(\\+|-){0,1}(\\d)+" ]
				}
			}
		},
		"a": {
			action: "validate",
			attributes: {
				"href": {
					regexps: [
						"^(?!//)(?![\\p{L}\\p{N}\\\\\\.\\#@\\$%\\+&;\\-_~,\\?=/!]*(&colon))[\\p{L}\\p{N}\\\\\\.\\#@\\$%\\+&;\\-_~,\\?=/!]*",
						"(\\s)*((ht|f)tp(s?)://|mailto:)[\\p{L}\\p{N}]+[\\p{L}\\p{N}\\p{Zs}\\.\\#@\\$%\\+&;:\\-_~,\\?=/!\\(\\)]*(\s)*"
					]
				},
				"nohref": {
					regexps: [ ".*" ]
				},
				"rel": {
					allowedValues: [ "nofollow" ]
				},
				"name": {
					regexps: [ "[a-zA-Z0-9\\-_\\$]+" ]
				}
			}
		},
		"map": "validate",
		"style": {
			action: "validate",
			attributes: {
				"type": {
					allowedValues: [ "text/css" ]
				}
			}
		},
		"span": "validate",
		"div": {
			action: "validate",
			attributes: {
				"align": {
					allowedValues: [ "center", "middle", "left", "right", "justify", "char" ]
				}
			}
		},
		"img": {
			action: "validate",
			attributes: {
				"src": {
					regexps: [
						"^(?!//)(?![\\p{L}\\p{N}\\\\\\.\\#@\\$%\\+&;\\-_~,\\?=/!]*(&colon))[\\p{L}\\p{N}\\\\\\.\\#@\\$%\\+&;\\-_~,\\?=/!]*",
						"(\\s)*((ht|f)tp(s?)://|mailto:)[\\p{L}\\p{N}]+[\\p{L}\\p{N}\\p{Zs}\\.\\#@\\$%\\+&;:\\-_~,\\?=/!\\(\\)]*(\s)*"
					],
					onInvalid: "removeTag"
				},
				"name": {
					regexps: [ "[a-zA-Z0-9\\-_\\$]+" ]
				},
				"alt": {
					regexps: [ "[\\p{L}\\p{N},'\\. \\s\\-_\\(\\)&;]*" ]
				},
				"height": { regexps: [ "(\\d)+(%{0,1})" ] },
				"width": { regexps: [ "(\\d)+(%{0,1})" ] },
				"border": { regexps: [ "(-|\\+)?([0-9]+(\\.[0-9]+)?)" ] },
				"align": {
					allowedValues: [ "center", "middle", "left", "right", "justify", "char" ]
				},
				"hspace": { regexps: [ "(-|\\+)?([0-9]+(\\.[0-9]+)?)" ] },
				"vspace": { regexps: [ "(-|\\+)?([0-9]+(\\.[0-9]+)?)" ] }
			}
		},
		"link": {
			action: "validate",
			attributes: {
				"type": {
					allowedValues: [ "text/css", "application/rss+xml", "image/x-icon" ],
					onInvalid: "removeTag"
				},
				"rel": {
					allowedValues: [ "stylesheet", "shortcut icon", "search", "copyright", "top", "alternate" ]
				}
			}
		},
		"ul": "validate",
		"ol": "validate",
		"li": "validate",
		"dd": "truncate",
		"dl": "truncate",
		"dt": "truncate",
		"thead": {
			action: "validate",
			attributes: {
				"align": {
					allowedValues: [ "center", "middle", "left", "right", "justify", "char" ]
				}
			}
		},
		"tbody": {
			action: "validate",
			attributes: {
				"align": {
					allowedValues: [ "center", "middle", "left", "right", "justify", "char" ]
				}
			}
		},
		"tfoot": {
			action: "validate",
			attributes: {
				"align": {
					allowedValues: [ "center", "middle", "left", "right", "justify", "char" ]
				}
			}
		},
		"table": {
			action: "validate",
			attributes: {
				"height": { regexps: [ "(\\d)+(%{0,1})" ] },
				"width": { regexps: [ "(\\d)+(%{0,1})" ] },
				"border": { regexps: [ "(-|\\+)?([0-9]+(\\.[0-9]+)?)" ] },
				"bgcolor": {
					regexps: [
						"(aqua|black|blue|fuchsia|gray|grey|green|lime|maroon|navy|olive|purple|red|silver|teal|white|yellow)",
						"(#([0-9a-fA-F]{6}|[0-9a-fA-F]{3}))"
					]
				},
				"cellpadding": { regexps: [ "(-|\\+)?([0-9]+(\\.[0-9]+)?)" ] },
				"cellspacing": { regexps: [ "(-|\\+)?([0-9]+(\\.[0-9]+)?)" ] },
				"align": {
					allowedValues: [ "center", "middle", "left", "right", "justify", "char" ]
				}
			}
		},
		"td": {
			action: "validate",
			attributes: {
				"align": {
					allowedValues: [ "center", "middle", "left", "right", "justify", "char" ]
				},
				"bgcolor": {
					regexps: [
						"(aqua|black|blue|fuchsia|gray|grey|green|lime|maroon|navy|olive|purple|red|silver|teal|white|yellow)",
						"(#([0-9a-fA-F]{6}|[0-9a-fA-F]{3}))"
					]
				},
				"height": { regexps: [ "(\\d)+(%{0,1})" ] },
				"width": { regexps: [ "(\\d)+(%{0,1})" ] },
				"colspan": { regexps: [ "(-|\\+)?([0-9]+(\\.[0-9]+)?)" ] },
				"rowspan": { regexps: [ "(-|\\+)?([0-9]+(\\.[0-9]+)?)" ] }
			}
		},
		"th": {
			action: "validate",
			attributes: {
				"align": {
					allowedValues: [ "center", "middle", "left", "right", "justify", "char" ]
				},
				"bgcolor": {
					regexps: [
						"(aqua|black|blue|fuchsia|gray|grey|green|lime|maroon|navy|olive|purple|red|silver|teal|white|yellow)",
						"(#([0-9a-fA-F]{6}|[0-9a-fA-F]{3}))"
					]
				},
				"height": { regexps: [ "(\\d)+(%{0,1})" ] },
				"width": { regexps: [ "(\\d)+(%{0,1})" ] },
				"colspan": { regexps: [ "(-|\\+)?([0-9]+(\\.[0-9]+)?)" ] },
				"rowspan": { regexps: [ "(-|\\+)?([0-9]+(\\.[0-9]+)?)" ] }
			}
		},
		"tr": {
			action: "validate",
			attributes: {
				"height": { regexps: [ "(\\d)+(%{0,1})" ] },
				"width": { regexps: [ "(\\d)+(%{0,1})" ] },
				"align": {
					allowedValues: [ "center", "middle", "left", "right", "justify", "char" ]
				}
			}
		},
		"colgroup": {
			action: "validate",
			attributes: {
				"span": { regexps: [ "(-|\\+)?([0-9]+(\\.[0-9]+)?)" ] },
				"width": { regexps: [ "(\\d)+(%{0,1})" ] },
				"align": {
					allowedValues: [ "center", "middle", "left", "right", "justify", "char" ]
				}
			}
		},
		"col": {
			action: "validate",
			attributes: {
				"align": {
					allowedValues: [ "center", "middle", "left", "right", "justify", "char" ]
				},
				"span": { regexps: [ "(-|\\+)?([0-9]+(\\.[0-9]+)?)" ] },
				"width": { regexps: [ "(\\d)+(%{0,1})" ] }
			}
		},
		"fieldset": "validate",
		"legend": "validate"
	},
	tagsToEncode: [ "g", "grin" ],
	allowedEmptyTags: [
		"br", "hr", "a", "img", "link", "iframe", "script", "object",
		"applet", "frame", "base", "param", "meta", "input", "textarea",
		"embed", "basefont", "col", "div"
	]
};

// Usage:
comment = getSafeHTML( form.comment, ebayPolicy );
```

----

## Ortus Sponsors

BoxLang is a professional open-source project and it is completely funded by the [community](https://patreon.com/ortussolutions) and [Ortus Solutions, Corp](https://www.ortussolutions.com). Ortus Patreons get many benefits like a cfcasts account, a FORGEBOX Pro account and so much more. If you are interested in becoming a sponsor, please visit our patronage page: [https://patreon.com/ortussolutions](https://patreon.com/ortussolutions)

### THE DAILY BREAD

> "I am the way, and the truth, and the life; no one comes to the Father, but by me (JESUS)" Jn 14:1-12
