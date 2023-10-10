package proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

class ParsedHttpRequest {
	public static final String METHOD_GET = "GET";
	public static final String METHOD_PUT = "PUT";
	public static final String HEADER_CONTENT_LENGTH = "Content-length";

	public static final String KEY_SHORT = "short";
	public static final String KEY_LONG = "long";
	public static final String KEY_NEWHOST = "newhost";
	public static final String KEY_OLDHOST = "oldhost";

	private static final int MAX_BODY_SIZE = 1024;

	private String method;
	private Map<String, String> params;
	private Map<String, String> headers;
	private String request;

	public ParsedHttpRequest(BufferedReader in) throws IOException {
		this.params = new HashMap<>();
		this.headers = new HashMap<>();

		parseRequest(in);
	}

	public String getHttpMethod() {
		return this.method;
	}

	public Map<String, String> getParams() {
		return this.params;
	}

	public Map<String, String> getHeaders() {
		return this.headers;
	}

	@Override
	public String toString() {
		return this.request;
	}

	private void parseRequest(BufferedReader in) throws IOException {
		List<String> requestLines = new ArrayList<String>();

		// get first line
		String firstLine = in.readLine();
		String[] parsedFirstLine = firstLine.split(" ", 3);
		this.method = parsedFirstLine[0];
		// get params - parsed manually through string operations
		String target = parsedFirstLine[1];
		if (METHOD_GET.equals(this.method)) {
			// expected: /param1
			String shortURL = target.substring(1, target.length());
			this.params.put(KEY_SHORT, shortURL);
		} else if (METHOD_PUT.equals(this.method)) {
			// expected: /?param1=value1&param2=value2&...
			if (target.indexOf('/') == 0 && target.indexOf('?') == 1) {
				String params = target.substring(2, target.length());
				String[] pairs = params.split("&");
				for (String pair : pairs) {
					String[] keyAndVal = pair.split("=");
					this.params.put(keyAndVal[0], keyAndVal[1]);
				}
			}
		}
		requestLines.add(firstLine);

		// get headers
		String inputLine;
		while (!(inputLine = in.readLine()).equals("")) {
			String[] parsed = inputLine.split(": ", 2);
			this.headers.put(parsed[0], parsed[1]);

			requestLines.add(inputLine);
		}

		// get body
		String contentLength = this.headers.get(HEADER_CONTENT_LENGTH);
		if (this.headers.get(HEADER_CONTENT_LENGTH) != null) {
			int bodySize = Integer.parseInt(contentLength);
			char[] readBuf = new char[MAX_BODY_SIZE];

			in.read(readBuf, 0, bodySize);
			String body = new String(readBuf, 0, bodySize);

			requestLines.add("");
			requestLines.add(body);
		}

		this.request = String.join("\n", requestLines);
	}
}