
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Simulatie {

    //aantal boeken
    int numberOfDecks;
    //aantal spelers
    int numberOfPlayers;
    //als bank zachte 17 heeft: verplicht hit (zacht -> A = 11)
    boolean dealerHitOn17;
    //zachte handen mogen gedubbeld worden
    boolean doubleWhenSoft;
    //dubbelen na splitten toegelaten
    boolean allowedDAS;
    //hersplitten toegelaten
    boolean resplits;
    //bank bekijkt hole kaart om te checken voor blackjack
    //wanneer hij A/10 toont
    boolean dealerCheckForBlackjack;
    //betaling voor blackjack (standaard 3 voor 2)
    double blackjackPayoff;
    //spelers kunnen alleen beginnen meedoen tijdens een shuffle
    boolean enterDuringShuffle;
    //speler mag opgeven als hij 2 kaarten heeft en bank geen blackjack heeft
    //hij krijgt helft van inzet terug
    boolean surrender;
    //alle kaarten
    List<Kaart> pack;

    List<Speler> spelers;

    GenericStrategy strategie;

    int eersteBankKaartWaarde;
    int tweedeBankKaartWaarde;
    
    int aantalBusts, aantalWins, aantalTies, aantalBlackjacks, aantalLosses;

    public Simulatie() {
        aantalBlackjacks = 0; aantalWins = 0; aantalTies = 0; aantalBusts = 0; aantalLosses = 0;
        laadRegels();
        creerPack();
        spelers = new ArrayList();
        for (int i = 0; i < numberOfPlayers; i++) {
            spelers.add(new Speler(1000000, strategie));
        }
    }

    public void setStrategie(GenericStrategy strategie) {
        this.strategie = strategie;
    }

    protected void laadRegels() {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("config.properties");

            // load a properties file
            prop.load(input);

            numberOfDecks = Integer.parseInt(prop.getProperty("numberOfDecks"));
            numberOfPlayers = Integer.parseInt(prop.getProperty("numberOfPlayers"));
            dealerHitOn17 = Boolean.parseBoolean(prop.getProperty("dealerHitOn17"));
            doubleWhenSoft = Boolean.parseBoolean(prop.getProperty("doubleWhenSoft"));
            allowedDAS = Boolean.parseBoolean(prop.getProperty("allowedDAS"));
            resplits = Boolean.parseBoolean(prop.getProperty("resplits"));
            dealerCheckForBlackjack = Boolean.parseBoolean(prop.getProperty("dealerCheckForBlackjack"));
            blackjackPayoff = Double.parseDouble(prop.getProperty("blackjackPayoff"));
            enterDuringShuffle = Boolean.parseBoolean(prop.getProperty("enterDuringShuffle"));
            surrender = Boolean.parseBoolean(prop.getProperty("surrender"));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void creerPack() {
        pack = new ArrayList();
        for (int i = 0; i < numberOfDecks; i++) {
            for (int j = 1; j <= 13; j++) {
                for (int k = 0; k < 4; k++) {
                    pack.add(new Kaart(j, k));
                }
            }
        }
        Collections.shuffle(pack);
    }

    public void speelRonde() {
//        Speler eersteSpeler = spelers.get(0);
//        spelers = new ArrayList();
//        spelers.add(eersteSpeler);
        for (int i = 0; i < numberOfPlayers; i++) {
            spelers.get(i).reset();
            spelers.get(i).addKaart(pack.remove(0));
            spelers.get(i).addKaart(pack.remove(0));
            spelers.get(i).setInzet(100);
        }
        Kaart eersteBankKaart = pack.remove(0);
        eersteBankKaartWaarde = berekenKaartWaarde(eersteBankKaart.getValue());
        Kaart tweedeBankKaart = pack.remove(0);
        tweedeBankKaartWaarde = berekenKaartWaarde(tweedeBankKaart.getValue());

        for (int i = 0; i < spelers.size(); i++) {
            strategie.reedsGesplit = false;
            speelBeurtSpeler(i);
        }

        List<Kaart> bankHand = speelRondeBank(eersteBankKaart, tweedeBankKaart);
        int totaal = bankHand.get(bankHand.size() - 1).getValue();

        System.out.print("BANK: ");
        for (int i = 0; i < bankHand.size() - 1; i++) {
            System.out.print(bankHand.get(i) + " ");
        }

        System.out.println(" TOTAAL: " + totaal);
        for (int i = 0; i < spelers.size(); i++) {
            System.out.print("SPELER " + (i + 1) + ": ");
            Speler speler = spelers.get(i);
            for (int j = 0; j < speler.getHand().size(); j++) {
                System.out.print(speler.getHand().get(j) + " ");
            }
            System.out.println("TOTAAL: " + speler.bepaalTotaalHand());
        }

        for (int i = 0; i < spelers.size(); i++) {
            Speler speler = spelers.get(i);
            if (speler.bepaalTotaalHand() > 21) {
                if (speler.isGesplitHand()) {
                    speler.afrekenen(-1);
                    spelers.get(i - 1).voegGeldToe(speler.getGeld());
                    spelers.remove(i);
                    i--;
                } else {
                    speler.afrekenen(-1);
                }
                System.out.println("SPELER " + (i+1) + ": BUST\n");
                aantalBusts++;
            }
        }
        if (totaal > 21) {
            for (int i = 0; i < spelers.size(); i++) {
                Speler speler = spelers.get(i);
                if (speler.bepaalTotaalHand() == 21) {
                    if (speler.isGesplitHand()) {
                        speler.afrekenen(blackjackPayoff);
                        spelers.get(i - 1).voegGeldToe(speler.getGeld());
                        spelers.remove(i);
                        i--;
                    } else {
                        speler.afrekenen(blackjackPayoff);
                    }
                    System.out.println("SPELER " + (i+1) + ": BLACKJACK");
                    aantalBlackjacks++;
                }
                if (speler.bepaalTotaalHand() < 21 && !speler.isSurrendered()) {
                    if (speler.isGesplitHand()) {
                        speler.afrekenen(1);
                        spelers.get(i - 1).voegGeldToe(speler.getGeld());
                        spelers.remove(i);
                        i--;
                    } else {
                        speler.afrekenen(1);
                    }
                    System.out.println("SPELER " + (i+1) + ": WIN");
                    aantalWins++;
                }
            }
        } else {
            for (int i = 0; i < spelers.size(); i++) {
                Speler speler = spelers.get(i);
                int spelerTotaal = speler.bepaalTotaalHand();

                if (spelerTotaal == totaal) {
                    if (speler.isGesplitHand()) {
                        spelers.remove(i);
                        i--;
                    } else {

                    }
                    System.out.println("SPELER " + (i+1) + ": TIE");
                    aantalTies++;
                } else if (spelerTotaal > totaal && spelerTotaal < 21) {
                    if (speler.isGesplitHand()) {
                        speler.afrekenen(1);
                        spelers.get(i - 1).voegGeldToe(speler.getGeld());
                        spelers.remove(i);
                        i--;
                    } else {
                        speler.afrekenen(1);
                    }
                    System.out.println("SPELER " + (i+1) + ": WIN");
                    aantalWins++;
                } else if (spelerTotaal == 21) {
                    if (speler.isGesplitHand()) {
                        speler.afrekenen(blackjackPayoff);
                        spelers.get(i - 1).voegGeldToe(speler.getGeld());
                        spelers.remove(i);
                        i--;
                    } else {
                        speler.afrekenen(blackjackPayoff);
                    }
                    
                    System.out.println("SPELER " + (i+1) + ": BLACKJACK");
                    aantalBlackjacks++;
                } else if (spelerTotaal < totaal) {
                    if (speler.isGesplitHand()) {
                        speler.afrekenen(-1);
                        spelers.get(i - 1).voegGeldToe(speler.getGeld());
                        spelers.remove(i);
                        i--;
                    } else {
                        speler.afrekenen(-1);
                    }
                    
                    System.out.println("SPELER " + (i+1) + ": LOSS");
                    aantalLosses++;
                }
            }
        }

        System.out.println("TOTAAL GELD: " + spelers.get(0).getGeld() + "\n");
    }

    private void speelBeurtSpeler(int i) {
        String actie = strategie.speelRonde(spelers.get(i).getHand());
        switch (actie) {
            case "double":
                spelers.get(i).addKaart(pack.remove(0));
                spelers.get(i).dubbel();
                break;
            case "hit":
                spelers.get(i).addKaart(pack.remove(0));
                speelBeurtSpeler(i);
                break;
            case "stand":
                break;
            case "surrender":
                spelers.get(i).surrender();
                break;
            case "split":
                spelers.add(i + 1, new Speler(0, strategie));
                spelers.get(i + 1).addKaart(spelers.get(i).getHand().remove(0));
                spelers.get(i + 1).setGesplitHand(true);
                spelers.get(i).addKaart(pack.remove(0));
                spelers.get(i + 1).addKaart(pack.remove(0));

                speelBeurtSpeler(i);
        }

    }

    public int berekenKaartWaarde(int kaart) {
        if (kaart <= 10) {
            return kaart;
        }
        return 10;
    }

    protected List<Kaart> speelRondeBank(Kaart eersteKaart, Kaart tweedeKaart) {

        List<Kaart> kaartenBank = new ArrayList();
        kaartenBank.add(eersteKaart);
        kaartenBank.add(tweedeKaart);
        int hand = berekenKaartWaarde(eersteKaart.getValue()) + berekenKaartWaarde(tweedeKaart.getValue());

        int aantalElven = 0;
        if (eersteKaart.getValue() == 1) {
            aantalElven++;
            hand += 10;
        } else if (tweedeKaart.getValue() == 1) {
            aantalElven++;
            hand += 10;
        }

        while (hand < 17 || (hand == 17 && dealerHitOn17 && aantalElven > 0)) {
            Kaart volgendeKaart = pack.remove(0);
            hand += berekenKaartWaarde(volgendeKaart.getValue());
            kaartenBank.add(volgendeKaart);
            if (volgendeKaart.getValue() == 1) {
                aantalElven++;
                hand += 10;
            }

            if (hand > 21 && aantalElven > 0) {
                aantalElven--;
                hand -= 10;
            }
        }

        kaartenBank.add(new Kaart(hand, 0));

        return kaartenBank;
    }
    
    public void toonWinst() {
        System.out.println("");
        System.out.println("Totaal wins: " + aantalWins);
        System.out.println("Totaal blackjacks: " + aantalBlackjacks);
        System.out.println("Totaal busts: " + aantalBusts);
        System.out.println("Totaal losses: " + aantalLosses);
        System.out.println("Totaal ties: " + aantalTies + "\n");
        
        System.out.println("Winst %: " + ((spelers.get(0).getGeld() - 1000000.0)/1000000.0) );
    }
}
