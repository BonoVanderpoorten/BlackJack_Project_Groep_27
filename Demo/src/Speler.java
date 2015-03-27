
import java.util.ArrayList;
import java.util.List;


public class Speler {

    private List<Kaart> hand;
    private int geld;
    private GenericStrategy strategie;
    private int inzet;
    private boolean surrendered;
    
    //wanneer een speler split, wordt een nieuwe "Speler" aangemaakt 
    //voor zijn nieuw hand met TRUE op deze boolean
    private boolean gesplitHand;
    
    public Speler(int geld, GenericStrategy strategie) {
        hand = new ArrayList();
        this.geld = geld;
        this.strategie = strategie;
        surrendered = false;
        gesplitHand = false;
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
        setSurrendered(false);
        setGesplitHand(false);
        setHand(new ArrayList());
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

    public int getGeld() {
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
    
    public void dubbel() {
        inzet *= 2;
    }
    
    public void surrender() {
        inzet /= 2;
        setSurrendered(true);
    }
    
    public int getInzet() {
        return inzet;
    }
    
    public void afrekenen(double payoff) {
        geld += inzet*payoff;
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

    public void voegGeldToe(int geld) {
        this.geld += geld;
    }
    
}
