import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.awt.Desktop;
import java.net.URI;

public class TradingServer {

    static Map<String, Stock> market = Map.of(
            "TCS", new Stock("TCS", "Tata Consultancy Services", 3800.00),
            "INFY", new Stock("INFY", "Infosys Ltd", 1550.00),
            "RELI", new Stock("RELI", "Reliance Industries", 2900.00),
            "HDFC", new Stock("HDFC", "HDFC Bank", 1650.00),
            "WIPRO", new Stock("WIPRO", "Wipro Ltd", 480.00)
    );
    static Map<String, Integer> portfolio = new HashMap<>();
    static Map<String, Double> invested = new HashMap<>();
    static List<Transaction> history = new ArrayList<>();
    static double cash = 100000.0;

    public static void main(String[] args) throws IOException, java.net.URISyntaxException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", ex -> respond(ex, PageHtml.PAGE, "text/html"));
        server.createContext("/api/data", ex -> respond(ex, dataJson(), "application/json"));
        server.createContext("/api/buy", ex -> handleTrade(ex, true));
        server.createContext("/api/sell", ex -> handleTrade(ex, false));
        server.start();
        System.out.println("Server started at http://localhost:8080");
        Desktop.getDesktop().browse(new URI("http://localhost:8080"));
    }

    static void handleTrade(HttpExchange ex, boolean isBuy) throws IOException {
        Map<String, String> params = queryToMap(ex.getRequestURI().getQuery());
        String symbol = params.get("symbol");
        int qty = Integer.parseInt(params.getOrDefault("qty", "0"));
        Stock s = market.get(symbol);
        String message;

        if (s == null || qty <= 0) {
            message = "Invalid request";
        } else if (isBuy) {
            double cost = qty * s.price();
            if (cost > cash) message = "Insufficient balance";
            else {
                cash -= cost;
                portfolio.merge(symbol, qty, Integer::sum);
                invested.merge(symbol, cost, Double::sum);
                history.add(new Transaction("BUY", symbol, qty, s.price()));
                message = "Bought " + qty + " " + symbol;
            }
        } else {
            int owned = portfolio.getOrDefault(symbol, 0);
            if (qty > owned) message = "You don't own that many shares";
            else {
                double price = s.price();
                cash += qty * price;
                double avgCost = invested.get(symbol) / owned;
                invested.merge(symbol, -avgCost * qty, Double::sum);
                if (qty == owned) { portfolio.remove(symbol); invested.remove(symbol); }
                else portfolio.put(symbol, owned - qty);
                history.add(new Transaction("SELL", symbol, qty, price));
                message = "Sold " + qty + " " + symbol;
            }
        }
        respond(ex, "{\"message\":\"" + message + "\"}", "application/json");
    }

    static Map<String, String> queryToMap(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            map.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return map;
    }

    static String dataJson() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"cash\":").append(cash).append(",");

        sb.append("\"market\":[");
        List<Stock> stocks = new ArrayList<>(market.values());
        for (int i = 0; i < stocks.size(); i++) {
            Stock s = stocks.get(i);
            sb.append(String.format("{\"symbol\":\"%s\",\"name\":\"%s\",\"price\":%.2f}", s.symbol(), s.name(), s.price()));
            if (i < stocks.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"portfolio\":[");
        List<String> symbols = new ArrayList<>(portfolio.keySet());
        for (int i = 0; i < symbols.size(); i++) {
            String sym = symbols.get(i);
            int qty = portfolio.get(sym);
            double price = market.get(sym).price();
            double value = qty * price;
            double inv = invested.get(sym);
            sb.append(String.format("{\"symbol\":\"%s\",\"qty\":%d,\"value\":%.2f,\"pl\":%.2f}", sym, qty, value, value - inv));
            if (i < symbols.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"history\":[");
        for (int i = 0; i < history.size(); i++) {
            Transaction t = history.get(i);
            sb.append(String.format("{\"type\":\"%s\",\"symbol\":\"%s\",\"qty\":%d,\"price\":%.2f}", t.type(), t.symbol(), t.qty(), t.price()));
            if (i < history.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    static void respond(HttpExchange ex, String body, String type) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", type);
        ex.sendResponseHeaders(200, bytes.length);
        OutputStream os = ex.getResponseBody();
        os.write(bytes);
        os.close();
    }
}