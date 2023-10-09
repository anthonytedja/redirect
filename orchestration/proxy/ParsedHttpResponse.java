package proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

class ParsedHttpResponse {
	public static final String STATUS_307= "307";
	public static final String STATUS_404 = "404";
	public static final String HEADER_CONTENT_LENGTH = "Content-length";

	private static final int MAX_BODY_SIZE = 1024; // should be enough for now
	
	private String protocol;
	private String statusCode;

	private Map<String, String> headers;

	private String response;

	public ParsedHttpResponse(BufferedReader in) throws IOException {
		this.headers = new HashMap<>();
		
		parseResponse(in);
	}

	public String getProtocol() {
		return this.protocol;
	}

	public String getStatusCode() {
		return this.statusCode;
	}

	public Map<String, String> getHeaders() {
		return this.headers;
	}

	@Override
	public String toString() {
		return this.response;
	}

	private void parseResponse(BufferedReader in) throws IOException {
		List<String> responseLines = new ArrayList<String>();

        // get first line
        String firstLine = in.readLine();
		String[] parsedFirstLine = firstLine.split(" ", 3);
		this.statusCode = parsedFirstLine[1];
        responseLines.add(firstLine);

        // get headers
		String inputLine;
        while (!(inputLine = in.readLine()).equals("")) {
			String[] parsed = inputLine.split(": ", 2);
            this.headers.put(parsed[0], parsed[1]);

            responseLines.add(inputLine);
        }

        // get body
        String contentLength = this.headers.get(HEADER_CONTENT_LENGTH);
        if (this.headers.get(HEADER_CONTENT_LENGTH) != null)  {
            int bodySize = Integer.parseInt(contentLength);
            char[] readBuf = new char[MAX_BODY_SIZE];

            in.read(readBuf, 0, bodySize);
            String body = new String(readBuf, 0, bodySize);

            responseLines.add("");
            responseLines.add(body);
        }

        this.response = String.join("\n", responseLines);
	}
}