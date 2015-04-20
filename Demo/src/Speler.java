
import java.util.ArrayList;
import java.util.List;


public class Speler {

    private List<Kaart> hand;
    private double geld;
    private int ingezetGeld, gewonnenGeld, verloren, gewonnen;
    private GenericStrategy strategie;
    private int inzet;
    private boolean surrendered;
    private boolean doubled = false;
    
    //wanneer een speler split, wordt een nieuwe "Speler" aangemaakt 
    //voor zijn nieuw hand met TRUE op deze boolean
    private boolean gesplitHand;
    private boolean heeftGesplit;
    
    public Speler(double geld, GenericStrategy strategie) {
        hand = new ArrayList();
        this.geld = geld;
        this.strategie = strategie;
        surrendered = false;
        gesplitHand = false;
        ingezetGeld = 0;
        gewonnenGeld = 0;
        verloren = 0; gewonnen = 0;
        heeftGesplit = false;
    }
    
    public int bepaalTotaalHand() {

        List<Integer> waarden = new ArrayList();
        for (Kaart kaart : hand) {
            waarden.add(berekenKaartWaarde(kaart.getValue()));
        }
        
        int totaal = 0;
        for (int waarde : waarden) {
            totaal += waarde;
        }

        boolean aasAlsElf = false;
        for (int waarde : waarden) {
            if (waarde == 1 && !aasAlsElf) {
                aasAlsElf = true;
                totaal += 10;
            }
        }

        if (aasAlsElf && totaal > 21) {
            aasAlsElf = false;
            totaal -= 10;
        }
        
        return totaal;
    }
    
    public void reset() {
        setGeld(0);
        setSurrendered(false);
        setDoubled(false);
        setGesplitHand(false);
        setHand(new ArrayList());
        setHeeftGesplit(false);
    }
    
    public int berekenKaartWaarde(int kaart) {
        if (kaart <= 10) {
            return kaart;
        }
        return 10;
    }

    public List<Kaart> getHand() {
        return hand;
    }

    public void setHand(List<Kaart> handen) {
        this.hand = handen;
    }
    
    public void addKaart(Kaart kaart) {
        hand.add(kaart);
    }

    public double getGeld() {
        return geld;
    }

    public void setGeld(int geld) {
        this.geld = geld;
    }

    public GenericStrategy getStrategie() {
        return strategie;
    }

    public void setStrategie(GenericStrategy strategie) {
        this.strategie = strategie;
    }
    
    public void setInzet(int inzet) {
        this.inzet = inzet;
    }

    public int getIngezetGeld() {
        return ingezetGeld;
    }

    public void setIngezetGeld(int ingezetGeld) {
        this.ingezetGeld = ingezetGeld;
    }

    public int getGewonnenGeld() {
        return gewonnenGeld;
    }

    public void setGewonnenGeld(int gewonnenGeld) {
        this.gewonnenGeld = gewonnenGeld;
    }

    public boolean isDoubled() {
        return doubled;
    }

    public void setDoubled(boolean doubled) {
        this.doubled = doubled;
    }
    
    
    public void dubbel() {
        inzet *= 2;
        doubled = true;
    }
    
    public void surrender() {
        inzet /= 2;
        setSurrendered(true);
    }
    
    public int getInzet() {
        return inzet;
    }
    
    public void afrekenen(double payoff) {
        double beloning = inzet * payoff;
        
        double verschil = beloning;
        if ((-1)*this.geld < beloning) {
            verschil = (-1) * this.geld;
        }
        
        if (this.geld > 0 && beloning < 0) {
            gewonnen += verschil;
            verloren += verschil;
        }
        if (this.geld < 0 && beloning > 0) {
            gewonnen -= verschil;
            verloren -= verschil;
        }
        
        geld += beloning;
        ingezetGeld += inzet;
        if (payoff >= 0) {
            gewonnenGeld += beloning + inzet;
        }
        
        
        if (payoff > 0) {
            gewonnen += beloning;
        } else {
            verloren -= beloning;
        }
            
    }

    public boolean isSurrendered() {
        return surrendered;
    }

    public void setSurrendered(boolean surrendered) {
        this.surrendered = surrendered;
    }

    public boolean isGesplitHand() {
        return gesplitHand;
    }

    public void setGesplitHand(boolean gesplitHand) {
        this.gesplitHand = gesplitHand;
    }

    public boolean heeftGesplit() {
        return heeftGesplit;
    }

    public void setHeeftGesplit(boolean heeftGesplit) {
        this.heeftGesplit = heeftGesplit;
    }
    
    public int getVerloren() {
        return verloren;
    }

    public void setVerloren(int verloren) {
        this.verloren = verloren;
    }

    public int getGewonnen() {
        return gewonnen;
    }

    public void setGewonnen(int gewonnen) {
        this.gewonnen = gewonnen;
    }

    
    
    
    public void voegGeldToe(double geld, double inzet) {
        double verschil = geld;
        double positiefGeld = this.geld >= 0 ? this.geld : (-1)*this.geld;
        double positiefBinnenkomendGeld = geld >= 0 ? geld : (-1)*geld;
        
        if (positiefGeld < positiefBinnenkomendGeld) {
            verschil = (-1) * this.geld;
        }
        
        if (this.geld > 0 && geld < 0) {
            gewonnen += verschil;
            verloren += verschil;
        }
        if (this.geld < 0 && geld > 0) {
            gewonnen -= verschil;
            verloren -= verschil;
        }
        
        this.geld += geld;
        ingezetGeld += inzet;
        if (geld >= 0) {
            gewonnenGeld += geld + inzet;
        }
        
        if (geld > 0) {
            gewonnen += geld;
        } else {
            verloren -= geld;
        }
    }
    
}
