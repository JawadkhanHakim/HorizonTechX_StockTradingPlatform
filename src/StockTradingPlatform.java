import java.util.*;
import java.io.FileWriter;
import java.io.IOException;

public class StockTradingPlatform {

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

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("===== HorizonTechX - Stock Trading Platform =====");
        System.out.print("Enter your name: ");
        String name = sc.nextLine().trim();
        System.out.printf("Welcome %s! Starting balance: Rs.%.2f%n", name, cash);

        while (true) {
            printMenu();
            switch (sc.nextLine().trim()) {
                case "1" -> showMarket();
                case "2" -> buy(sc);
                case "3" -> sell(sc);
                case "4" -> showPortfolio();
                case "5" -> printHistory();
                case "6" -> { saveHistory(name); return; }
                default -> System.out.println("Invalid option, try again.");
            }
        }
    }

    static void printMenu() {
        System.out.println("\n---------------- MENU ----------------");
        System.out.println("1. View Market");
        System.out.println("2. Buy Stock");
        System.out.println("3. Sell Stock");
        System.out.println("4. View Portfolio");
        System.out.println("5. View Transaction History");
        System.out.println("6. Exit & Save");
        System.out.println("---------------------------------------");
        System.out.print("Choose an option: ");
    }

    static void showMarket() {
        System.out.println("\n--- Current Market ---");
        System.out.printf("%-6s %-25s %-10s%n", "SYMBOL", "COMPANY", "PRICE");
        market.values().forEach(s -> System.out.printf("%-6s %-25s Rs.%-10.2f%n", s.symbol(), s.name(), s.price()));
    }

    static void buy(Scanner sc) {
        showMarket();
        System.out.print("\nEnter symbol to buy: ");
        Stock s = market.get(sc.nextLine().trim().toUpperCase());
        if (s == null) { System.out.println("Invalid symbol."); return; }
        System.out.print("Enter quantity: ");
        int qty = Integer.parseInt(sc.nextLine().trim());
        double cost = qty * s.price();
        if (cost > cash) { System.out.println("Insufficient balance."); return; }
        cash -= cost;
        portfolio.merge(s.symbol(), qty, Integer::sum);
        invested.merge(s.symbol(), cost, Double::sum);
        history.add(new Transaction("BUY", s.symbol(), qty, s.price()));
        System.out.printf("%nBought %d %s.%nNew balance: Rs.%.2f%n", qty, s.symbol(), cash);
    }

    static void sell(Scanner sc) {
        if (portfolio.isEmpty()) { System.out.println("\nYou don't own any stocks yet."); return; }
        showPortfolio();
        System.out.print("\nEnter symbol to sell: ");
        String sym = sc.nextLine().trim().toUpperCase();
        int owned = portfolio.getOrDefault(sym, 0);
        if (owned == 0) { System.out.println("You don't own " + sym); return; }
        System.out.print("Enter quantity (you own " + owned + "): ");
        int qty = Integer.parseInt(sc.nextLine().trim());
        if (qty > owned) { System.out.println("Can't sell more than you own."); return; }
        double price = market.get(sym).price();
        cash += qty * price;
        double avgCost = invested.get(sym) / owned;
        invested.merge(sym, -avgCost * qty, Double::sum);
        if (qty == owned) { portfolio.remove(sym); invested.remove(sym); }
        else portfolio.put(sym, owned - qty);
        history.add(new Transaction("SELL", sym, qty, price));
        System.out.printf("%nSold %d %s.%nNew balance: Rs.%.2f%n", qty, sym, cash);
    }

    static void showPortfolio() {
        System.out.println("\n--- Your Portfolio ---");
        System.out.printf("Cash Balance: Rs.%.2f%n", cash);
        if (portfolio.isEmpty()) { System.out.println("No stocks owned yet."); return; }
        System.out.printf("%n%-6s %-6s %-12s %-12s%n", "SYMBOL", "QTY", "VALUE", "P/L");
        double totalVal = 0, totalInv = 0;
        for (var e : portfolio.entrySet()) {
            double price = market.get(e.getKey()).price();
            double value = e.getValue() * price;
            double inv = invested.get(e.getKey());
            totalVal += value; totalInv += inv;
            System.out.printf("%-6s %-6d Rs.%-9.2f Rs.%-9.2f%n", e.getKey(), e.getValue(), value, value - inv);
        }
        System.out.printf("%nTotal Invested: Rs.%.2f%nCurrent Value: Rs.%.2f%nOverall P/L: Rs.%.2f%n", totalInv, totalVal, totalVal - totalInv);
    }

    static void printHistory() {
        System.out.println("\n--- Transaction History ---");
        if (history.isEmpty()) { System.out.println("No transactions yet."); return; }
        history.forEach(System.out::println);
    }

    static void saveHistory(String name) throws IOException {
        try (FileWriter fw = new FileWriter("TransactionHistory.txt")) {
            fw.write("Transaction History for " + name + "\n\n");
            for (Transaction t : history) fw.write(t + "\n");
            fw.write("\nFinal Balance: Rs." + String.format("%.2f", cash));
        }
        System.out.println("\nSaved to TransactionHistory.txt. Goodbye!");
    }
}