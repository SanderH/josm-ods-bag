package org.openstreetmap.josm.plugins.ods.bag.modifiers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.josm.plugins.ods.bag.entity.NL_Address;
import org.openstreetmap.josm.plugins.ods.bag.entity.NL_HouseNumber;
import org.openstreetmap.josm.plugins.ods.bag.entity.NL_HouseNumberImpl;
import org.openstreetmap.josm.plugins.ods.entities.EntityModifier;

/**
 * <p>
 * The BAG database was built on the assumption that all house numbers in the Netherlands consisted of a numeric value,
 * followed by an optional letter, followed by an optional alphanumeric extension. This assumption was wrong. A number
 * of localities have house numbers that follow these conventions with one notable exception: they start with a letter.
 * </p>
 * <p>
 * Because the BAG encodes the required house number field as an integer, a leading letter is not possible. To work
 * around this limitation, the BAG engineers conceived of a hack that appended that letter to the street name instead.
 * On a printed address label, this doesn't matter too much (house number <u>underlined</u> for emphasis):
 * </p>
 * <dl>
 *     <dt>Local usage (signage etc.)</dt>
 *     <dd>Dominee Sicco Tjadenstraat <u>C52</u></dd>
 *     <dt>BAG-hack</dt>
 *     <dd>Dominee Sicco Tjadenstraat C <u>52</u></dd>
 * </dl>
 * <p>
 * The downside of this hack is that consumers of BAG data now have streets named &quot;Dominee Sicco Tjadenstraat
 * C&quot, where in reality the street sign says &quot;Dominee Sicco Tjadenstraat&quot;; and house numbers that lack
 * their leading letter, whereas on the houses themselves the sign includes it.
 * </p>
 * <p>
 * OpenStreetMap has no such limitation, so this class reverses this hack for known postcodes. The list of postcodes
 * can be extended, but care should be taken to include only those postcodes that contain addresses on streets that are
 * applicable. It is fine to include postcodes that contain addresses on streets that do not end with a letter as
 * well as those that do, but bear in mind that not every street that ends in a letter should be passed to this class
 * (e.g., &quot;Hoofdstraat W&quot; in Winsum is literally called this on local signage).
 * </p>
 * 
 * <p>
 * @author Jeroen Hoek
 * @author gertjan
 *
 */


public class NL_PrefixedHouseNumberAddressModifier implements EntityModifier<NL_Address> {
    static final Set<String> applicablePostcodes4;
    static final Set<String> applicablePostcodes6;

    static {
        // Add larger postcode areas by leading number here.
        applicablePostcodes4 = new HashSet<>(Arrays.asList(

                // Pekela; het Pekelder ABC.
                "9663"
        ));

        // More localised postcode areas can be added here.
       applicablePostcodes6 = new HashSet<>(Arrays.asList(

                // Zinkweg, Nieuw-Beijerland. Drie adressen in Zinkweg liggen net in een andere
                // gemeente. Zij hebben de voorloopletter 'N' (voor Nieuw-Beijerland). Het bedrijf dat
                // op Zinkweg N4 zit gebruikt de voorloopletter ook letterlijk zo op bebording en website.
                "3264LK",

                // Langbroekerdijk, Langbroek. Voorloopletter (A of B) staat op de
                // huisnummerbordjes.
                "3947BA",
                "3947BB",
                "3947BC",
                "3947BD",
                "3947BE",
                "3947BG",
                "3947BH",
                "3947BJ",
                "3947BK",


                // Graafjansdijk, Westdorpe (Terneuzen). Voorloopletter (A of B) staat op de
                // huisnummerbordjes.
                "4554AE",
                "4554AG",
                "4554AH",
                "4554AJ",
                "4554AK",
                "4554AL",
                "4554AM",
                "4554CA",
                "4554CB",
                "4554CC",
                "4554CD",
                "4554LA",
                "4554LB",
                "4554LC",
                "4554LD",
                "4554LE",
                "4554LG",


                // Deken van Erpstraat en Meierij, Sint-Oedenrode. Flats uit 2007 in bestaande straten.
                // Voorloopletters zijn gebruikt om adresruimte te creëren in
                "5492DE",
                "5492DG",
                "5492DH",
                "5492DJ",
                "5492DK",
                "5492DL",


                // Adelbert van Scharnlaan, Maastricht. Een lange straat met flats met voorloopletter.
                // (A t/m S). Boven de portieken van de flats staan de huisnummers die daar bij horen,
                // met voorloopletter. Straatnaamborden spreken ook enkel van Adelbert van Scharnlaan
                // (geen letter erachter).
                "6226EC",
                "6226ED",
                "6226EE",
                "6226EG",
                "6226EH",
                "6226EJ",
                "6226EK",
                "6226EL",
                "6226EM",
                "6226EN",
                "6226EP",
                "6226ER",
                "6226ES",
                "6226ET",
                "6226EV",
                "6226EW",
                "6226EX",
                "6226EZ",
                "6226HT",
                "6226HV",
                "6226HW",
                "6227CE",
                "6227CG",
                "6227CH",
                "6227VE",
                "6227VG",
                "6227VH",


                // Wethouder Vrankenstraat, Maastricht. Deze straat heeft naast gewone huisnummers
                // één flat met voorloopletters aan het begin van de straat.
                "6227CE",
                "6227CG",
                "6227CH",


                // Burgemeester Kessensingel, Maastricht. Deze straat heeft naast gewone huisnummers
                // drie flats ('A', 'B', 'C') met voorloopletters. Op de straatnaamborden bestaat
                // alleen de Burgemeester Kessensingel zonder toevoeging.
                "6227VE",
                "6227VG",
                "6227VH",


                // Averbergen, Olst. Dit zijn (verzorgings)flats waar de vleugels een eigen letter
                // hebben. De straat heet gewoon Averbergen.
                "8121CD",
                "8121CE",
                "8121CG",
                "8121CH",
                "8121CJ",


                // Recreatiewoningen aan de Beukenlaan in Oudemirdum.
                // Huisnummers beginnen allen met 'Z' (valt op de huisnummerplaatjes te zien).
                "8567HE",
                "8567HG",


                // Nieuwebildtzijl. Adressen hebben gehuchtnaam als 'straatnaam', maar een paar adressen
                // (deze postcode) liggen net in een andere gemeente (Nieuwebildtzijl ligt in de Waadhoeke).
                // Nu is dat Noardeast-Frsylân, maar dat was Ferwerderadiel. Vandaar de 'F' die deze
                // huisnummers als voorloopletter hebben (valt op de huisnummerplaatjes te zien).
                "9074PA",


                // Bartlehiem. Adressen hebben gehuchtnaam als 'straatnaam', maar een paar adressen
                // (deze postcode) liggen net in een andere gemeente (de rest van Bartlehiem ligt in
                // Tytsjerksteradiel). Nu is dat Noardeast-Frsylân, maar dat was Ferwerderadiel. Vandaar
                // de 'F' die deze huisnummers als voorloopletter hebben (valt op de huisnummerplaatjes te zien).
                "9178GH",


                // Recreatiepark Oosterduinen in Norg.
                // Alle adressen hebben Oosterduinen (het terrein) als 'straatnaam'. Paden tussen de huisjes hebben
                // eigen namen die niet in de adressen terugkeren.
                // Zie ook: https://oosterduinen.nl/
                "9331WB",
                "9331WC",
                "9331WD",
                "9331WE",
                "9331WG",
                "9331WH",
                "9331WK",


                // Amerika, Een (Drenthe). Recreatiewoningen met een voorloopletter om binnen de adresruimte
                // te passen.
                "9342TN",


                // Fivelkade Appingedam. De woonboten aan de kade hebben een voorloopletter 'W'. De huizen
                // aan dezelfde straat niet. Zonder voorloopletter botsen de nummers.
                "9901GE"
        ));

        // False positives: these streets look like they might be used to hide a voorloopletter, but
        // are in fact valid street names. They are documented here to prevent contributors from wasting
        // time hunting down these streets ending in a letter.

        /*
            2064KA
                Zijkanaal C, Spaarndam. Vernoemd naar naastgelegen kanaal dat zo heet.
            5521E*
                Lindehof N, M, B, Z in Eersel. Letter hoort bij straatnaam.
            6225CH
                In de O, Maastricht. Straat heet gewoon zo.
            7739PX
                Kolonieweg O. Staat zo op straatnaambordjes. Vroeger zal deze weg in de naastgelegen gemeente
                verder hebben gelopen, maar die lijkt hem hernoemd te hebben naar 'De Kolonie'.
            8011VS
                Kleine A, Zwolle. Letter hoort bij straatnaam.
            9951A*
                Winsum, Hoofdstraat O en Hoofdstraat W. O en W staan voor Obergum en Winsum, respectievelijk.
            9711HV
                Kleine der A, Groningen, de A is een gracht in Groningen.
            9712A*
                Hoge der A, Groningen, de A is een gracht in Groningen.
            9718B*
                Lage der A, Groningen, de A is een gracht in Groningen.
         */    }

    @Override
    public Class<NL_Address> getTargetType() {
        return NL_Address.class;
    }

    @Override
    public void modify(NL_Address address) {
        String street = address.getStreetName();
        String prefix = street.substring(street.length() -1);
        address.setStreetName(street.substring(0, street.length() - 2));
        modifyHouseNumber(address, prefix);
    }
    
    private void modifyHouseNumber(NL_Address address, String prefix) {
        NL_HouseNumber hnr = address.getHouseNumber();
        address.setHouseNumber(new NL_HouseNumberImpl(
                prefix, hnr.getHouseNumber(), hnr.getHouseLetter(),
                hnr.getHouseNumberExtra()));
    }

    @Override
    public boolean isApplicable(NL_Address address) {
        String postcode6 = address.getPostcode();
        if (postcode6 == null) return false;

        // The 1234 part of 1234AB.
        String postcode4 = postcode6.substring(0, 4);

        if (applicablePostcodes4.contains(postcode4) || applicablePostcodes6.contains(postcode6)) {
            return address.getStreetName() != null &&
                    // Ignore addresses on streets that do not end in a single capital letter.
                    address.getStreetName().matches(".+ [A-Z]") &&
                    address.getHouseNumber() != null;
        }
        return false;
    }
}
