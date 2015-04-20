
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class StartUp {

    /////////////////////////////////////////
    //VERANDER HIER DE TE TESTEN PARAMETERS//
    /////////////////////////////////////////
    //NOOT: parameters zijn strings omdat ze opgeslagen worden in een tekstbestand (behalve aantal rondes)
    //aantal testrondes die gesimuleerd worden
    //hoe hoger het aantal hoe nauwkeuriger de uitkomst maar hoe langer de simulatie loopt
    private final static int aantalRondes = 1000000;

    //aantal decks
    private final static String aantalDecks = "8";

    //als de open kaart van bank A of J/Q/K is zal hij de andere kaart checken
    //in geval van blackjack verliest iedereen die geen blackjack heeft
    //mensen die wel blackjack hebben pushen
    private final static String bankCheckForBlackjack = "false";

    //betaling voor blacjack, standaard 1.5 (3 for 2)
    private final static String blackjackPayoff = "1.5";

    //speler mag eender welke 2-hand kaart opgeven (krijgt helft inzet terug)
    //dit kan niet wanneer de bankt checkt voor blackjack en blackjack heeft
    private final static String surrender = "true";

    //dealer moet hitten als hij zachte 17 heeft (dwz hij heeft een A die 11 waard is)
    private final static String dealerHitOn17 = "false";

    //dubbelen na splitten toegelaten
    private final static String allowedDAS = "true";
    //hersplitten toegelaten
    private final static String resplits = "true";

    ////////////////////
    //EINDE PARAMETERS//
    ////////////////////
    
    //voor te testen als pack wel klopte; wordt momenteel niet gebruikt
    private final static Comparator<Kaart> bySuit = (kaart1, kaart2) -> kaart1.getSuit().compareTo(kaart2.getSuit());
    private final static Comparator<Kaart> byValue = (Kaart k, Kaart k2) -> k.getValue() - k2.getValue();

    public static void main(String[] args) {
        regels();
        Simulatie simulatie = new Simulatie();
        simulatie.setStrategie(new GenericStrategy(simulatie));
//        Map<Double, Integer> teller = new HashMap();
        
        for (int i = 0; i < aantalRondes; i++) {
            simulatie.creerPack();
            double resultaat = simulatie.speelRonde();
//            Integer aantal = teller.get(resultaat);
//            if (aantal == null) {
//                teller.put(resultaat, 1);
//            } else {
//                teller.put(resultaat, aantal + 1);
//            }
        }
        
//        Map<Double, Integer> sortedTeller = new TreeMap(teller);
        
        String output = simulatie.toonWinst();

        String[] lijnen = output.split("\n");

        try {
            File f = new File("data.txt");
            PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(f)));
            w.println("Resultaat na " + aantalRondes + " gesimuleerde rondes met volgende parameters: ");
            w.println("Aantal decks: " + aantalDecks);
            w.println("Uitbetaling bij blackjack: " + blackjackPayoff);
            w.println("Bank checkt voor blackjack: " + (Boolean.parseBoolean(bankCheckForBlackjack) ? "ja" : "nee"));
            w.println("Opgeven mag: " + (Boolean.parseBoolean(surrender) ? "ja" : "nee"));
            w.println("Dealer hit op zachte 17: " + (Boolean.parseBoolean(dealerHitOn17) ? "ja" : "nee"));
            w.println("Dubbelen na splitten mag: " + (Boolean.parseBoolean(allowedDAS) ? "ja" : "nee"));
            w.println("Hersplitten mag: " + (Boolean.parseBoolean(resplits) ? "ja" : "nee"));

            w.println("");
            for (String s : lijnen) {
                w.println(s);
            }

//            for (Map.Entry<Double, Integer> entry : sortedTeller.entrySet()) {
//                System.out.println("Winst: " + entry.getKey() + "\tAantal keer: "
//                        + entry.getValue());
//            }
            
            w.close();
        } catch (IOException ex) {

        }
    }

    protected static void regels() {
        Properties prop = new Properties();
        OutputStream output = null;

        try {
            output = new FileOutputStream("config.properties");

            // set the properties value
            prop.setProperty("numberOfDecks", aantalDecks);
            prop.setProperty("numberOfPlayers", "1");
            prop.setProperty("dealerHitOn17", dealerHitOn17);
            prop.setProperty("doubleWhenSoft", "false");
            prop.setProperty("allowedDAS", allowedDAS);
            prop.setProperty("resplits", resplits);
            prop.setProperty("bankCheckForBlackjack", bankCheckForBlackjack);
            prop.setProperty("blackjackPayoff", blackjackPayoff);
            prop.setProperty("enterDuringShuffle", "false");
            prop.setProperty("surrender", surrender);

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
