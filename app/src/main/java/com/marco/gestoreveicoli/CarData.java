package com.marco.gestoreveicoli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Elenco statico di marche e modelli per i menu a tendina. */
public class CarData {

    private static final Map<String, String[]> DATA = new LinkedHashMap<>();

    static {
        DATA.put("Abarth", new String[]{"500", "595", "695", "124 Spider", "Punto Evo"});
        DATA.put("Alfa Romeo", new String[]{"MiTo", "Giulietta", "Giulia", "Stelvio", "Tonale", "Junior", "147", "156", "159", "GT", "Brera", "Spider"});
        DATA.put("Audi", new String[]{"A1", "A3", "A4", "A5", "A6", "A7", "A8", "Q2", "Q3", "Q5", "Q7", "Q8", "TT", "e-tron"});
        DATA.put("BMW", new String[]{"Serie 1", "Serie 2", "Serie 3", "Serie 4", "Serie 5", "Serie 7", "X1", "X2", "X3", "X5", "X6", "Z4", "i3", "iX"});
        DATA.put("Citroën", new String[]{"C1", "C2", "C3", "C3 Aircross", "C4", "C4 Cactus", "C5", "C5 Aircross", "Berlingo", "Xsara", "Saxo"});
        DATA.put("Cupra", new String[]{"Formentor", "Leon", "Ateca", "Born", "Terramar"});
        DATA.put("Dacia", new String[]{"Sandero", "Duster", "Logan", "Jogger", "Spring", "Lodgy", "Dokker"});
        DATA.put("DS", new String[]{"DS 3", "DS 4", "DS 5", "DS 7", "DS 9"});
        DATA.put("Ferrari", new String[]{"296", "F8 Tributo", "Roma", "Portofino", "SF90", "812", "488", "458", "California", "Purosangue"});
        DATA.put("Fiat", new String[]{"500", "500L", "500X", "600", "Panda", "Punto", "Grande Punto", "Punto Evo", "Tipo", "Bravo", "Stilo", "Croma", "Doblò", "Qubo", "Freemont", "Multipla", "Uno", "Seicento", "Cinquecento", "Barchetta"});
        DATA.put("Ford", new String[]{"Fiesta", "Focus", "Puma", "Kuga", "EcoSport", "Mondeo", "C-Max", "S-Max", "Galaxy", "Ka", "Mustang", "Ranger", "Transit"});
        DATA.put("Honda", new String[]{"Jazz", "Civic", "CR-V", "HR-V", "e:Ny1", "Accord"});
        DATA.put("Hyundai", new String[]{"i10", "i20", "i30", "Kona", "Tucson", "Santa Fe", "Bayon", "Ioniq", "Ioniq 5", "ix20", "ix35"});
        DATA.put("Jeep", new String[]{"Renegade", "Compass", "Avenger", "Cherokee", "Grand Cherokee", "Wrangler", "Gladiator"});
        DATA.put("Kia", new String[]{"Picanto", "Rio", "Ceed", "Stonic", "Sportage", "Niro", "Sorento", "EV6", "Soul", "Venga"});
        DATA.put("Lamborghini", new String[]{"Huracán", "Urus", "Aventador", "Revuelto", "Gallardo"});
        DATA.put("Lancia", new String[]{"Ypsilon", "Delta", "Musa", "Thema", "Thesis", "Lybra", "Fulvia"});
        DATA.put("Land Rover", new String[]{"Defender", "Discovery", "Discovery Sport", "Range Rover", "Range Rover Sport", "Range Rover Evoque", "Range Rover Velar", "Freelander"});
        DATA.put("Maserati", new String[]{"Ghibli", "Levante", "Quattroporte", "Grecale", "GranTurismo", "MC20"});
        DATA.put("Mazda", new String[]{"Mazda2", "Mazda3", "Mazda6", "CX-3", "CX-30", "CX-5", "CX-60", "MX-5", "MX-30"});
        DATA.put("Mercedes-Benz", new String[]{"Classe A", "Classe B", "Classe C", "Classe E", "Classe S", "CLA", "GLA", "GLB", "GLC", "GLE", "SL", "SLK", "Vito", "EQA", "EQB"});
        DATA.put("MG", new String[]{"MG3", "MG4", "ZS", "HS", "Marvel R"});
        DATA.put("Mini", new String[]{"Cooper", "One", "Countryman", "Clubman", "Cabrio", "Paceman", "Aceman"});
        DATA.put("Mitsubishi", new String[]{"Space Star", "ASX", "Eclipse Cross", "Outlander", "L200", "Pajero"});
        DATA.put("Nissan", new String[]{"Micra", "Juke", "Qashqai", "X-Trail", "Leaf", "Note", "Navara", "Ariya", "350Z", "GT-R"});
        DATA.put("Opel", new String[]{"Corsa", "Astra", "Mokka", "Crossland", "Grandland", "Insignia", "Meriva", "Zafira", "Adam", "Karl", "Agila", "Vectra", "Tigra"});
        DATA.put("Peugeot", new String[]{"106", "107", "108", "206", "207", "208", "2008", "306", "307", "308", "3008", "406", "407", "408", "5008", "508", "Partner", "Rifter"});
        DATA.put("Porsche", new String[]{"911", "718 Boxster", "718 Cayman", "Panamera", "Macan", "Cayenne", "Taycan"});
        DATA.put("Renault", new String[]{"Twingo", "Clio", "Captur", "Mégane", "Scénic", "Kadjar", "Austral", "Arkana", "Espace", "Laguna", "Modus", "Kangoo", "Zoe", "5 E-Tech"});
        DATA.put("Seat", new String[]{"Ibiza", "Leon", "Arona", "Ateca", "Tarraco", "Alhambra", "Altea", "Toledo", "Mii"});
        DATA.put("Skoda", new String[]{"Fabia", "Octavia", "Superb", "Kamiq", "Karoq", "Kodiaq", "Scala", "Enyaq", "Citigo", "Yeti", "Rapid"});
        DATA.put("Smart", new String[]{"ForTwo", "ForFour", "Roadster", "#1", "#3"});
        DATA.put("Subaru", new String[]{"Impreza", "Forester", "Outback", "XV", "Crosstrek", "Solterra", "Legacy"});
        DATA.put("Suzuki", new String[]{"Swift", "Ignis", "Vitara", "S-Cross", "Jimny", "Celerio", "Baleno", "Splash", "Alto"});
        DATA.put("Tesla", new String[]{"Model 3", "Model S", "Model X", "Model Y", "Cybertruck"});
        DATA.put("Toyota", new String[]{"Aygo", "Aygo X", "Yaris", "Yaris Cross", "Corolla", "C-HR", "RAV4", "Auris", "Avensis", "Prius", "Land Cruiser", "Hilux", "Supra", "bZ4X"});
        DATA.put("Volkswagen", new String[]{"up!", "Polo", "Golf", "Passat", "T-Cross", "T-Roc", "Tiguan", "Touran", "Touareg", "Arteon", "Scirocco", "Beetle", "Caddy", "ID.3", "ID.4", "ID.5"});
        DATA.put("Volvo", new String[]{"V40", "V50", "V60", "V70", "V90", "S60", "S90", "XC40", "XC60", "XC90", "C30", "C40", "EX30"});
        DATA.put("Altro", new String[]{});
    }

    public static List<String> marche() {
        return new ArrayList<>(DATA.keySet());
    }

    public static List<String> modelli(String marca) {
        List<String> out = new ArrayList<>();
        if (marca != null) {
            for (Map.Entry<String, String[]> e : DATA.entrySet()) {
                if (e.getKey().equalsIgnoreCase(marca.trim())) {
                    for (String m : e.getValue()) {
                        out.add(m);
                    }
                    return out;
                }
            }
            // marca non in elenco: nessun suggerimento specifico
        }
        return out;
    }
}
