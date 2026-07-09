public class PageHtml {
    public static final String PAGE = """
        <!DOCTYPE html>
        <html>
        <head>
        <title>HorizonTechX Stock Trading</title>
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <style>
            :root {
                --bg: #0f1115; --card: #171a21; --text: #e8eaed; --sub: #9aa0a6;
                --accent: #4f8cff; --green: #26a65b; --red: #e5484d; --border: #2a2e37;
            }
            body.light {
                --bg: #f4f6f8; --card: #ffffff; --text: #1a1a1a; --sub: #5f6368;
                --accent: #1a56db; --green: #1e8e3e; --red: #d93025; --border: #e0e3e7;
            }
            * { box-sizing: border-box; }
            body {
                font-family: 'Segoe UI', Arial, sans-serif; background: var(--bg); color: var(--text);
                margin: 0; padding: 24px; transition: background 0.3s, color 0.3s;
            }
            .top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
            h1 { margin: 0; font-size: 22px; }
            h2 { margin: 0 0 14px 0; font-size: 16px; color: var(--sub); text-transform: uppercase; letter-spacing: 1px; }
            .toggle-btn {
                background: var(--card); border: 1px solid var(--border); color: var(--text);
                padding: 8px 16px; border-radius: 20px; cursor: pointer; font-size: 13px;
            }
            .stats { display: flex; gap: 16px; margin-bottom: 20px; flex-wrap: wrap; }
            .stat-card {
                background: var(--card); border: 1px solid var(--border); border-radius: 10px;
                padding: 16px 20px; flex: 1; min-width: 160px;
            }
            .stat-label { color: var(--sub); font-size: 12px; text-transform: uppercase; }
            .stat-value { font-size: 22px; font-weight: 600; margin-top: 4px; }
            .grid { display: grid; grid-template-columns: 1.3fr 1fr; gap: 20px; margin-bottom: 20px; }
            .card {
                background: var(--card); border: 1px solid var(--border); border-radius: 10px; padding: 20px;
            }
            table { width: 100%; border-collapse: collapse; }
            th, td { padding: 10px 8px; text-align: left; border-bottom: 1px solid var(--border); font-size: 14px; }
            th { color: var(--sub); font-weight: 500; font-size: 12px; text-transform: uppercase; }
            .price-up { color: var(--green); }
            .price-down { color: var(--red); }
            button.buy, button.sell {
                border: none; padding: 6px 14px; border-radius: 6px; cursor: pointer; color: white; font-size: 13px;
            }
            button.buy { background: var(--green); }
            button.sell { background: var(--red); }
            input[type=number] {
                width: 55px; padding: 5px; border-radius: 5px; border: 1px solid var(--border);
                background: var(--bg); color: var(--text);
            }
            #message { min-height: 20px; color: var(--accent); font-weight: 500; margin-bottom: 10px; }
            canvas { max-height: 220px; }
        </style>
        </head>
        <body>
            <div class="top">
                <h1>\uD83D\uDCC8 HorizonTechX Trading</h1>
                <button class="toggle-btn" onclick="toggleTheme()">\uD83C\uDF13 Toggle Theme</button>
            </div>

            <div id="message"></div>

            <div class="stats">
                <div class="stat-card"><div class="stat-label">Cash Balance</div><div class="stat-value" id="cash">-</div></div>
                <div class="stat-card"><div class="stat-label">Invested</div><div class="stat-value" id="invested">-</div></div>
                <div class="stat-card"><div class="stat-label">Current Value</div><div class="stat-value" id="curval">-</div></div>
                <div class="stat-card"><div class="stat-label">Profit / Loss</div><div class="stat-value" id="pl">-</div></div>
            </div>

            <div class="grid">
                <div class="card">
                    <h2>Market</h2>
                    <table id="marketTable"><thead><tr><th>Symbol</th><th>Company</th><th>Price</th><th>Qty</th><th></th></tr></thead><tbody></tbody></table>
                </div>
                <div class="card">
                    <h2>Portfolio Distribution</h2>
                    <canvas id="chart"></canvas>
                </div>
            </div>

            <div class="grid">
                <div class="card">
                    <h2>Your Portfolio</h2>
                    <table id="portfolioTable"><thead><tr><th>Symbol</th><th>Qty</th><th>Value</th><th>P/L</th><th></th></tr></thead><tbody></tbody></table>
                </div>
                <div class="card">
                    <h2>Transaction History</h2>
                    <table id="historyTable"><thead><tr><th>Type</th><th>Symbol</th><th>Qty</th><th>Price</th></tr></thead><tbody></tbody></table>
                </div>
            </div>

        <script>
            let chart;

            function toggleTheme() {
                document.body.classList.toggle('light');
                localStorage.setItem('theme', document.body.classList.contains('light') ? 'light' : 'dark');
            }
            if (localStorage.getItem('theme') === 'light') document.body.classList.add('light');

            function fmt(n) { return 'Rs.' + n.toLocaleString('en-IN', {minimumFractionDigits: 2, maximumFractionDigits: 2}); }

            function load() {
                fetch('/api/data').then(r => r.json()).then(data => {
                    document.getElementById('cash').innerText = fmt(data.cash);

                    let invested = data.portfolio.reduce((a, p) => a + (p.value - p.pl), 0);
                    let curval = data.portfolio.reduce((a, p) => a + p.value, 0);
                    let pl = curval - invested;

                    document.getElementById('invested').innerText = fmt(invested);
                    document.getElementById('curval').innerText = fmt(curval);
                    let plEl = document.getElementById('pl');
                    plEl.innerText = fmt(pl);
                    plEl.className = 'stat-value ' + (pl >= 0 ? 'price-up' : 'price-down');

                    let mBody = document.querySelector('#marketTable tbody');
                    mBody.innerHTML = '';
                    data.market.forEach(s => {
                        mBody.innerHTML += `<tr><td><b>${s.symbol}</b></td><td>${s.name}</td><td>${fmt(s.price)}</td>
                        <td><input type="number" id="qty_${s.symbol}" value="1" min="1"></td>
                        <td><button class="buy" onclick="trade('${s.symbol}', true)">Buy</button></td></tr>`;
                    });

                    let pBody = document.querySelector('#portfolioTable tbody');
                    pBody.innerHTML = '';
                    if (data.portfolio.length === 0) pBody.innerHTML = '<tr><td colspan="5" style="color:var(--sub)">No holdings yet</td></tr>';
                    data.portfolio.forEach(p => {
                        let plClass = p.pl >= 0 ? 'price-up' : 'price-down';
                        pBody.innerHTML += `<tr><td><b>${p.symbol}</b></td><td>${p.qty}</td><td>${fmt(p.value)}</td><td class="${plClass}">${fmt(p.pl)}</td>
                        <td><button class="sell" onclick="sellPrompt('${p.symbol}', ${p.qty})">Sell</button></td></tr>`;
                    });

                    let hBody = document.querySelector('#historyTable tbody');
                    hBody.innerHTML = '';
                    if (data.history.length === 0) hBody.innerHTML = '<tr><td colspan="4" style="color:var(--sub)">No transactions yet</td></tr>';
                    data.history.slice().reverse().forEach(t => {
                        let cls = t.type === 'BUY' ? 'price-up' : 'price-down';
                        hBody.innerHTML += `<tr><td class="${cls}">${t.type}</td><td>${t.symbol}</td><td>${t.qty}</td><td>${fmt(t.price)}</td></tr>`;
                    });

                    updateChart(data.portfolio);
                });
            }

            function updateChart(portfolio) {
                let labels = portfolio.map(p => p.symbol);
                let values = portfolio.map(p => p.value);
                if (labels.length === 0) { labels = ['No holdings']; values = [1]; }

                if (chart) chart.destroy();
                let ctx = document.getElementById('chart').getContext('2d');
                chart = new Chart(ctx, {
                    type: 'doughnut',
                    data: {
                        labels: labels,
                        datasets: [{
                            data: values,
                            backgroundColor: ['#4f8cff', '#26a65b', '#e5484d', '#f5a623', '#9b59b6', '#1abc9c']
                        }]
                    },
                    options: {
                        plugins: { legend: { labels: { color: getComputedStyle(document.body).getPropertyValue('--text') } } }
                    }
                });
            }

            function trade(symbol, isBuy) {
                let qty = document.getElementById('qty_' + symbol).value;
                fetch(`/api/${isBuy ? 'buy' : 'sell'}?symbol=${symbol}&qty=${qty}`)
                    .then(r => r.json()).then(res => {
                        document.getElementById('message').innerText = res.message;
                        load();
                    });
            }

            function sellPrompt(symbol, maxQty) {
                let qty = prompt(`Sell how many shares of ${symbol}? (You own ${maxQty})`, 1);
                if (qty) {
                    fetch(`/api/sell?symbol=${symbol}&qty=${qty}`)
                        .then(r => r.json()).then(res => {
                            document.getElementById('message').innerText = res.message;
                            load();
                        });
                }
            }

            load();
        </script>
        </body>
        </html>
        """;
}