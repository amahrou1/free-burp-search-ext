# Burp Search Extension

A Burp Suite Community Edition extension that provides a suite-wide search function equivalent to Burp Pro's built-in **Search** feature.

## demo

https://youtu.be/uv6HcbZcrjA

## Features

- **Searches Proxy history and Site map** for HTTP messages matching your expression
- **Regex or literal** matching, with case-sensitivity control
- **Negative match** mode — find items that do NOT contain the expression
- **In-scope-only** filtering via the current Target → Scope
- **Dynamic update** — new Proxy requests are tested live against the active query and appended to results without re-running a full search
- **5 searchable locations** — Request URL, Request headers, Request body, Response headers, Response body (each independently toggleable)
- **Deduplication** — results are grouped by `(method, URL)` with a match count
- **Request/response preview** with search expression highlight (see below)
- **Context menu** — Send to Repeater, Copy URL, Copy as cURL, Add host to scope, Remove from results
- **Status bar** with live progress and search timing

## Build

Requires Java 17+ and Maven.

```bash
mvn clean package
```

Output: `target/burp-search-1.0.0.jar`

## Install

1. Open Burp Suite Community Edition
2. Go to **Extensions → Installed → Add**
3. Select **Java** extension type
4. Browse to `target/burp-search-1.0.0.jar`
5. Click **Next** — a **Search** tab will appear on the Burp tab bar

## Usage

1. Enter a search expression in the text field
2. Configure regex/case/negative options and select locations and sources
3. Click **Search** (or press Enter)
4. Results appear in the table as they are found; click a row to preview the full request and response
5. Right-click a row for additional actions

## Match highlighting in preview editors

The extension calls `setSearchExpression` on the Montoya `HttpRequestEditor` / `HttpResponseEditor` interfaces via reflection. If the Montoya API version bundled with your Burp installation does not expose this method (it was added in a later release), the call is silently skipped and you can use the editor's own built-in search bar (Ctrl+F) to find matches manually.

## Known limitations

- **Repeater tabs, Intruder attack results, and Logger entries are not searchable.** The Montoya API for Burp Community Edition does not expose these sources. Only Proxy history and Site map are available.
- Results are **session-only** — reloading the extension clears all results; there is no persistence.
- The **Dynamic update** queue is capped at 1 000 items; if the queue fills faster than it drains, the oldest items are dropped and a counter is shown in the status bar.

## License

MIT — see LICENSE file (placeholder; replace with actual license text).
