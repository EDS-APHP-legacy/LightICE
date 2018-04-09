package fakedds;

public class Subscriber {
    private final String name;

    public Subscriber(String name) {
        this.name = name;
    }

    public void handle(Topic topic, String message) {
        System.out.println("Subscriber " + name + " received message '" + message + "' from " + topic.name);
    }
}
