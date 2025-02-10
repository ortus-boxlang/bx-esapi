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

Once installed, several built-in-functions and components will be available for use in your BoxLang code.

## BIFs

Here are the built-in-functions (BIFs) that are available in this module:

### Encoding

This module contributes the following ESAPI encoding BIFs:

* `encodeFor()` - see [cfdocs.org/encodeFor](https://cfdocs.org/encodeFor)
* `esapiEncode()` - see [cfdocs.org/esapiEncode](https://cfdocs.org/esapiEncode)
* `encodeForSQL()` - see [cfdocs.org/encodeForSQL](https://cfdocs.org/encodeForSQL)
* `encodeForCSS()` - see [cfdocs.org/encodeForCSS](https://cfdocs.org/encodeForCSS)
* `encodeForDN()` - see [cfdocs.org/encodeForDN](https://cfdocs.org/encodeForDN)
* `encodeForHTML()` - see [cfdocs.org/encodeForHTML](https://cfdocs.org/encodeForHTML)
* `encodeForHTMLAttribute()` - see [cfdocs.org/encodeForHTMLAttribute](https://cfdocs.org/encodeForHTMLAttribute)
* `encodeForJavaScript()` - see [cfdocs.org/encodeForJavaScript](https://cfdocs.org/encodeForJavaScript)
* `encodeForLDAP()` - see [cfdocs.org/encodeForLDAP](https://cfdocs.org/encodeForLDAP)
* `encodeForURL()` - see [cfdocs.org/encodeForURL](https://cfdocs.org/encodeForURL)
* `encodeForXML()` - see [cfdocs.org/encodeForXML](https://cfdocs.org/encodeForXML)
* `encodeForXMLAttribute()` - see [cfdocs.org/encodeForXMLAttribute](https://cfdocs.org/encodeForXMLAttribute)
* `encodeForXPath()` - see [cfdocs.org/encodeForXPath](https://cfdocs.org/encodeForXPath)

### Decoding

This module contributes the following ESAPI decoding BIFs:

* `canonicalize()` - see [cfdocs.org/canonicalize](https://cfdocs.org/canonicalize)
* `decodeFor()`
* `esapiDecode()` - see [cfdocs.org/esapiDecode](https://cfdocs.org/esapiDecode)
* `decodeForBase64()` - see [cfdocs.org/decodeForBase64](https://cfdocs.org/decodeForBase64)
* `decodeForHTML()` - see [cfdocs.org/decodeForHTML](https://cfdocs.org/decodeForHTML)
* `decodeForJSON()` - see [cfdocs.org/decodeForJSON](https://cfdocs.org/decodeForJSON)
* `decodeFromURL()` - see [cfdocs.org/decodeFromURL](https://cfdocs.org/decodeFromURL)

### Other

This module contributes these remaining ESAPI BIFs:

* `getSafeHTML()` - see [cfdocs.org/getSafeHTML](https://cfdocs.org/getSafeHTML)
* `isSafeHTML()` - see [cfdocs.org/isSafeHTML](https://cfdocs.org/isSafeHTML)
* `sanitizeHTML()` - see [cfdocs.org/sanitizeHTML](https://cfdocs.org/sanitizeHTML)

## Component

This module contains no BoxLang Components.

## Examples

Encode user-supplied data in HTML to avoid XSS vulnerabilities:

```html
<bx:output>
	<h2>#encodeForHTML( book.title )#</h2>
	<a href="#encodeForHTMLAttribute( book.goodreadsURL )#">Read on Goodreads</a>
</bx:output>
```

## Ortus Sponsors

BoxLang is a professional open-source project and it is completely funded by the [community](https://patreon.com/ortussolutions) and [Ortus Solutions, Corp](https://www.ortussolutions.com). Ortus Patreons get many benefits like a cfcasts account, a FORGEBOX Pro account and so much more. If you are interested in becoming a sponsor, please visit our patronage page: [https://patreon.com/ortussolutions](https://patreon.com/ortussolutions)

### THE DAILY BREAD

> "I am the way, and the truth, and the life; no one comes to the Father, but by me (JESUS)" Jn 14:1-12
