# 请注意，在使⽤该代码前，需要替换 API 密钥（apiKey）和代理服务器的地址和端⼝（proxy_host 和 proxy_port）。

- 1 public class ChatGPTExample {

- 2 public static void main(String[] args) throws IOException {

- 3 BufferedReader reader

- 4 reader = new BufferedReader(new InputStreamReader(System.in));

- 5 System.out.print("Enter your question: ");

- 6 String question = reader.readLine();

- 7

- 8 // Replace the API key with your own key

- 9 String apiKey = "your_api_key";

- 10 String prompt = question;

- 11 String endpoint = "https://api.openai.com/v1/engines/davinci/jobs";

- 12

- 13 URL url = new URL(endpoint);

Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy_ho st", proxy_port));

- 14

- 15 HttpsURLConnection con = (HttpsURLConnection) url.openConnection(proxy);

- 16 con.setRequestMethod("POST");

- 17 con.setRequestProperty("Authorization", "Bearer " + apiKey);

- 18 con.setRequestProperty("Content-Type", "application/json");

- 19 con.setDoOutput(true);

- 20

- 21 String jsonInputString = "{%"prompt%": %"" + prompt + "%"}";

- 22 try(OutputStream os = con.getOutputStream()) {

- 23 byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);

- 24 os.write(input, 0, input.length);

- 25 }

- 26

- 27 int responseCode = con.getResponseCode();

- 28 if (responseCode == HttpURLConnection.HTTP_OK) {

BufferedReader br = new BufferedReader(new InputStreamReader(con.get InputStream(), StandardCharsets.UTF_8));

- 29

- 30 StringBuilder response = new StringBuilder();

- 31 String responseLine = null;

- 32 while ((responseLine = br.readLine()) != null) {

- 33 response.append(responseLine.trim());

- 34 }

- 35 System.out.println("Response: " + response.toString());

- 36 } else {

- 37 System.out.println("Response code: " + responseCode);

- 38 }


- 39 }

- 40 }


