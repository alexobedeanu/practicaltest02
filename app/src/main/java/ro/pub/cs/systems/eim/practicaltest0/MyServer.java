package ro.pub.cs.systems.eim.practicaltest0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

public class MyServer extends Thread {

    private int port;
    private ServerSocket serverSocket;

    private String cacheUpdatedTime;
    private String cacheRateUSD;
    private String cacheRateEUR;
    private long cacheUpdatedTimeMillis;

    public MyServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                String line = in.readLine();
                if (line != null) {
                    if (cacheUpdatedTime == null || cacheUpdatedTimeMillis + TimeUnit.MINUTES.toMillis(1) < System.currentTimeMillis()) {
                        String json = fetchDataFromCoindesk();
                        parseJson(json);
                    }

                    if ("usd".equalsIgnoreCase(line)) {
                        out.println(cacheRateUSD);
                    } else if ("eur".equalsIgnoreCase(line)) {
                        // If EUR rate is not in the json, change this to show an error message
                        out.println("EUR rate is not available.");
                        out.println(cacheRateEUR);
                    } else {
                        out.println("Invalid request. Please send 'usd' or 'eur'.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String fetchDataFromCoindesk() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.coindesk.com/v1/bpi/currentprice/BTC.json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private void parseJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            cacheUpdatedTime = jsonObject.getJSONObject("time").getString("updated");
            cacheUpdatedTimeMillis = System.currentTimeMillis();
            JSONObject bpi = jsonObject.getJSONObject("bpi");
            cacheRateUSD = bpi.getJSONObject("USD").getString("rate");
            cacheRateEUR = bpi.getJSONObject("EUR").getString("rate");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
