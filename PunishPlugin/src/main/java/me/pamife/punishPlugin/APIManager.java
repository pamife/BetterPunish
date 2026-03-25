package me.pamife.punishPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class APIManager {

    private HttpServer server;
    private final PunishPlugin plugin;
    private final Gson gson;
    private String publicIP = "127.0.0.1";

    public APIManager(PunishPlugin plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("api.enabled", false)) return;

        fetchPublicIP();

        int port = plugin.getConfig().getInt("api.port", 8080);
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/panel", this::handlePanel);
            server.createContext("/api/reports", this::handleReports);
            server.createContext("/api/mutes", this::handleMutes);
            server.createContext("/api/history", this::handleHistory);

            server.setExecutor(null);
            server.start();
            plugin.getLogger().info("✅ Web-Panel Pro gestartet auf Port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    public String getPanelURL() {
        int port = plugin.getConfig().getInt("api.port", 8080);
        String key = plugin.getConfig().getString("api.key", "geheim");
        return "http://" + publicIP + ":" + port + "/panel?key=" + key;
    }

    private void fetchPublicIP() {
        try {
            URL url = new URL("https://api.ipify.org");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            this.publicIP = br.readLine();
        } catch (Exception e) {
            this.publicIP = "DEINE_SERVER_IP";
        }
    }

    private void handlePanel(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String key = plugin.getConfig().getString("api.key", "geheim");

        if (query == null || !query.contains("key=" + key)) {
            sendResponse(exchange, 403, "text/html", "<h1>403 Forbidden</h1>");
            return;
        }

        int port = plugin.getConfig().getInt("api.port", 8080);
        String html = getHtmlTemplate()
                .replace("%%BASE_URL%%", "http://" + publicIP + ":" + port)
                .replace("%%API_KEY%%", "?key=" + key);

        sendResponse(exchange, 200, "text/html", html);
    }

    private void handleReports(HttpExchange exchange) throws IOException {
        if (!checkAuth(exchange)) return;
        sendResponse(exchange, 200, "application/json", gson.toJson(plugin.getDataManager().getReports()));
    }

    private void handleMutes(HttpExchange exchange) throws IOException {
        if (!checkAuth(exchange)) return;
        List<UUID> mutes = plugin.getDataManager().getActiveMutes();
        JsonArray arr = new JsonArray();
        for (UUID u : mutes) {
            JsonObject o = new JsonObject();
            o.addProperty("uuid", u.toString());
            o.addProperty("expiry", plugin.getDataManager().getMuteExpiry(u));
            arr.add(o);
        }
        sendResponse(exchange, 200, "application/json", gson.toJson(arr));
    }

    private void handleHistory(HttpExchange exchange) throws IOException {
        if (!checkAuth(exchange)) return;
        // Diese Route benötigt zusätzlich &uuid=... im Link
        String query = exchange.getRequestURI().getQuery();
        if (query.contains("uuid=")) {
            String uuidStr = query.split("uuid=")[1].split("&")[0];
            List<String> history = plugin.getDataManager().getHistory(UUID.fromString(uuidStr));
            sendResponse(exchange, 200, "application/json", gson.toJson(history));
        } else {
            sendResponse(exchange, 400, "application/json", "{\"error\": \"UUID missing\"}");
        }
    }

    private boolean checkAuth(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String apiKey = plugin.getConfig().getString("api.key", "geheim");
        return query != null && query.contains("key=" + apiKey);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String contentType, String response) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", contentType + "; charset=UTF-8");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getHtmlTemplate() {
        return "<!DOCTYPE html>\n" +
                "    <html lang=\"en\">\n" +
                "    <head>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "        <title>BetterPunish Pro | Dashboard</title>\n" +
                "        <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css\">\n" +
                "        <style>\n" +
                "            :root {\n" +
                "                --bg: #0f0f17;\n" +
                "                --card: #1a1b26;\n" +
                "                --accent: #7aa2f7;\n" +
                "                --danger: #f7768e;\n" +
                "                --success: #9ece6a;\n" +
                "                --text: #c0caf5;\n" +
                "                --subtext: #9aa5ce;\n" +
                "            }\n" +
                "\n" +
                "            body {\n" +
                "                font-family: 'Inter', system-ui, -apple-system, sans-serif;\n" +
                "                background-color: var(--bg);\n" +
                "                color: var(--text);\n" +
                "                margin: 0;\n" +
                "                display: flex;\n" +
                "                flex-direction: column;\n" +
                "                align-items: center;\n" +
                "                min-height: 100vh;\n" +
                "                padding: 40px 20px;\n" +
                "            }\n" +
                "\n" +
                "            .header { text-align: center; margin-bottom: 40px; }\n" +
                "            .header h1 {\n" +
                "                font-size: 2.5rem;\n" +
                "                margin: 0;\n" +
                "                background: linear-gradient(90deg, #7aa2f7, #bb9af7);\n" +
                "                -webkit-background-clip: text;\n" +
                "                -webkit-text-fill-color: transparent;\n" +
                "            }\n" +
                "\n" +
                "            .container {\n" +
                "                width: 100%;\n" +
                "                max-width: 1100px;\n" +
                "                background: var(--card);\n" +
                "                border-radius: 20px;\n" +
                "                border: 1px solid rgba(255,255,255,0.05);\n" +
                "                box-shadow: 0 20px 50px rgba(0,0,0,0.5);\n" +
                "                padding: 30px;\n" +
                "            }\n" +
                "\n" +
                "            .controls {\n" +
                "                display: flex;\n" +
                "                justify-content: space-between;\n" +
                "                align-items: center;\n" +
                "                margin-bottom: 30px;\n" +
                "                flex-wrap: wrap;\n" +
                "                gap: 20px;\n" +
                "            }\n" +
                "\n" +
                "            .tabs {\n" +
                "                display: flex;\n" +
                "                gap: 10px;\n" +
                "                background: rgba(0,0,0,0.2);\n" +
                "                padding: 6px;\n" +
                "                border-radius: 12px;\n" +
                "            }\n" +
                "\n" +
                "            .tab-btn {\n" +
                "                background: transparent;\n" +
                "                border: none;\n" +
                "                color: var(--subtext);\n" +
                "                padding: 10px 20px;\n" +
                "                border-radius: 8px;\n" +
                "                cursor: pointer;\n" +
                "                font-weight: 600;\n" +
                "                transition: 0.3s;\n" +
                "                display: flex;\n" +
                "                align-items: center;\n" +
                "                gap: 8px;\n" +
                "            }\n" +
                "\n" +
                "            .tab-btn.active {\n" +
                "                background: var(--accent);\n" +
                "                color: white;\n" +
                "            }\n" +
                "\n" +
                "            .search-box {\n" +
                "                position: relative;\n" +
                "            }\n" +
                "\n" +
                "            .search-box input {\n" +
                "                background: rgba(0,0,0,0.2);\n" +
                "                border: 1px solid rgba(255,255,255,0.1);\n" +
                "                padding: 10px 15px 10px 40px;\n" +
                "                border-radius: 10px;\n" +
                "                color: white;\n" +
                "                width: 250px;\n" +
                "            }\n" +
                "\n" +
                "            .search-box i {\n" +
                "                position: absolute;\n" +
                "                left: 15px;\n" +
                "                top: 12px;\n" +
                "                color: var(--subtext);\n" +
                "            }\n" +
                "\n" +
                "            table { width: 100%; border-collapse: separate; border-spacing: 0 8px; }\n" +
                "            th { text-align: left; padding: 15px; color: var(--subtext); font-size: 0.8rem; text-transform: uppercase; }\n" +
                "            tbody tr { background: rgba(255,255,255,0.02); transition: 0.2s; }\n" +
                "            tbody tr:hover { background: rgba(255,255,255,0.05); }\n" +
                "            td { padding: 15px; border-top: 1px solid rgba(255,255,255,0.05); border-bottom: 1px solid rgba(255,255,255,0.05); }\n" +
                "            td:first-child { border-radius: 10px 0 0 10px; }\n" +
                "            td:last-child { border-radius: 0 10px 10px 0; }\n" +
                "\n" +
                "            .player-info { display: flex; align-items: center; gap: 12px; }\n" +
                "            .player-head { width: 32px; height: 32px; border-radius: 4px; }\n" +
                "            \n" +
                "            .badge { padding: 4px 10px; border-radius: 6px; font-size: 0.7rem; font-weight: bold; }\n" +
                "            .badge-danger { background: rgba(247, 118, 142, 0.1); color: var(--danger); }\n" +
                "            .badge-info { background: rgba(122, 162, 247, 0.1); color: var(--accent); cursor: pointer; }\n" +
                "\n" +
                "            /* Modal */\n" +
                "            .modal { display: none; position: fixed; z-index: 100; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.7); backdrop-filter: blur(4px); }\n" +
                "            .modal-content { background: var(--card); margin: 10% auto; padding: 30px; border-radius: 15px; width: 50%; border: 1px solid var(--accent); }\n" +
                "            .history-line { padding: 10px; border-bottom: 1px solid rgba(255,255,255,0.05); font-size: 0.9rem; }\n" +
                "        </style>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "\n" +
                "        <div class=\"header\">\n" +
                "            <h1>⚖ BetterPunish Dashboard</h1>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"container\">\n" +
                "            <div class=\"controls\">\n" +
                "                <div class=\"tabs\">\n" +
                "                    <button class=\"tab-btn active\" onclick=\"switchTab('reports')\" id=\"btn-reports\"><i class=\"fas fa-bullhorn\"></i> Reports</button>\n" +
                "                    <button class=\"tab-btn\" onclick=\"switchTab('mutes')\" id=\"btn-mutes\"><i class=\"fas fa-microphone-slash\"></i> Mutes</button>\n" +
                "                </div>\n" +
                "                <div class=\"search-box\">\n" +
                "                    <i class=\"fas fa-search\"></i>\n" +
                "                    <input type=\"text\" id=\"searchInput\" placeholder=\"Search UUID for history...\" onchange=\"searchHistory()\">\n" +
                "                </div>\n" +
                "            </div>\n" +
                "\n" +
                "            <div id=\"content\"></div>\n" +
                "        </div>\n" +
                "\n" +
                "        <div id=\"hModal\" class=\"modal\">\n" +
                "            <div class=\"modal-content\">\n" +
                "                <h3 id=\"hTitle\">Player History</h3>\n" +
                "                <div id=\"hBody\"></div>\n" +
                "                <button onclick=\"document.getElementById('hModal').style.display='none'\" class=\"tab-btn active\" style=\"margin-top:20px\">Close</button>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "\n" +
                "        <script>\n" +
                "            const BASE_URL = \"%%BASE_URL%%\";\n" +
                "            const API_KEY = \"%%API_KEY%%\";\n" +
                "            let currentTab = 'reports';\n" +
                "\n" +
                "            async function getPlayerName(uuid) {\n" +
                "                try {\n" +
                "                    const res = await fetch(`https://api.crafthead.net/profile/${uuid}`);\n" +
                "                    const data = await res.json();\n" +
                "                    return data.name || uuid;\n" +
                "                } catch { return uuid; }\n" +
                "            }\n" +
                "\n" +
                "            async function showHistory(uuid) {\n" +
                "                const modal = document.getElementById('hModal');\n" +
                "                const body = document.getElementById('hBody');\n" +
                "                modal.style.display = 'block';\n" +
                "                body.innerHTML = 'Loading...';\n" +
                "                try {\n" +
                "                    const res = await fetch(`${BASE_URL}/api/history${API_KEY}&uuid=${uuid}`);\n" +
                "                    const data = await res.json();\n" +
                "                    body.innerHTML = data.length ? data.map(l => `<div class=\"history-line\">${l}</div>`).join('') : 'No entries found.';\n" +
                "                } catch { body.innerHTML = 'Error loading history.'; }\n" +
                "            }\n" +
                "\n" +
                "            function searchHistory() {\n" +
                "                const val = document.getElementById('searchInput').value;\n" +
                "                if(val.length > 30) showHistory(val);\n" +
                "            }\n" +
                "\n" +
                "            async function switchTab(tab) {\n" +
                "                currentTab = tab;\n" +
                "                document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));\n" +
                "                document.getElementById('btn-' + tab).classList.add('active');\n" +
                "                render();\n" +
                "            }\n" +
                "\n" +
                "            async function render() {\n" +
                "                const div = document.getElementById('content');\n" +
                "                try {\n" +
                "                    const res = await fetch(`${BASE_URL}/api/${currentTab}${API_KEY}`);\n" +
                "                    const data = await res.json();\n" +
                "                    if(!data.length) { div.innerHTML = \"<p style='text-align:center'>No data available.</p>\"; return; }\n" +
                "\n" +
                "                    let html = `<table><thead><tr><th>Player</th><th>${currentTab === 'reports' ? 'Reason' : 'Expiry'}</th><th>Action</th></tr></thead><tbody>`;\n" +
                "                    for(const i of data) {\n" +
                "                        const uuid = i.target || i.uuid;\n" +
                "                        const name = await getPlayerName(uuid);\n" +
                "                        html += `<tr>\n" +
                "                            <td><div class=\"player-info\"><img class=\"player-head\" src=\"https://api.crafthead.net/avatar/${uuid}\"><b>${name}</b></div></td>\n" +
                "                            <td class=\"${currentTab === 'reports' ? 'highlight' : ''}\">${i.reason || new Date(i.expiry).toLocaleString()}</td>\n" +
                "                            <td><span class=\"badge badge-info\" onclick=\"showHistory('${uuid}')\"><i class=\"fas fa-eye\"></i> History</span></td>\n" +
                "                        </tr>`;\n" +
                "                    }\n" +
                "                    div.innerHTML = html + \"</tbody></table>\";\n" +
                "                } catch { div.innerHTML = \"Connection error.\"; }\n" +
                "            }\n" +
                "\n" +
                "            render();\n" +
                "            setInterval(render, 30000);\n" +
                "        </script>\n" +
                "    </body>\n" +
                "    </html>\";";
    }
}