package proxy;

public class HttpUtil {
  public static String extractMethod(String http) {
    return http.split(" ")[0];
  }

  public static String extractPath(String http) {
    return http.split(" ")[1];
  }

  public static String extractRedirect(String http) {
    for (String line : http.split("\n")) {
      if (line.contains("Location")) {
        return line.split("Location: ")[1];
      }
    }

    System.out.println("something went wrong");
    return "";
  }

  public static String formatRedirect(String redirect) {
    return String.format(
        """
            HTTP/1.1 307 Temporary Redirect
            Location: %s
            Content-length: 0
            \n
            """, redirect);
  }
}