public record Transaction(String type, String symbol, int qty, double price) {
    public String toString() {
        return String.format("%-4s %-6s Qty:%-4d Price:Rs.%.2f Total:Rs.%.2f", type, symbol, qty, price, qty * price);
    }
}