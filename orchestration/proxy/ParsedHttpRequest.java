package proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ParsedHttpRequest {
	public static final String METHOD_GET = "GET";
	public static final String METHOD_PUT = "PUT";
	public static final String HEADER_CONTENT_LENGTH = "Content-length";

	public static final String KEY_SHORT = "short";
	public static final String KEY_LONG = "long";

	private static final Pattern PATTERN_GET_TARGET = Pattern.compile("^/([^\\?=&]+)$");
	private static final Pattern PATTERN_PUT_TARGET = Pattern.compile("^/\\?short=(\\S+)&long=(\\S+)$");
	private static final int MAX_BODY_SIZE = 1024; // should be enough for now

	private String method;
	private HashMap<String, String> params;
	private HashMap<String, String> headers;
	private String request;

	public ParsedHttpRequest(BufferedReader in) throws IOException {
		this.params = new HashMap<>();
		this.headers = new HashMap<>();

		parseRequest(in);
	}

	public String getHttpMethod() {
		return this.method;
	}

	public HashMap<String, String> getParams() {
		return this.params;
	}

	public HashMap<String, String> getHeaders() {
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
		// get params
		String target = parsedFirstLine[1];
		if (METHOD_GET.equals(this.method)) {
			Matcher m = PATTERN_GET_TARGET.matcher(target);
			if (m.matches()) {
				this.params.put(KEY_SHORT, m.group(1));
			}
		} else if (METHOD_PUT.equals(this.method)) {
			Matcher m = PATTERN_PUT_TARGET.matcher(target);
			if (m.matches()) {
				this.params.put(KEY_SHORT, m.group(1));
				this.params.put(KEY_LONG, m.group(2));
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
        if (this.headers.get(HEADER_CONTENT_LENGTH) != null)  {
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