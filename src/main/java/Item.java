public class Item {
    private final Long id;
    private final String value;
    private String state;

    public Item(Long id, String value, String state) {
        this.id = id;
        this.value = value;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getState() {
        return state;
    }
}