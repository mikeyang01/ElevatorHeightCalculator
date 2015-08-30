package jp.locky.android.elevator.net.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import org.apache.http.NameValuePair;

public class HttpMultiPost {
	private static final String BOUNDARY = "----------V2ymHFg03ehbqgZCaKO6jy";
	private String url;
	private List<NameValuePair> postData;

	/** 构造方法 */
	public HttpMultiPost(String url, List<NameValuePair> postData) {
		this.url = url;
		this.postData = postData;
	}

	public String send() {
		URLConnection conn = null;
		String res = null;
		try {
			/** start connect */
			conn = new URL(url).openConnection();
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(5000);
			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + BOUNDARY);
			// User Agentの設定はAndroid1.6の場合のみ必要
			conn.setRequestProperty("User-Agent", "Android");
			// HTTP POSTのための設定
			((HttpURLConnection) conn).setRequestMethod("POST");
			conn.setDoOutput(true);
			// HTTP接続開始
			conn.connect();

			/** send data */
			OutputStream os = conn.getOutputStream();
			os.write(createFormDataMessage().getBytes());// send the string data
			// saveToFile(createBoundaryMessage(file.getName()).getBytes());
			String endBoundary = "\r\n--" + BOUNDARY + "--\r\n";
			os.write(endBoundary.getBytes());
			os.close();
			/**get response*/
			InputStream is = conn.getInputStream();
			res = convertToString(is);
			return res;
		} catch (Exception e) {
		//	Log.d("error:HttpMultipartRequest:", e.getMessage());
		} finally {
			if (conn != null)
				((HttpURLConnection) conn).disconnect();
		}
		return res;
	}

	/** get filename output the string */
	private String createFormDataMessage() {
		StringBuffer res = new StringBuffer("--").append(BOUNDARY).append(
				"\r\n");
		for (NameValuePair nv : postData) {
			res.append("Content-Disposition: form-data; name=\"")
					.append(nv.getName()).append("\"\r\n").append("\r\n")
					.append(nv.getValue()).append("\r\n").append("--")
					.append(BOUNDARY).append("\r\n");
		}
		return res.toString();
	}

	private String convertToString(InputStream stream) {
		InputStreamReader streamReader = null;
		BufferedReader bufferReader = null;
		try {
			streamReader = new InputStreamReader(stream, "UTF-8");
			bufferReader = new BufferedReader(streamReader);
			StringBuilder builder = new StringBuilder();
			for (String line = null; (line = bufferReader.readLine()) != null;) {
				builder.append(line).append("\n");
			}
			return builder.toString();
		} catch (UnsupportedEncodingException e) {
			// Log.e("HttpMultipartRequest:", e.getMessage());
		} catch (IOException e) {
			// Log.e("HttpMultipartRequest:", e.toString());
		} finally {
			try {
				stream.close();
				if (bufferReader != null)
					bufferReader.close();
			} catch (IOException e) {
				//
			}
		}
		return null;
	}
}