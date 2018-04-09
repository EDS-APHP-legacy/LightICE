package fakedds;

import java.util.ArrayList;
import java.util.List;

public class Topic {
    private List<Subscriber> subscribers;
    public String name;

    public Topic(String name) {
        this.name = name;
        this.subscribers = new ArrayList<>();
    }

    public void addSubscriber(Subscriber s) {
        this.subscribers.add(s);
    }

    public void write(String message) {
        for (Subscriber s: subscribers) {
            s.handle(this, message);
        }
    }
}
