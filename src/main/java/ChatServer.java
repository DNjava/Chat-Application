import com.google.gson.Gson;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class ChatServer extends WebSocketServer {

    private Set<User> users = Collections.synchronizedSet(new HashSet<>());
    private List<Message> messageHistory = Collections.synchronizedList(new ArrayList<>());
    private Gson gson = new Gson();

    public ChatServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // Ожидание авторизации
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (message.startsWith("/login")) {
            handleLogin(conn, message);
        } else {
            User sender = getUserByConnection(conn);
            if (sender != null) {
                Message msg = new Message(sender.getUsername(), message);
                messageHistory.add(msg);
                broadcast(gson.toJson(msg));
                System.out.println("Сообщение от " + sender.getUsername() + ": " + message);
            }
        }
    }

    private void handleLogin(WebSocket conn, String message) {
        String username = message.split(" ", 2)[1];
        if (getUserByUsername(username) == null) {
            users.add(new User(username, conn));
            conn.send("Добро пожаловать, " + username);
            broadcast(username + " присоединился к чату.");
            System.out.println(username + " подключился.");

            // Отправка истории сообщений новому пользователю
            for (Message msg : messageHistory) {
                conn.send(gson.toJson(msg));
            }
        } else {
            conn.send("Пользователь с таким именем уже существует.");
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        User user = getUserByConnection(conn);
        if (user != null) {
            users.remove(user);
            broadcast(user.getUsername() + " покинул чат.");
            System.out.println(user.getUsername() + " отключился.");
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            User user = getUserByConnection(conn);
            if (user != null) {
                users.remove(user);
                conn.close();
            }
        }
    }

    @Override
    public void onStart() {
        System.out.println("Сервер запущен и готов к приему подключений!");
    }

    @Override
    public void broadcast(String message) {
        synchronized (users) {
            for (User user : users) {
                user.getConnection().send(message);
            }
        }
    }

    private User getUserByConnection(WebSocket conn) {
        for (User user : users) {
            if (user.getConnection().equals(conn)) {
                return user;
            }
        }
        return null;
    }

    private User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8887;
        InetSocketAddress address = new InetSocketAddress(host, port);
        ChatServer server = new ChatServer(address);
        server.start();
        System.out.println("Сервер запущен на " + host + ":" + port);
    }
}
