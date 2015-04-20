
public class Kaart{

    private int value;
    private String suit;

    public Kaart(int value, int suitValue) {
        this.value = value;
        suit = bepaalSuit(suitValue);
    }

    private String bepaalSuit(int suitValue) {
        switch (suitValue) {
            case 0:
                return "♠";
            case 1:
                return "♥";
            case 2:
                return "♦";
            case 3:
                return "♣";
            default:
                return "";
        }
    }

    public int getValue() {
        return value;
    }

    public String getSuit() {
        return suit;
    }

    @Override
    public String toString() {
        String face;
        switch (value) {
            case 1:
                face = "A"; break;
            case 11:
                face = "J"; break;
            case 12:
                face = "Q"; break;
            case 13:
                face = "K"; break;
            default:
                face = Integer.toString(value);
        }
        return face + suit;
    }

}
