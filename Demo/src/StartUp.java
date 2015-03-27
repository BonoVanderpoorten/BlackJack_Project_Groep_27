
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Properties;


public class StartUp {

    //voor te testen als pack wel klopte; wordt momenteel niet gebruikt
    private final static Comparator<Kaart> bySuit = (kaart1, kaart2) -> kaart1.getSuit().compareTo(kaart2.getSuit());
    private final static Comparator<Kaart> byValue = (Kaart k, Kaart k2) -> k.getValue() - k2.getValue();
    
    public static void main(String[] args) {
        standaardRegels();
        Simulatie simulatie = new Simulatie();
        simulatie.setStrategie(new GenericStrategy(simulatie));
        for (int i = 0; i < 1000; i++) {
            simulatie.speelRonde();
            simulatie.creerPack();
        }
        simulatie.toonWinst();
    }
    
    protected static void standaardRegels() {
        Properties prop = new Properties();
        OutputStream output = null;

        try {
            output = new FileOutputStream("config.properties");

            // set the properties value
            prop.setProperty("numberOfDecks", "4");
            prop.setProperty("numberOfPlayers", "1");
            prop.setProperty("dealerHitOn17", "false");
            prop.setProperty("doubleWhenSoft", "true");
            prop.setProperty("allowedDAS", "true");
            prop.setProperty("resplits", "true");
            prop.setProperty("bankCheckForBlackjack", "true");
            prop.setProperty("blackjackPayoff", "1.5");
            prop.setProperty("enterDuringShuffle", "false");
            prop.setProperty("allowedSurrender", "false");

            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
