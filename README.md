# Stock Trading Platform

A Java-based Stock Trading Platform with a **live web dashboard**, built as part of the **HorizonTechX Java Programming Internship**.

## What it does

- Runs a lightweight web server directly from Java (no external frameworks)
- Opens a live dashboard in the browser — no console typing required
- Simulates a stock market with 5 companies (TCS, INFY, RELI, HDFC, WIPRO)
- Lets the user **buy and sell** stocks using virtual cash (starts with Rs. 1,00,000)
- Tracks a live **portfolio** with quantity owned, current value, and profit/loss per stock
- Visualizes portfolio distribution with a **doughnut chart**
- Includes a **dark/light theme toggle**
- Keeps a full **transaction history** of every buy/sell action

## Concepts used

- Object-Oriented Programming (`Stock`, `Transaction` as Java records)
- Collections — `HashMap` for market/portfolio data, `ArrayList` for transaction history
- Core Java's built-in `HttpServer` to serve a web dashboard (no Spring Boot needed)
- HTML, CSS, and JavaScript (with Chart.js) for the frontend dashboard
- JSON-based communication between the Java backend and the browser frontend

## How to run

```bash
javac *.java
java TradingServer
```

Then open `http://localhost:8080` in your browser (opens automatically on most systems).

## Project Structure

- `Stock.java` — represents a single stock (symbol, company name, price)
- `Transaction.java` — represents a single buy/sell transaction
- `TradingServer.java` — main server logic (market, portfolio, buy/sell handling)
- `PageHtml.java` — the dashboard's HTML/CSS/JavaScript

## Sample Output

Live dashboard showing cash balance, invested amount, current value, profit/loss, market table, portfolio table, transaction history, and a portfolio distribution chart.

## Author

Mohammad Jawadkhan Hakim — [LinkedIn](https://www.linkedin.com/in/jawad-khan-hakim/)

---
*Built as part of the HorizonTechX Java Programming Internship.*
