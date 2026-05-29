package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.dto.CartAddDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.service.AddressService;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.CartService;
import com.example.bookstore.service.OrderService;
import com.example.bookstore.util.AuthContext;
import com.example.bookstore.util.JwtUtils;
import com.example.bookstore.vo.BookVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private static final String MODEL = "MiniMax-M2.7";
    private static final int MAX_ITERATIONS = 5;

    @Value("${minimax.api.key}")
    private String apiKey;

    @Value("${minimax.api.url}")
    private String apiUrl;

    private final BookService bookService;
    private final BookMapper bookMapper;
    private final CartService cartService;
    private final OrderService orderService;
    private final AddressService addressService;
    private final JwtUtils jwtUtils;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            trySetAuth(authHeader);
            ChatResult result = processChat(request);
            Map<String, Object> res = new HashMap<>();
            res.put("reply", result.reply);
            if (!result.books.isEmpty()) {
                res.put("books", result.books);
            }
            return Result.success(res);
        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("reply", "抱歉，处理您的请求时出现了问题，请稍后再试。");
            return Result.success(res);
        } finally {
            AuthContext.remove();
        }
    }

    @PostMapping("/chat/stream")
    public SseEmitter chatStream(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        httpResponse.setContentType("text/event-stream");
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setHeader("Cache-Control", "no-cache");
        httpResponse.setHeader("Connection", "keep-alive");
        httpResponse.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(0L);

        new Thread(() -> {
            try {
                trySetAuth(authHeader);
                ChatResult result = processChat(request);
                String reply = result.reply;

                for (int i = 0; i < reply.length(); i += 3) {
                    String chunk = reply.substring(i, Math.min(i + 3, reply.length()));
                    emitter.send(SseEmitter.event().data(chunk));
                    Thread.sleep(15);
                }

                if (!result.books.isEmpty()) {
                    emitter.send(SseEmitter.event().name("books").data(objectMapper.writeValueAsString(result.books)));
                }

                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().data("抱歉，处理您的请求时出现了问题，请稍后再试。"));
                    emitter.complete();
                } catch (Exception ignored) {}
            } finally {
                AuthContext.remove();
            }
        }).start();

        return emitter;
    }

    private ChatResult processChat(Map<String, Object> request) throws Exception {
        List<Map<String, Object>> incomingMessages = (List<Map<String, Object>>) request.get("messages");
        ArrayNode messages = objectMapper.createArrayNode();
        for (Map<String, Object> msg : incomingMessages) {
            ObjectNode node = messages.addObject();
            String role = (String) msg.get("role");
            if ("ai".equals(role)) role = "assistant";
            node.put("role", role);
            String content = (String) msg.getOrDefault("content", "");
            node.put("content", content);
        }

        String systemPrompt = buildSystemPrompt();
        JsonNode response = callAnthropic(messages, systemPrompt, true);

        int iterations = 0;
        while (isToolUse(response) && iterations < MAX_ITERATIONS) {
            JsonNode choice = response.get("choices").get(0);
            JsonNode assistantMsg = choice.get("message");

            ObjectNode assistantNode = messages.addObject();
            assistantNode.put("role", "assistant");
            if (assistantMsg.has("content") && !assistantMsg.get("content").isNull()) {
                assistantNode.put("content", assistantMsg.get("content").asText());
            }
            if (assistantMsg.has("tool_calls")) {
                assistantNode.set("tool_calls", assistantMsg.get("tool_calls"));
            }

            for (JsonNode toolCall : assistantMsg.get("tool_calls")) {
                String id = toolCall.get("id").asText();
                String name = toolCall.get("function").get("name").asText();
                String args = toolCall.get("function").get("arguments").asText();
                String result = executeTool(name, args);

                ObjectNode toolResult = messages.addObject();
                toolResult.put("role", "tool");
                toolResult.put("tool_call_id", id);
                toolResult.put("content", result);
            }

            response = callAnthropic(messages, systemPrompt, true);
            iterations++;
        }

        String reply = extractText(response);
        List<BookVO> books = extractBookRefs(reply);
        return new ChatResult(reply, books);
    }

    private record ChatResult(String reply, List<BookVO> books) {}

    private void trySetAuth(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) return;
        try {
            String token = authHeader;
            if (token.startsWith("Bearer ")) token = token.substring(7);
            if (!jwtUtils.isTokenExpired(token)) {
                Long userId = jwtUtils.getUserId(token);
                String role = jwtUtils.getRole(token);
                var user = new com.example.bookstore.entity.User();
                user.setId(userId);
                user.setRole(role);
                AuthContext.setCurrentUser(user);
            }
        } catch (Exception ignored) {}
    }

    private String buildSystemPrompt() {
        List<Book> books = bookMapper.selectList(null);
        String bookList = books.stream()
                .filter(b -> b.getStatus() != 0)
                .map(b -> String.format("- 《%s》| %s | ¥%s | %s",
                        b.getTitle(), b.getAuthor(), b.getPrice(),
                        truncate(b.getDescription(), 120)))
                .collect(Collectors.joining("\n"));

        return """
你是一家名为"拾光书屋"的书店AI助手。你的工作是帮助用户找书、推荐书、介绍书籍内容，以及执行购书相关操作。

## 当前用户
%s

## 书店书籍目录
%s

## 可用功能
你可以在回答中调用以下函数：
1. searchBooks(keyword) — 按关键词搜索书籍，返回匹配的书籍列表
2. getBookDetail(bookId) — 获取某本书的详细信息（含完整描述）
3. addToCart(bookId, quantity) — 将书籍加入购物车（需要用户登录）
4. getCartItems() — 查看当前购物车内容
5. listAddresses() — 查看用户的收货地址列表

## 回复要求
- 回答简洁友好，使用中文
- 当用户询问某本书的内容时，先调用 getBookDetail 获取详细信息后再回答
- 当用户想买书或加入购物车时，先调用 searchBooks 找到书籍，再调用 addToCart
- 推荐书时，可以一次推荐多本，说明推荐理由
- 介绍书时，加入对该书风格、适合人群的点评
- 不要编造书籍信息，只使用系统中已有的书籍
- 可用一些颜文字或符号让回复更生动
""".formatted(
        AuthContext.getCurrentUserId() != null
                ? "已登录用户(ID: " + AuthContext.getCurrentUserId() + ")，可以执行购物车和下单操作"
                : "游客（未登录），只能浏览和搜索书籍",
        bookList
        );
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "……" : s;
    }

    private String executeTool(String name, String args) {
        return switch (name) {
            case "searchBooks" -> executeSearchBooks(args);
            case "getBookDetail" -> executeGetBookDetail(args);
            case "addToCart" -> executeAddToCart(args);
            case "getCartItems" -> executeGetCartItems();
            case "listAddresses" -> executeListAddresses();
            default -> "{\"error\": \"unknown tool: " + name + "\"}";
        };
    }

    private String executeSearchBooks(String args) {
        try {
            JsonNode params = objectMapper.readTree(args);
            String keyword = params.has("keyword") ? params.get("keyword").asText() : "";
            var query = new com.example.bookstore.dto.BookQueryDTO();
            query.setKeyword(keyword);
            query.setPageSize(10);
            var page = bookService.pageQuery(query);
            List<Map<String, Object>> list = page.getRecords().stream().map(v -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", v.getId().toString());
                m.put("title", v.getTitle());
                m.put("author", v.getAuthor());
                m.put("price", v.getPrice());
                m.put("avgRating", v.getAvgRating());
                m.put("description", truncate(getBookDesc(v.getId()), 200));
                m.put("stock", v.getStock());
                return m;
            }).collect(Collectors.toList());
            return objectMapper.writeValueAsString(Map.of("books", list));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String executeGetBookDetail(String args) {
        try {
            JsonNode params = objectMapper.readTree(args);
            Long bookId = params.has("bookId") ? Long.parseLong(params.get("bookId").asText()) : 0;
            var detail = bookService.getDetail(bookId);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", detail.getId().toString());
            m.put("title", detail.getTitle());
            m.put("author", detail.getAuthor());
            m.put("price", detail.getPrice());
            m.put("avgRating", detail.getAvgRating());
            m.put("description", detail.getDescription());
            m.put("publisher", detail.getPublisher());
            m.put("stock", detail.getStock());
            m.put("sales", detail.getSales());
            m.put("categoryName", detail.getCategoryName());
            return objectMapper.writeValueAsString(m);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String executeAddToCart(String args) {
        Long userId = AuthContext.getCurrentUserId();
        if (userId == null) return "{\"error\": \"请先登录后再添加购物车\"}";
        try {
            JsonNode params = objectMapper.readTree(args);
            String bookId = params.has("bookId") ? params.get("bookId").asText() : "";
            int quantity = params.has("quantity") ? params.get("quantity").asInt() : 1;
            var dto = new CartAddDTO();
            dto.setBookId(bookId);
            dto.setQuantity(quantity);
            cartService.add(userId, dto);
            return "{\"success\": true, \"message\": \"已加入购物车\"}";
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String executeGetCartItems() {
        Long userId = AuthContext.getCurrentUserId();
        if (userId == null) return "{\"error\": \"请先登录\"}";
        try {
            var items = cartService.getList(userId);
            List<Map<String, Object>> list = items.stream().map(v -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", v.getId().toString());
                m.put("bookId", v.getBookId().toString());
                m.put("title", v.getBookTitle());
                m.put("author", v.getBookAuthor());
                m.put("price", v.getPrice());
                m.put("quantity", v.getQuantity());
                return m;
            }).collect(Collectors.toList());
            return objectMapper.writeValueAsString(Map.of("items", list));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String executeListAddresses() {
        Long userId = AuthContext.getCurrentUserId();
        if (userId == null) return "{\"error\": \"请先登录\"}";
        try {
            var list = addressService.getList(userId);
            List<Map<String, Object>> result = list.stream().map(v -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", v.getId().toString());
                m.put("receiverName", v.getReceiverName());
                m.put("phone", v.getPhone());
                m.put("address", v.getProvince() + v.getCity() + v.getDistrict() + v.getDetailAddress());
                m.put("isDefault", v.getIsDefault());
                return m;
            }).collect(Collectors.toList());
            return objectMapper.writeValueAsString(Map.of("addresses", result));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String getBookDesc(Long bookId) {
        try {
            return bookService.getDetail(bookId).getDescription();
        } catch (Exception e) {
            return "";
        }
    }

    private JsonNode callAnthropic(ArrayNode messages, String systemPrompt, boolean withTools) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", MODEL);
            body.put("max_tokens", 2048);

            ArrayNode msgs = objectMapper.createArrayNode();
            ObjectNode sysMsg = msgs.addObject();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            for (JsonNode m : messages) {
                msgs.add(m);
            }
            body.set("messages", msgs);

            if (withTools) {
                body.set("tools", buildTools());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            String jsonBody = objectMapper.writeValueAsString(body);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            ObjectNode fallback = objectMapper.createObjectNode();
            ArrayNode choices = fallback.putArray("choices");
            ObjectNode choice = choices.addObject();
            choice.put("finish_reason", "stop");
            ObjectNode message = choice.putObject("message");
            message.put("role", "assistant");
            message.put("content", "抱歉，我现在无法连接服务器，请稍后再试。");
            return fallback;
        }
    }

    private boolean isToolUse(JsonNode response) {
        try {
            JsonNode choices = response.get("choices");
            if (choices == null || choices.isEmpty()) return false;
            String reason = choices.get(0).get("finish_reason").asText();
            return "tool_calls".equals(reason);
        } catch (Exception e) {
            return false;
        }
    }

    private String extractText(JsonNode response) {
        try {
            JsonNode choices = response.get("choices");
            if (choices == null || choices.isEmpty()) return "";
            JsonNode message = choices.get(0).get("message");
            if (message != null && message.has("content") && !message.get("content").isNull()) {
                return message.get("content").asText();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private List<BookVO> extractBookRefs(String reply) {
        List<BookVO> result = new ArrayList<>();
        try {
            for (var book : bookService.pageQuery(new com.example.bookstore.dto.BookQueryDTO()).getRecords()) {
                if (reply.contains(book.getTitle())) {
                    result.add(book);
                    if (result.size() >= 5) break;
                }
            }
        } catch (Exception ignored) {}
        return result;
    }

    private ArrayNode buildTools() {
        ArrayNode tools = objectMapper.createArrayNode();

        tools.add(buildTool("searchBooks",
                "按关键词搜索书籍，返回匹配的书籍列表",
                """
                {
                    "type": "object",
                    "properties": {
                        "keyword": {"type": "string", "description": "搜索关键词，支持书名和作者"}
                    },
                    "required": ["keyword"]
                }"""));

        tools.add(buildTool("getBookDetail",
                "获取某本书的详细信息，含完整描述、评分、库存等",
                """
                {
                    "type": "object",
                    "properties": {
                        "bookId": {"type": "string", "description": "书籍ID"}
                    },
                    "required": ["bookId"]
                }"""));

        tools.add(buildTool("addToCart",
                "将书籍加入购物车（需要登录）",
                """
                {
                    "type": "object",
                    "properties": {
                        "bookId": {"type": "string", "description": "书籍ID"},
                        "quantity": {"type": "integer", "description": "数量，默认为1"}
                    },
                    "required": ["bookId"]
                }"""));

        tools.add(buildTool("getCartItems",
                "查看当前购物车内容",
                """
                {
                    "type": "object",
                    "properties": {},
                    "required": []
                }"""));

        tools.add(buildTool("listAddresses",
                "查看用户的收货地址列表",
                """
                {
                    "type": "object",
                    "properties": {},
                    "required": []
                }"""));

        return tools;
    }

    private ObjectNode buildTool(String name, String description, String parametersJson) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("type", "function");
        ObjectNode func = tool.putObject("function");
        func.put("name", name);
        func.put("description", description);
        try {
            func.set("parameters", objectMapper.readTree(parametersJson));
        } catch (JsonProcessingException e) {
            func.putObject("parameters");
        }
        return tool;
    }
}
