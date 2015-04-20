
import com.sun.jndi.ldap.Ber;
import java.util.ArrayList;
import java.util.List;

public class GenericStrategy {

    private Simulatie simulatie;
    boolean reedsGesplit = false;

    public GenericStrategy(Simulatie simulatie) {
        this.simulatie = simulatie;
    }

    public String speelRonde(List<Kaart> lijst) {
        int bankKaart = simulatie.eersteBankKaartWaarde;

        

        List<Integer> waarden = new ArrayList();
        for (Kaart kaart : lijst) {
            waarden.add(simulatie.berekenKaartWaarde(kaart.getValue()));
        }

        //<editor-fold defaultstate="collapsed" desc="AAS CHECK">
        //////////////////////////////////////////////////////////////////////////
        //1 VERANDEREN IN 11, INDIEN DIT HAND BOVEN 21 DOET GAAN, ONGEDAAN MAKEN//
        //////////////////////////////////////////////////////////////////////////
        int hand = 0;
        for (int waarde : waarden) {
            hand += waarde;
        }

        boolean aasAlsElf = false;
        for (int waarde : waarden) {
            if (waarde == 1 && !aasAlsElf) {
                aasAlsElf = true;
                hand += 10;
            }
        }

        if (aasAlsElf && hand > 21) {
            aasAlsElf = false;
            hand -= 10;
        }
//</editor-fold>

        //spreekt voor zichzelf
        if (hand >= 21) {
            return "stand";
        }
        //einde spreekt voor zichzelf

        //<editor-fold defaultstate="collapsed" desc="SPLIT CHECK">
        if (waarden.size() == 2 && (simulatie.resplits || !reedsGesplit)) {
            boolean splitten = false;
            int kaart1 = waarden.get(0), kaart2 = waarden.get(1);
            if (kaart1 == kaart2) {
                if (kaart1 == 1 || kaart1 == 8) {
                    splitten = true;
                }

                if (kaart1 == 2 || kaart1 == 3) {
                    if (simulatie.allowedDAS) {
                        if (bankKaart >= 2 && bankKaart <= 7) {
                            splitten = true;
                        }
                    } else {
                        if (bankKaart >= 4 && bankKaart <= 7) {
                            splitten = true;
                        }
                    }
                }

                if (kaart1 == 4 && simulatie.allowedDAS && bankKaart >= 5 && bankKaart <= 6) {
                    splitten = true;
                }

                if (kaart1 == 6) {
                    if (simulatie.allowedDAS) {
                        if (bankKaart >= 2 && bankKaart <= 6) {
                            splitten = true;
                        }
                    } else {
                        if (bankKaart >= 3 && bankKaart <= 6) {
                            splitten = true;
                        }
                    }
                }

                if (kaart1 == 7) {
                    if (simulatie.allowedDAS) {
                        if (bankKaart >= 2 && bankKaart <= 7) {
                            splitten = true;
                        }
                    } else {
                        if (bankKaart >= 2 && bankKaart <= 7) {
                            splitten = true;
                        }
                    }
                }

                if (kaart1 == 9) {
                    if (simulatie.allowedDAS) {
                        if (bankKaart >= 2 && bankKaart <= 9 && bankKaart != 7) {
                            splitten = true;
                        }
                    } else {
                        if (bankKaart >= 2 && bankKaart <= 9 && bankKaart != 7) {
                            splitten = true;
                        }
                    }
                }
            }

            if (splitten) {
                reedsGesplit = true;
                return "split";
            }
        }
//</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="SURRENDER CHECK">
        if (simulatie.surrender && waarden.size() == 2) {
            if (hand == 15 && bankKaart == 10) {
                return "surrender";
            }
            if (hand == 16
                    && (bankKaart == 10
                    || bankKaart == 9
                    || bankKaart == 1)) {
                return "surrender";
            }
        }
//</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="STAND CHECK">
        if (hand >= 13 && (bankKaart == 2 || bankKaart == 3)) {
            return "stand";
        }

        if (hand >= 12 && bankKaart >= 4 && bankKaart <= 6) {
            return "stand";
        }

        if (hand >= 17 && (bankKaart == 1 || (bankKaart >= 7 && bankKaart <= 10))) {
            return "stand";
        }

        if (aasAlsElf) {
            if (hand >= 18 && (bankKaart == 2 || bankKaart == 7 || bankKaart == 8)) {
                return "stand";
            }

            if (hand >= 18 && waarden.size() >= 3 && bankKaart >= 3 && bankKaart <= 6) {
                return "stand";
            }

            if (hand >= 19 && (bankKaart == 9 || bankKaart == 10 || bankKaart == 1)) {
                return "stand";
            }
        }

//</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="DOUBLE CHECK">
        if (waarden.size() == 2 && (simulatie.allowedDAS || !reedsGesplit)) {
            if (hand == 11 && bankKaart >= 2 && bankKaart <= 10) {
                return "double";
            }

            if (hand == 10 && bankKaart >= 2 && bankKaart <= 9) {
                return "double";
            }

            if (hand == 9 || (hand == 17 && aasAlsElf) || (hand == 18 && aasAlsElf)) {
                if (bankKaart >= 3 && bankKaart <= 6) {
                    return "double";
                }
            }

            if (aasAlsElf) {
                if (hand == 15 || hand == 16) {
                    if (bankKaart >= 4 && bankKaart <= 6) {
                        return "double";
                    }
                }

                if (hand == 13 || hand == 14) {
                    if (bankKaart == 5 || bankKaart == 6) {
                        return "double";
                    }
                }
            }
        }

//</editor-fold>
        return "hit";
    }
}
