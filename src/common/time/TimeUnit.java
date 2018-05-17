package common.time;

public enum TimeUnit {
    SEC ("second"),
    MS ("Millisecond"),
    µS ("Microsecond"),
    NS ("Nanosecond");

    private String name = "";

    TimeUnit(String name){
        this.name = name;
    }

    public String toString(){
        return name;
    }
}