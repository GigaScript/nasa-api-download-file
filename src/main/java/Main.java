import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.URL;

public class Main {
    public static final String DOWNLOAD_DIRECTORY = "C://NasaContentTemp/";
    public static final String API_KEY = "xkpRwbdjLw7nNXyqIsrBBcFzGs1QiWpMm5suzEye";
    public static final String REMOTE_SERVICE_URI = "https://api.nasa.gov/planetary/apod?api_key=" + API_KEY;
    public static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .setUserAgent("Super Agent")
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectTimeout(5000)
                                .setSocketTimeout(30000)
                                .setRedirectsEnabled(false)
                                .build())
                .build();
        HttpGet request = new HttpGet(REMOTE_SERVICE_URI);
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            NasaContent nasaContent = mapper.readValue(
                    response.getEntity().getContent(),
                    NasaContent.class
            );
            String url = nasaContent.getUrl();
            saveMedia(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createContentDirectory() {
        File directory = new File(DOWNLOAD_DIRECTORY);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw new IllegalArgumentException("Not possible create directory in path\n: " + DOWNLOAD_DIRECTORY);
            }
        }
    }

    private static void saveMedia(String fileUrl) throws IOException {
        createContentDirectory();
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1, fileUrl.lastIndexOf('.'));
        String fileExtension = fileUrl.substring(fileUrl.lastIndexOf('.'), fileUrl.length());
        try (
                InputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            byte[] buffer = new byte[1024];
            int n = 0;
            while (-1 != (n = in.read(buffer))) {
                out.write(buffer, 0, n);
            }
            byte[] response = out.toByteArray();
            saveFile(response, DOWNLOAD_DIRECTORY + fileName + fileExtension);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveFile(byte[] fileContent, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
