package server;

import Model.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dao.DAOFactory;
import service.*;
import util.JsonUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Built-in HTTP Server hosting the REST API and the Web Frontend.
 * Zero external libraries required.
 */
public class HttpServerApp {
    private final int port;
    private HttpServer server;

    private final BookService bookService = new BookService();
    private final UserService userService = new UserService();
    private final TransactionService transactionService = new TransactionService();
    private final ReservationService reservationService = new ReservationService();
    private final StatsService statsService = new StatsService();
    private final LibraryService libraryService = new LibraryService();

    private final String webRoot;

    public HttpServerApp(int port) {
        this.port = port;
        this.webRoot = findWebDirectory();
    }

    private static String findWebDirectory() {
        String[] potentialPaths = {
            "src/web",
            "web",
            "/home/ken/Downloads/Smart-Library-System-main-main/src/web"
        };
        for (String p : potentialPaths) {
            File dir = new File(p);
            if (dir.exists() && dir.isDirectory()) {
                return dir.getAbsolutePath();
            }
        }
        return "src/web";
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));

        // REST API endpoints
        server.createContext("/api/health", this::handleHealth);
        server.createContext("/api/auth/login", this::handleLogin);
        server.createContext("/api/auth/register", this::handleRegister);
        server.createContext("/api/books", this::handleBooks);
        server.createContext("/api/borrow", this::handleBorrow);
        server.createContext("/api/buy", this::handleBuy);
        server.createContext("/api/return", this::handleReturn);
        server.createContext("/api/reserve", this::handleReserve);
        server.createContext("/api/reservations", this::handleReservations);
        server.createContext("/api/reservations/claim", this::handleClaimReservation);
        server.createContext("/api/books/queue", this::handleBookQueue);
        server.createContext("/api/transactions", this::handleTransactions);
        server.createContext("/api/libraries", this::handleLibraries);
        server.createContext("/api/libraries/nearby", this::handleNearbyLibraries);
        server.createContext("/api/admin/create-reader", this::handleAdminCreateReader);
        server.createContext("/api/admin/readers", this::handleAdminReaders);
        server.createContext("/api/admin/readers/delete", this::handleAdminDeleteReader);
        server.createContext("/api/stats", this::handleStats);
        server.createContext("/api/users", this::handleUsers);
        server.createContext("/api/config/db", this::handleDatabaseToggle);

        // Static files (Web Frontend)
        server.createContext("/", this::handleStaticFiles);

        server.start();
        System.out.println("🚀 Smart Library System Server running on http://localhost:" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Server stopped.");
        }
    }

    // --- Handlers ---

    private void handleHealth(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("status", "UP");
        resp.put("service", "Smart Library System API");
        resp.put("databaseMode", DAOFactory.isUseDatabase());
        sendJsonResponse(exchange, 200, resp);
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }
        Map<String, Object> body = readJsonBody(exchange);
        String userId = (String) body.get("userId");
        String password = (String) body.get("password");

        User user = userService.login(userId, password);
        if (user != null) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("user", user.toSafeMap());
            sendJsonResponse(exchange, 200, resp);
        } else {
            sendError(exchange, 401, "Invalid User ID or Password");
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }
        Map<String, Object> body = readJsonBody(exchange);
        String userId = (String) body.get("userId");
        String name = (String) body.get("name");
        String password = (String) body.get("password");
        String role = (String) body.get("role");

        User user = userService.registerUser(userId, name, password, role);
        if (user != null) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("user", user.toSafeMap());
            sendJsonResponse(exchange, 201, resp);
        } else {
            sendError(exchange, 400, "Registration failed: User ID might already exist or role is invalid.");
        }
    }

    private void handleBooks(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        String method = exchange.getRequestMethod().toUpperCase();

        if ("GET".equals(method)) {
            Map<String, String> params = parseQueryParams(exchange);
            String query = params.get("query");
            String type = params.get("type");
            String sort = params.get("sort");

            List<Book> list = bookService.queryBooks(query, type, sort);

            List<Map<String, Object>> result = new ArrayList<>();
            for (Book b : list) {
                result.add(b.toMap());
            }
            sendJsonResponse(exchange, 200, result);
        } else if ("POST".equals(method)) {
            Map<String, Object> body = readJsonBody(exchange);
            String bookId = (String) body.get("bookId");
            String title = (String) body.get("title");
            String author = (String) body.get("author");
            String bookType = (String) body.get("bookType");

            if (bookId == null || title == null || author == null) {
                sendError(exchange, 400, "bookId, title, and author are required.");
                return;
            }

            String genre = (String) body.get("genre");
            if (genre == null || genre.trim().isEmpty()) genre = "General";
            double buyPrice = 499.0;
            Object bpObj = body.get("buyPrice");
            if (bpObj instanceof Number) buyPrice = ((Number) bpObj).doubleValue();
            else if (bpObj instanceof String) {
                try { buyPrice = Double.parseDouble((String) bpObj); } catch (Exception ignored) {}
            }

            Book book;
            if ("AUDIOBOOK".equalsIgnoreCase(bookType)) {
                int duration = 180;
                Object durObj = body.get("audioDurationMinutes");
                if (durObj instanceof Number) duration = ((Number) durObj).intValue();
                String narrator = (String) body.get("narrator");
                String streamUrl = (String) body.get("audioStreamUrl");
                book = new AudioBook(bookId, title, author, genre, duration, narrator != null ? narrator : "Narrator", streamUrl != null ? streamUrl : "", buyPrice, true);
            } else if ("EBOOK".equalsIgnoreCase(bookType)) {
                String link = (String) body.get("downloadLink");
                book = new EBook(bookId, title, author, genre, link != null ? link : "", buyPrice, true);
            } else {
                int shelf = 100;
                Object shelfObj = body.get("shelfNumber");
                if (shelfObj instanceof Number) {
                    shelf = ((Number) shelfObj).intValue();
                } else if (shelfObj instanceof String) {
                    try { shelf = Integer.parseInt((String) shelfObj); } catch (Exception ignored) {}
                }
                book = new PhysicalBook(bookId, title, author, genre, shelf, buyPrice, true);
            }
            bookService.addBook(book);
            sendJsonResponse(exchange, 201, book.toMap());
        } else if ("PUT".equals(method)) {
            Map<String, Object> body = readJsonBody(exchange);
            String bookId = (String) body.get("bookId");
            String title = (String) body.get("title");
            String author = (String) body.get("author");
            String bookType = (String) body.get("bookType");
            String genre = (String) body.get("genre");
            if (genre == null || genre.trim().isEmpty()) genre = "General";
            double buyPrice = 499.0;
            Object bpObj = body.get("buyPrice");
            if (bpObj instanceof Number) buyPrice = ((Number) bpObj).doubleValue();
            else if (bpObj instanceof String) {
                try { buyPrice = Double.parseDouble((String) bpObj); } catch (Exception ignored) {}
            }

            Book book;
            if ("AUDIOBOOK".equalsIgnoreCase(bookType)) {
                int duration = 180;
                Object durObj = body.get("audioDurationMinutes");
                if (durObj instanceof Number) duration = ((Number) durObj).intValue();
                String narrator = (String) body.get("narrator");
                String streamUrl = (String) body.get("audioStreamUrl");
                book = new AudioBook(bookId, title, author, genre, duration, narrator != null ? narrator : "Narrator", streamUrl != null ? streamUrl : "", buyPrice, true);
            } else if ("EBOOK".equalsIgnoreCase(bookType)) {
                String link = (String) body.get("downloadLink");
                book = new EBook(bookId, title, author, genre, link != null ? link : "", buyPrice, true);
            } else {
                int shelf = 100;
                Object shelfObj = body.get("shelfNumber");
                if (shelfObj instanceof Number) {
                    shelf = ((Number) shelfObj).intValue();
                }
                book = new PhysicalBook(bookId, title, author, genre, shelf, buyPrice, true);
            }
            boolean ok = bookService.updateBook(book);
            if (ok) {
                sendJsonResponse(exchange, 200, book.toMap());
            } else {
                sendError(exchange, 404, "Book not found to update.");
            }
        } else if ("DELETE".equals(method)) {
            Map<String, String> params = parseQueryParams(exchange);
            String id = params.get("id");
            if (id == null) {
                sendError(exchange, 400, "Book ID required in query params: ?id=...");
                return;
            }
            boolean ok = bookService.deleteBook(id);
            if (ok) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("message", "Book deleted successfully");
                sendJsonResponse(exchange, 200, resp);
            } else {
                sendError(exchange, 404, "Book not found");
            }
        } else {
            sendError(exchange, 405, "Method not allowed");
        }
    }

    private void handleBorrow(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }
        Map<String, Object> body = readJsonBody(exchange);
        String userId = (String) body.get("userId");
        String bookId = (String) body.get("bookId");
        int days = 7;
        Object daysObj = body.get("days");
        if (daysObj instanceof Number) {
            days = ((Number) daysObj).intValue();
        } else if (daysObj instanceof String) {
            try { days = Integer.parseInt((String) daysObj); } catch (Exception ignored) {}
        }

        Transaction tx = transactionService.borrowBook(userId, bookId, days);
        if (tx != null) {
            sendJsonResponse(exchange, 200, tx.toMap());
        } else {
            sendError(exchange, 400, "Cannot borrow book. It may be currently unavailable or invalid ID.");
        }
    }

    private void handleReturn(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }
        Map<String, Object> body = readJsonBody(exchange);
        String txId = (String) body.get("transactionId");

        Map<String, Object> receipt = transactionService.returnBookWithDetails(txId);
        if (receipt != null) {
            Transaction tx = (Transaction) receipt.get("transaction");
            receipt.put("transaction", tx != null ? tx.toMap() : null);
            sendJsonResponse(exchange, 200, receipt);
        } else {
            sendError(exchange, 404, "Transaction not found: " + txId);
        }
    }

    private void handleReserve(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        String method = exchange.getRequestMethod().toUpperCase();

        if ("POST".equals(method)) {
            Map<String, Object> body = readJsonBody(exchange);
            String userId = (String) body.get("userId");
            String bookId = (String) body.get("bookId");

            Reservation res = reservationService.reserveBook(userId, bookId);
            if (res != null) {
                sendJsonResponse(exchange, 201, res.toMap());
            } else {
                sendError(exchange, 400, "Unable to create reservation.");
            }
        } else if ("DELETE".equals(method)) {
            Map<String, String> params = parseQueryParams(exchange);
            String id = params.get("id");
            if (id == null) {
                sendError(exchange, 400, "Reservation ID required in query params: ?id=...");
                return;
            }
            boolean ok = reservationService.cancelReservation(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", ok);
            sendJsonResponse(exchange, 200, resp);
        } else {
            sendError(exchange, 405, "Method not allowed");
        }
    }

    private void handleClaimReservation(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }
        Map<String, Object> body = readJsonBody(exchange);
        String resId = (String) body.get("reservationId");
        int days = 7;
        Object dObj = body.get("days");
        if (dObj instanceof Number) days = ((Number) dObj).intValue();

        Reservation res = reservationService.getReservation(resId);
        if (res == null) {
            sendError(exchange, 404, "Reservation not found");
            return;
        }

        Transaction tx = transactionService.borrowBook(res.getUserId(), res.getBookId(), days);
        if (tx != null) {
            reservationService.fulfillReservation(resId);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("transaction", tx.toMap());
            resp.put("message", "Reservation claimed! Book issued to you.");
            sendJsonResponse(exchange, 200, resp);
        } else {
            sendError(exchange, 400, "Unable to claim reservation. Book may still be on loan.");
        }
    }

    private void handleReservations(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        Map<String, String> params = parseQueryParams(exchange);
        String userId = params.get("userId");

        List<Reservation> list;
        if (userId != null && !userId.isEmpty()) {
            list = reservationService.getUserReservations(userId);
        } else {
            list = reservationService.getAllReservations();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Reservation r : list) {
            result.add(r.toMap());
        }
        sendJsonResponse(exchange, 200, result);
    }

    private void handleTransactions(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        Map<String, String> params = parseQueryParams(exchange);
        String userId = params.get("userId");

        List<Transaction> list;
        if (userId != null && !userId.isEmpty()) {
            list = transactionService.getUserTransactions(userId);
        } else {
            list = transactionService.getAllTransactions();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction t : list) {
            result.add(t.toMap());
        }
        sendJsonResponse(exchange, 200, result);
    }

    private void handleStats(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        Map<String, Object> stats = statsService.getSystemStats();
        sendJsonResponse(exchange, 200, stats);
    }

    private void handleUsers(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        List<User> list = userService.getAllUsers();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : list) {
            result.add(u.toSafeMap());
        }
        sendJsonResponse(exchange, 200, result);
    }

    private void handleBuy(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }
        Map<String, Object> body = readJsonBody(exchange);
        String userId = (String) body.get("userId");
        String bookId = (String) body.get("bookId");

        Transaction tx = transactionService.buyBook(userId, bookId);
        if (tx != null) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("transaction", tx.toMap());
            resp.put("message", "Book purchased successfully! It has been added to your permanent collection.");
            sendJsonResponse(exchange, 200, resp);
        } else {
            sendError(exchange, 400, "Purchase failed. Book might be out of stock or invalid IDs provided.");
        }
    }

    private void handleBookQueue(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        Map<String, String> params = parseQueryParams(exchange);
        String bookId = params.get("bookId");
        if (bookId == null || bookId.isEmpty()) {
            sendError(exchange, 400, "bookId query parameter is required");
            return;
        }
        List<Reservation> queue = reservationService.getBookQueue(bookId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Reservation r : queue) {
            result.add(r.toMap());
        }
        sendJsonResponse(exchange, 200, result);
    }

    private void handleLibraries(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        List<Library> list = libraryService.getAllLibraries();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Library l : list) {
            result.add(l.toMap());
        }
        sendJsonResponse(exchange, 200, result);
    }

    private void handleNearbyLibraries(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        Map<String, String> params = parseQueryParams(exchange);
        String query = params.get("query");
        List<Library> list = libraryService.searchNearbyLibraries(query);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Library l : list) {
            result.add(l.toMap());
        }
        sendJsonResponse(exchange, 200, result);
    }

    private void handleAdminCreateReader(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }
        Map<String, Object> body = readJsonBody(exchange);
        String customId = (String) body.get("userId");
        String name = (String) body.get("name");
        String email = (String) body.get("email");
        String password = (String) body.get("password");

        try {
            User reader = userService.adminCreateReader(customId, name, email, password);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("reader", reader.toMap());
            resp.put("message", "Reader account created successfully!");
            sendJsonResponse(exchange, 201, resp);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Server error creating reader: " + e.getMessage());
        }
    }

    private void handleAdminReaders(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        List<User> list = userService.getAllReaders();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : list) {
            result.add(u.toMap());
        }
        sendJsonResponse(exchange, 200, result);
    }

    private void handleAdminDeleteReader(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        Map<String, String> params = parseQueryParams(exchange);
        String userId = params.get("userId");
        if (userId == null || userId.isEmpty()) {
            Map<String, Object> body = readJsonBody(exchange);
            userId = (String) body.get("userId");
        }
        if (userId == null || userId.isEmpty()) {
            sendError(exchange, 400, "userId is required to delete reader.");
            return;
        }

        boolean ok = userService.deleteReader(userId);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", ok);
        resp.put("message", ok ? "Reader deleted successfully." : "Reader not found or cannot delete root admin.");
        sendJsonResponse(exchange, ok ? 200 : 404, resp);
    }

    private void handleDatabaseToggle(HttpExchange exchange) throws IOException {
        if (handleCors(exchange)) return;
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }
        Map<String, Object> body = readJsonBody(exchange);
        boolean enable = Boolean.TRUE.equals(body.get("enable"));
        boolean success = DAOFactory.setUseDatabase(enable);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", success);
        resp.put("databaseMode", DAOFactory.isUseDatabase());
        resp.put("message", DAOFactory.isUseDatabase() ? "Switched to MySQL Database storage" : "Switched to In-Memory storage");
        sendJsonResponse(exchange, 200, resp);
    }

    // --- Static Web Files Handler ---

    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        String uriPath = exchange.getRequestURI().getPath();
        if (uriPath.equals("/") || uriPath.isEmpty()) {
            uriPath = "/index.html";
        }

        File file = new File(webRoot, uriPath);
        if (!file.exists() || file.isDirectory()) {
            file = new File(webRoot, "index.html");
        }

        if (!file.exists()) {
            String notFound = "<html><body><h1>404 Not Found</h1><p>Web UI files not found in " + webRoot + "</p></body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] b = notFound.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, b.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(b);
            }
            return;
        }

        String mime = getMimeType(file.getName());
        exchange.getResponseHeaders().set("Content-Type", mime);
        byte[] bytes = Files.readAllBytes(file.toPath());
        boolean isHead = "HEAD".equalsIgnoreCase(exchange.getRequestMethod());
        exchange.sendResponseHeaders(200, isHead ? -1 : bytes.length);
        if (!isHead) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.getResponseBody().close();
        }
    }

    private String getMimeType(String filename) {
        if (filename.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filename.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filename.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filename.endsWith(".json")) return "application/json; charset=UTF-8";
        if (filename.endsWith(".svg")) return "image/svg+xml";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        return "text/plain; charset=UTF-8";
    }

    // --- Helpers ---

    private boolean handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private Map<String, Object> readJsonBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            String str = baos.toString(StandardCharsets.UTF_8);
            return JsonUtil.parseObject(str);
        }
    }

    private Map<String, String> parseQueryParams(HttpExchange exchange) {
        Map<String, String> map = new HashMap<>();
        String rawQuery = exchange.getRequestURI().getRawQuery();
        if (rawQuery == null || rawQuery.isEmpty()) return map;

        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                if (idx > 0) {
                    String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    map.put(k, v);
                } else if (!pair.isEmpty()) {
                    map.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
                }
            } catch (Exception ignored) {}
        }
        return map;
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = JsonUtil.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("error", message);
        sendJsonResponse(exchange, statusCode, err);
    }
}
