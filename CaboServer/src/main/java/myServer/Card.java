package myServer;

public class Card {

    private String type;
    private int value;
    // NOTE: The special effects of a card, such as `spy`, `peek`, `swap`
    private String choice;

    public Card (int value, String type){
        this.value=value;
        this.type=type;
    }

    public Card (int value, String type, String choice) {
        this.value = value;
        this.type = type;
        this.choice = choice;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void peek() {

    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<Card: ");
        buffer.append(value);
        buffer.append(", ");
        buffer.append(type);
        if (choice != null) {
            buffer.append(", ");
            buffer.append(choice);
        }
        buffer.append(">");
        return buffer.toString();
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Card)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Card c = (Card) o;

        // Compare the data members and return accordingly
        return this.value == c.value &&
                this.type == c.type &&
                this.choice == c.choice;
    }
}
