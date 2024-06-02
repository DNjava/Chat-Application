import org.java_websocket.WebSocket;

public class User {
    private String username;
    private WebSocket connection;

    public User(String username, WebSocket connection) {
        this.username = username;
        this.connection = connection;
    }

    public String getUsername() {
        return username;
    }

    public WebSocket getConnection() {
        return connection;
    }
}
