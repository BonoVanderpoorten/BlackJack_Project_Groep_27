
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

    int totaalW = 0, totaalL = 0;
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
    boolean bankCheckForBlackjack;
    //betaling voor blackjack (standaard 3 voor 2)
    double blackjackPayoff;
    //spelers kunnen alleen beginnen meedoen tijdens een shuffle
    boolean enterDuringShuffle;
    //speler mag opgeven als hij 2 kaarten heeft en bank geen blackjack heeft
    //hij krijgt helft van inzet terug
    boolean surrender;

    private static final double startGeldSpelers = 0;

    //alle kaarten
    List<Kaart> pack;

    List<Speler> spelers;

    GenericStrategy strategie;

    int eersteBankKaartWaarde;
    int tweedeBankKaartWaarde;

    int aantalBusts, aantalWins, aantalTies, aantalBlackjacks, aantalLosses, aantalSurrenders;
    int aantalDoubledBusts, aantalDoubledWins, aantalDoubledTies, aantalDoubledLosses;

    public Simulatie() {
        aantalBlackjacks = 0;
        aantalWins = 0;
        aantalTies = 0;
        aantalBusts = 0;
        aantalLosses = 0;
        aantalSurrenders = 0;
        aantalDoubledWins = 0;
        aantalDoubledTies = 0;
        aantalDoubledBusts = 0;
        aantalDoubledLosses = 0;
        laadRegels();
        creerPack();
        spelers = new ArrayList();
        for (int i = 0; i < numberOfPlayers; i++) {
            spelers.add(new Speler(startGeldSpelers, strategie));
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
            bankCheckForBlackjack = Boolean.parseBoolean(prop.getProperty("bankCheckForBlackjack"));
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

    public double speelRonde() {
//        Speler eersteSpeler = spelers.get(0);
//        spelers = new ArrayList();
//        spelers.add(eersteSpeler);
        for (int i = 0; i < numberOfPlayers; i++) {
            spelers.get(i).reset();
            spelers.get(i).addKaart(pack.remove(0));
            spelers.get(i).addKaart(pack.remove(0));
            spelers.get(i).setInzet(10);
        }
        Kaart eersteBankKaart = pack.remove(0);
        eersteBankKaartWaarde = berekenKaartWaarde(eersteBankKaart.getValue());
        Kaart tweedeBankKaart = pack.remove(0);
        tweedeBankKaartWaarde = berekenKaartWaarde(tweedeBankKaart.getValue());

        strategie.reedsGesplit = false;

        if (!bankCheckForBlackjack
                || (eersteBankKaartWaarde != 1 || tweedeBankKaartWaarde != 10) && (eersteBankKaartWaarde != 10 || tweedeBankKaartWaarde != 1)) {

            for (int i = 0; i < spelers.size(); i++) {
                strategie.reedsGesplit = false;
                speelBeurtSpeler(i);
            }
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
            if (speler.bepaalTotaalHand() > 21 || speler.isSurrendered()) {

                if (speler.isSurrendered()) {
                    System.out.println("SPELER " + (i + 1) + ": " + "SURRENDERED");
                    aantalSurrenders++;
                } else {
                    System.out.println("SPELER " + (i + 1) + ": " + (speler.isDoubled() ? "DOUBLED " : "") + "BUST");
                    if (speler.isDoubled()) {
                        aantalDoubledBusts++;
                    } else {
                        aantalBusts++;
                    }
                }

                if (speler.isGesplitHand()) {
                    speler.afrekenen(-1);
                    spelers.get(i - 1).voegGeldToe(speler.getGeld(), speler.getInzet());
                    spelers.remove(i);
                    i--;
                } else {
                    speler.afrekenen(-1);
                }

            }
        }
        if (totaal > 21) {
            for (int i = 0; i < spelers.size(); i++) {
                Speler speler = spelers.get(i);
                if (!speler.isSurrendered()) {
                    if (speler.bepaalTotaalHand() == 21 && speler.getHand().size() == 2 && !(speler.isGesplitHand() || speler.heeftGesplit())) {
                        System.out.println("SPELER " + (i + 1) + ": BLACKJACK");
                        if (speler.isGesplitHand()) {
                            speler.afrekenen(1);
                            spelers.get(i - 1).voegGeldToe(speler.getGeld(), speler.getInzet());
                            spelers.remove(i);
                            i--;
                        } else {
                            speler.afrekenen(blackjackPayoff);
                        }
                        aantalBlackjacks++;
                    } else if (speler.bepaalTotaalHand() <= 21) {
                        System.out.println("SPELER " + (i + 1) + ": " + (speler.isDoubled() ? "DOUBLED " : "") + "WIN");
                        if (speler.isGesplitHand()) {
                            speler.afrekenen(1);
                            spelers.get(i - 1).voegGeldToe(speler.getGeld(), speler.getInzet());
                            spelers.remove(i);
                            i--;
                        } else {
                            speler.afrekenen(1);
                        }

                        if (speler.isDoubled()) {
                            aantalDoubledWins++;
                        } else {
                            aantalWins++;
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < spelers.size(); i++) {
                Speler speler = spelers.get(i);
                if (!speler.isSurrendered()) {
                    int spelerTotaal = speler.bepaalTotaalHand();

                    if (spelerTotaal == totaal) {
                        if (totaal == 21) {
                            //bankhand.size is gelijk aan 3 wanneer blackjack, omdat de slechte methode speelRondeBank() een lijst teruggeeft van kaarten
                            //deze lijst bevat de kaarten van de bank + een zogezegde dummykaart waarvan de value gelijk is aan de totale waarde van de bank zijn hand
                            //deze dummykaart is dus GEEN onderdeel van de bank zijn werkelijke hand!
                            if (bankHand.size() == 3 && speler.getHand().size() != 2) {
                                System.out.println("SPELER " + (i + 1) + ": " + (speler.isDoubled() ? "DOUBLED " : "") + "LOSS");
                                if (speler.isGesplitHand()) {
                                    speler.afrekenen(-1);
                                    spelers.get(i - 1).voegGeldToe(speler.getGeld(), speler.getInzet());
                                    spelers.remove(i);
                                    i--;
                                } else {
                                    speler.afrekenen(-1);
                                }

                                if (speler.isDoubled()) {
                                    aantalDoubledLosses++;
                                } else {
                                    aantalLosses++;
                                }
                            } else if (bankHand.size() != 3 && speler.getHand().size() == 2 && !(speler.isGesplitHand() || speler.heeftGesplit())) {
                                System.out.println("SPELER " + (i + 1) + ": BLACKJACK");
                                if (speler.isGesplitHand()) {
                                    speler.afrekenen(1);
                                    spelers.get(i - 1).voegGeldToe(speler.getGeld(), speler.getInzet());
                                    spelers.remove(i);
                                    i--;
                                } else {
                                    speler.afrekenen(blackjackPayoff);
                                }

                                aantalBlackjacks++;
                            } else {
                                System.out.println("SPELER " + (i + 1) + ": " + (speler.isDoubled() ? "DOUBLED " : "") + "TIE");
                                if (speler.isGesplitHand()) {
                                    speler.afrekenen(0);
                                    spelers.get(i - 1).voegGeldToe(speler.getGeld(), speler.getInzet());
                                    spelers.remove(i);
                                    i--;
                                } else {
                                    speler.afrekenen(0);
                                }

                                if (speler.isDoubled()) {
                                    aantalDoubledTies++;
                                } else {
                                    aantalTies++;
                                }
                            }
                        } else {
                            System.out.println("SPELER " + (i + 1) + ": " + (speler.isDoubled() ? "DOUBLED " : "") + "TIE");
                            if (speler.isGesplitHand()) {
                                speler.afrekenen(0);
                                spelers.get(i - 1).voegGeldToe(speler.getGeld(), speler.getInzet());
                                spelers.remove(i);
                                i--;
                            } else {
                                speler.afrekenen(0);
                            }

                            if (speler.isDoubled()) {
                                aantalDoubledTies++;
                            } else {
                                aantalTies++;
                            }
                        }

                    } else if (spelerTotaal == 21 && speler.getHand().size() == 2 && !(speler.isGesplitHand() || speler.heeftGesplit())) {
                        System.out.println("SPELER " + (i + 1) + ": BLACKJACK");
                        if (speler.isGesplitHand()) {
                            speler.afrekenen(1);
                            spelers.get(i - 1).voegGeldToe(speler.getGeld(), speler.getInzet());
                            spelers.remove(i);
                            i--;
                        } else {
                            speler.afrekenen(blackjackPayoff);
                        }

                        aantalBlackjacks++;
                    } else if (spelerTotaal > totaal && spelerTotaal <= 21) {
                        System.out.println("SPELER " + (i + 1) + ": " + (speler.isDoubled() ? "DOUBLED " : "") + "WIN");
                        if (speler.isGesplitHand()) {
                            speler.afrekenen(1);
                            spelers.get(i - 1).voegGeldToe(speler.getGeld(), speler.getInzet());
                            spelers.remove(i);
                            i--;
                        } else {
                            speler.afrekenen(1);
                        }

                        if (speler.isDoubled()) {
                            aantalDoubledWins++;
                        } else {
                            aantalWins++;
                        }

                    } else if (spelerTotaal < totaal) {
                        System.out.println("SPELER " + (i + 1) + ": " + (speler.isDoubled() ? "DOUBLED " : "") + "LOSS");
                        if (speler.isGesplitHand()) {
                            speler.afrekenen(-1);
                            spelers.get(i - 1).voegGeldToe(speler.getGeld(), speler.getInzet());
                            spelers.remove(i);
                            i--;
                        } else {
                            speler.afrekenen(-1);
                        }

                        if (speler.isDoubled()) {
                            aantalDoubledLosses++;
                        } else {
                            aantalLosses++;
                        }
                    }
                }
            }
        }

        System.out.println("OPBRENGST: " + spelers.get(0).getGeld() + "\n");


        return spelers.get(0).getGeld();
//        System.out.printf("%-18s%d\n%-18s%d\n\n", "Totaal ingezet: ", spelers.get(0).getIngezetGeld(),
//                "Totaal gewonnen: ", spelers.get(0).getGewonnenGeld());
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
                spelers.get(i).setHeeftGesplit(true);
                spelers.get(i).addKaart(pack.remove(0));
                spelers.get(i + 1).addKaart(pack.remove(0));
                spelers.get(i + 1).setInzet(spelers.get(i).getInzet());

                if (spelers.get(i).getHand().get(0).getValue() != 1) {
                    speelBeurtSpeler(i);
                }
                break;
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

    public String toonWinst() {

        int gewonnen = spelers.get(0).getGewonnen();
        int verloren = spelers.get(0).getVerloren();

        System.out.println("");
        System.out.println("Totaal blackjacks: " + aantalBlackjacks);
        System.out.println("Totaal wins: " + aantalWins);
        System.out.println("Totaal doubled wins: " + aantalDoubledWins);
        System.out.println("Totaal busts: " + aantalBusts);
        System.out.println("Totaal doubled busts: " + aantalDoubledBusts);
        System.out.println("Totaal losses: " + aantalLosses);
        System.out.println("Totaal doubled losses: " + aantalDoubledLosses);
        System.out.println("Totaal ties: " + aantalTies);
        System.out.println("Totaal doubled ties: " + aantalDoubledTies);
        System.out.println("Totaal surrenders: " + aantalSurrenders + "\n");

        System.out.println("Gewonnen geld: " + gewonnen);
        System.out.println("Verloren geld: " + verloren);

        double percent = ((((1.0) * spelers.get(0).getGewonnenGeld()) - spelers.get(0).getIngezetGeld()) / spelers.get(0).getIngezetGeld());
        double gewonnenVerloren = (1 - (1.0 * gewonnen) / verloren) * (-100);

        System.out.println("Winst %: " + gewonnenVerloren);

        String output = String.format("Totaal blackjacks: %d\nTotaal wins: %d\nTotaal doubled wins: %d\nTotaal busts: %d\n"
                + "Totaal doubled busts: %d\nTotaal losses: %d\nTotaal doubled losses: %d\nTotaal ties: %d\nTotaal doubled ties: %d\nTotaal surrenders: %d\n"
                + "Gewonnen geld: %d\nVerloren geld: %d\nWinst in %s: %.3f", aantalBlackjacks, aantalWins, aantalDoubledWins, aantalBusts, aantalDoubledBusts, aantalLosses,
                aantalDoubledLosses, aantalTies, aantalDoubledTies, aantalSurrenders, gewonnen, verloren, "%", gewonnenVerloren);

        return output;
    }
}
