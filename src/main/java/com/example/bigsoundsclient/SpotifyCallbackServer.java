package com.example.bigsoundsclient;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SpotifyCallbackServer implements Runnable {

    static final int PORT = 5173;
    private static final int TIMEOUT_MS = 120_000;

    private final Consumer<String> onCode;
    private final Runnable onError;

    private volatile boolean stopped = false;
    private volatile ServerSocket ss4;
    private volatile ServerSocket ss6;

    private final CountDownLatch readyLatch = new CountDownLatch(1);
    private final BlockingQueue<String> codeQueue = new LinkedBlockingQueue<>(1);

    public SpotifyCallbackServer(Consumer<String> onCode, Runnable onError) {
        this.onCode = onCode;
        this.onError = onError;
    }

    public void waitUntilReady() throws InterruptedException {
        readyLatch.await();
    }

    public void stop() {
        stopped = true;
        readyLatch.countDown();
        codeQueue.offer("__STOP__");
        closeQuietly(ss4);
        closeQuietly(ss6);
    }

    @Override
    public void run() {
        newAcceptor("127.0.0.1", "oauth-ipv4").start();
        newAcceptor("::1",       "oauth-ipv6").start();

        try {
            if (!readyLatch.await(5, TimeUnit.SECONDS) && !stopped) {
                onError.run();
                return;
            }

            String code = codeQueue.poll(TIMEOUT_MS, TimeUnit.MILLISECONDS);

            if (!stopped) {
                if (code != null && !code.equals("__STOP__")) {
                    onCode.accept(code);
                } else {
                    onError.run();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            stop();
        }
    }

    private Thread newAcceptor(String host, String name) {
        Thread t = new Thread(() -> runAcceptor(host), name);
        t.setDaemon(true);
        return t;
    }

    private void runAcceptor(String host) {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress(InetAddress.getByName(host), PORT));

            if (host.contains(":")) ss6 = ss; else ss4 = ss;
            readyLatch.countDown();

            while (!stopped) {
                ss.setSoTimeout(5_000);
                try (Socket socket = ss.accept()) {
                    if (stopped) return;
                    String code = parseSpotifyCode(socket);
                    sendHtmlResponse(socket.getOutputStream(), code != null);
                    if (code != null) {
                        codeQueue.offer(code);
                        return;
                    }
                } catch (SocketTimeoutException ignored) {}
            }
        } catch (Exception e) {
            readyLatch.countDown();
        } finally {
            closeQuietly(ss);
        }
    }

    private static String parseSpotifyCode(Socket socket) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        String requestLine = reader.readLine();
        if (requestLine == null) return null;

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) return null;
        String path = parts[1];

        if (path.contains("?")) {
            for (String param : path.substring(path.indexOf('?') + 1).split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) {
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String val = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    if ("spotifyCode".equals(key) && !val.isBlank()) return val;
                }
            }
        }
        return null;
    }

    private static void sendHtmlResponse(OutputStream out, boolean success) throws IOException {
        String html = success
                ? "<html><head><meta charset='utf-8'/></head>"
                  + "<body style='font-family:sans-serif;text-align:center;padding:60px'>"
                  + "<h2 style='color:#1db954'>&#10003; Autoryzacja udana!</h2>"
                  + "<p>Możesz zamknąć tę kartę i wrócić do aplikacji BigSounds.</p>"
                  + "</body></html>"
                : "<html><head><meta charset='utf-8'/></head>"
                  + "<body style='font-family:sans-serif;text-align:center;padding:60px'>"
                  + "<h2 style='color:#e53e3e'>&#10005; Autoryzacja nieudana.</h2>"
                  + "<p>Zamknij tę kartę i spróbuj ponownie.</p>"
                  + "</body></html>";

        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        pw.print("HTTP/1.1 200 OK\r\n");
        pw.print("Content-Type: text/html; charset=utf-8\r\n");
        pw.print("Content-Length: " + body.length + "\r\n");
        pw.print("Connection: close\r\n");
        pw.print("\r\n");
        pw.flush();
        out.write(body);
        out.flush();
    }

    private static void closeQuietly(ServerSocket ss) {
        try { if (ss != null) ss.close(); } catch (IOException ignored) {}
    }
}
