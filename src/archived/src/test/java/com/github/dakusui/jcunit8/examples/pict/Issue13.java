package com.github.dakusui.jcunit8.examples.pict;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.helpers.ParameterUtils;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;

import java.util.Objects;

/**
 * <a href="https://github.com/Microsoft/pict/issues/11">Issue-11 of PICT</a>
 * <code>
 * [0 2017/08/08T04:23:14JST hiroshi@alexios ~/Documents/pict]
 * $ ./pict issue-13.pict
 * ^C
 * [130 2017/08/08T07:02:50JST hiroshi@alexios ~/Documents/pict]
 * $ cat issue-13.pict
 * Eingangskanal: EVA_Anlageberatung, EVA_Order, EVA_Sonderweg, EVA_Sparplan, EVA_Neuemission, EVA_Direkteinstieg, HOST_T19000, HOST_T19001, HOST_T19901, HOST_T19750, HOST_T28900, Onlinebanking_PC, Onlinebanking_MSB, Onlinebanking_Neuemission_PC, Onlinebanking_Neuemission_MSB, Onlinebanking_Tablet_PC, Infobroker, commerzbank_de_pib
 * Finanzinstrument: Aktie, Unstrukturierte_Anleihe, strukturierte_Anleihe, Inv.Fonds, OIF, Zertifikat, Optionsschein, Xetra_Gold_ETC, EMISID
 * Prod
 * </code>
 * <p>
 * JCUnit is at least slightly better at handling constraints than pict.
 * pict cannot finish generating test suite from the same model in 2.5 hours but
 * JCUnit does in less than 2 hours.
 */
@SuppressWarnings("SimplifiableIfStatement")
@RunWith(JCUnit8.class)
public class Issue13 {
  /**
   * Eingangskanal:
   * <p>
   * EVA_Anlageberatung, EVA_Order, EVA_Sonderweg, EVA_Sparplan, EVA_Neuemission,
   * EVA_Direkteinstieg, HOST_T19000, HOST_T19001, HOST_T19901, HOST_T19750,
   * HOST_T28900, Onlinebanking_PC, Onlinebanking_MSB, Onlinebanking_Neuemission_PC,
   * Onlinebanking_Neuemission_MSB, Onlinebanking_Tablet_PC, Infobroker, commerzbank_de_pib
   */
  @ParameterSource
  public Parameter.Factory<String> eingangskanal() {
    return ParameterUtils.simple(
        "EVA_Anlageberatung", "EVA_Order", "EVA_Sonderweg", "EVA_Sparplan", "EVA_Neuemission",
        "EVA_Direkteinstieg", "HOST_T19000", "HOST_T19001", "HOST_T19901", "HOST_T19750",
        "HOST_T28900", "Onlinebanking_PC", "Onlinebanking_MSB", "Onlinebanking_Neuemission_PC",
        "Onlinebanking_Neuemission_MSB", "Onlinebanking_Tablet_PC", "Infobroker", "commerzbank_de_pib"
    );
  }

  /**
   * Finanzinstrument:
   * <p>
   * Aktie, Unstrukturierte_Anleihe, strukturierte_Anleihe, Inv.Fonds, OIF,
   * Zertifikat, Optionsschein, Xetra_Gold_ETC, EMISID
   */
  @ParameterSource
  public Parameter.Factory<String> finanzinstrument() {
    return ParameterUtils.simple(
        "Aktie", "Unstrukturierte_Anleihe", "strukturierte_Anleihe", "Inv.Fonds", "OIF",
        "Zertifikat", "Optionsschein", "Xetra_Gold_ETC", "EMISID"
    );
  }

  /**
   * Produktzyklus:
   * <p>
   * Neuemission_Information, Neuemission_offen, Neuemission_geschlossen, Neuemission_abgerechnet,
   * Sekundaermarkt, n/a
   */
  @ParameterSource
  public Parameter.Factory<String> produktzyklus() {
    return ParameterUtils.simple(
        "Neuemission_Information", "Neuemission_offen", "Neuemission_geschlossen", "Neuemission_abgerechnet",
        "Sekundaermarkt", "n/a"
    );
  }

  /**
   * Dienstleistungsart: Anlageberatung, beratungsfreies_Geschaeft, n/a
   */
  @ParameterSource
  public Parameter.Factory<String> dienstleistungsart() {
    return ParameterUtils.simple(
        "Anlageberatung", "beratungsfreies_Geschaeft", "n/a"
    );
  }

  /**
   * Orderart:
   * Kauf(Beratungsdatum_gueltig), Kauf_(ohne_Beratungsdatum), Verkauf,
   * aenderung, Streichung, Storno, Berichtigungsauftrag, n/a
   */
  @ParameterSource
  public Parameter.Factory<String> orderart() {
    return ParameterUtils.simple(
        "Kauf(Beratungsdatum_gueltig)", "Kauf_(ohne_Beratungsdatum)", "Verkauf",
        "aenderung", "Streichung", "Storno", "Berichtigungsauftrag", "n/a"
    );
  }

  /**
   * Initiator: Kunde, Bank, n/a
   */
  @ParameterSource
  public Parameter.Factory<String> initiator() {
    return ParameterUtils.simple(
        "Kunde", "Bank", "n/a"
    );
  }

  /**
   * Auftragserteilung: telefonisch, persoenlich, schriftlich, Haustuergeschaeft, n/a
   */
  @ParameterSource
  public Parameter.Factory<String> auftragserteilung() {
    return ParameterUtils.simple(
        "telefonisch", "persoenlich", "schriftlich", "Haustuergeschaeft", "n/a"
    );
  }

  /**
   * Bereitstellungsdokumente:
   * <p>
   * Beratungsprotokoll_Kunde, Beratungsprotokoll_Interessent, Beratungsprotokoll_Potenzial,
   * Nachtraeglicher_KID-Versand, n/a
   */
  @ParameterSource
  public Parameter.Factory<String> bereitstellungsdokumente() {
    return ParameterUtils.simple(
        "Beratungsprotokoll_Kunde", "Beratungsprotokoll_Interessent", "Beratungsprotokoll_Potenzial",
        "Nachtraeglicher_KID-Versand", "n/a"
    );
  }

  /**
   * UDAL-Status:
   * <p>
   * Normalbetrieb, Back_up-Betrieb, PRIIP,ohne_KID, WKN_nicht_vorhanden, WKN_inaktiv/geloescht,
   * defektes_Dokument, Virus, non-PRIIP_auf_Blackliste, PRIIP_auf_Blackliste, UDAL_nicht_verfuegbar,
   * n/a
   */
  @ParameterSource
  public Parameter.Factory<String> udalStatus() {
    return ParameterUtils.simple(
        "Normalbetrieb", "Back_up-Betrieb", "PRIIP", "ohne_KID", "WKN_nicht_vorhanden",
        "WKN_inaktiv/geloescht", "defektes_Dokument", "Virus", "non-PRIIP_auf_Blackliste",
        "PRIIP_auf_Blackliste", "UDAL_nicht_verfuegbar", "n/a"
    );
  }

  /**
   * Abruf_Infoblaetter:
   * <p>
   * WA_von_FWW, PIB_von_DOTi, PIB_von_C&M-FIC, PIB_von_PC_PM,
   * KID_von_PC_PM, KID_von_DOTi, KID_von_externem_Hersteller
   */
  @ParameterSource
  public Parameter.Factory<String> abruf_Infoblaetter() {
    return ParameterUtils.simple(
        "WA_von_FWW", "PIB_von_DOTi", "PIB_von_C&M-FIC", "PIB_von_PC_PM",
        "KID_von_PC_PM", "KID_von_DOTi", "KID_von_externem_Hersteller"
    );
  }

  ////
  //1
  //
  //IF [Eingangskanal] = "HOST_T19901"
  //THEN [Orderart] <> "Kauf_(Beratungsdatum_gueltig)"
  //AND [Produktzyklus] <> "Neuemission_offen"
  //AND [Produktzyklus] <> "n/a"
  //AND [Dienstleistungsart] <> "n/a"
  //AND [Orderart] <> "n/a"
  //AND [Initiator] = "n/a"
  //AND [Auftragserteilung] = "n/a"
  //AND [Bereitstellungsdokumente]= "n/a"
  //AND [UDAL-Status] = "n/a";
  @Condition(constraint = true)
  public boolean constraint1(
      @From("eingangskanal") String eingangskanal,
      @From("orderart") String orderart,
      @From("produktzyklus") String produktzyklus,
      @From("dienstleistungsart") String dienstleistungsart,
      @From("initiator") String initiator,
      @From("auftragserteilung") String auftragserteilung,
      @From("bereitstellungsdokumente") String bereitstellungsdokumente,
      @From("udalStatus") String udalStatus
  ) {
    //noinspection SimplifiableIfStatement
    if (Objects.equals(eingangskanal, "HOST_T19901"))
      return (
          !Objects.equals(orderart, "Kauf_(Beratungsdatum_gueltig)") &&
              !Objects.equals(produktzyklus, "Neuemission_offen") &&
              !Objects.equals(produktzyklus, "n/a") &&
              !Objects.equals(dienstleistungsart, "n/a") &&
              !Objects.equals(orderart, "n/a") &&
              Objects.equals(initiator, "n/a") &&
              Objects.equals(auftragserteilung, "n/a") &&
              Objects.equals(bereitstellungsdokumente, "n/a") &&
              Objects.equals(udalStatus, "n/a")
      );
    return true;
  }

  /*/
  ////2
  //
  //IF [Eingangskanal] in {"HOST_T19000", "HOST_T19001", "HOST_T19750", "HOST_T28900"}
  //THEN [Produktzyklus] <> "Neuemission_Information"
  //AND [Produktzyklus] <> "Neuemission_offen"
  //AND [Produktzyklus] <> "n/a"
  //AND [Dienstleistungsart] <> "n/a"
  //AND [Orderart] <> "n/a"
  //AND [Initiator] = "n/a"
  //AND [Auftragserteilung] = "n/a"
  //AND [Bereitstellungsdokumente]= "n/a"
  //AND [UDAL-Status] = "n/a";
  @Condition(constraint = true)
  public boolean constraint2(
      @From("eingangskanal") String eingangskanal,
      @From("orderart") String orderart,
      @From("produktzyklus") String produktzyklus,
      @From("dienstleistungsart") String dienstleistungsart,
      @From("initiator") String initiator,
      @From("auftragserteilung") String auftragserteilung,
      @From("bereitstellungsdokumente") String bereitstellungsdokumente,
      @From("udalStatus") String udalStatus
  ) {
    if (asList("HOST_T19000", "HOST_T19001", "HOST_T19750", "HOST_T28900").contains(eingangskanal)) {
      return !produktzyklus.equals("Neuemission_Information") &&
          !produktzyklus.equals("Neuemission_offen") &&
          !produktzyklus.equals("n/a") &&
          !dienstleistungsart.equals("n/a") &&
          !orderart.equals("n/a") &&
          initiator.equals("n/a") &&
          auftragserteilung.equals("n/a") &&
          bereitstellungsdokumente.equals("n/a") &&
          udalStatus.equals("n/a");
    }
    return true;
  }

  ////3
  //
  //IF [Eingangskanal] like "EVA_*"
  //THEN [Initiator] <> "n/a"
  //AND [Produktzyklus] <> "n/a"
  //AND [Dienstleistungsart] <> "n/a"
  //AND [Orderart] <> "n/a"
  //AND [Auftragserteilung] <> "n/a"
  //AND [Bereitstellungsdokumente] <> "n/a"
  //AND [UDAL-Status] <> "n/a";
  @Condition(constraint = true)
  public boolean constraint3(
      @From("eingangskanal") String eingangskanal,
      @From("orderart") String orderart,
      @From("produktzyklus") String produktzyklus,
      @From("initiator") String initiator,
      @From("auftragserteilung") String auftragserteilung,
      @From("bereitstellungsdokumente") String bereitstellungsdokumente,
      @From("udalStatus") String udalStatus
  ) {
    if (eingangskanal.startsWith("EVA_"))
      return !initiator.equals("n/a") &&
          !produktzyklus.equals("n/a") &&
          !orderart.equals("n/a") &&
          !auftragserteilung.equals("n/a") &&
          !bereitstellungsdokumente.equals("n/a") &&
          !udalStatus.equals("n/a");
    return true;
  }

  ////4
  //
  //IF [Eingangskanal] IN {"Onlinebanking_PC", "Onlinebanking_MSB", "Onlinebanking_Tablet_PC"}
  //THEN [Produktzyklus] <> "Neuemission_Information"
  //AND [Produktzyklus] <> "Neuemission_offen"
  //AND [Produktzyklus] <> "n/a"
  //AND [Dienstleistungsart] <> "n/a"
  //AND [Orderart] <> "n/a";
  @Condition(constraint = true)
  public boolean constraint4(
      @From("eingangskanal") String eingangskanal,
      @From("orderart") String orderart,
      @From("produktzyklus") String produktzyklus,
      @From("dienstleistungsart") String dienstleistungsart
  ) {
    if (asList("Onlinebanking_PC", "Onlinebanking_MSB", "Onlinebanking_Tablet_PC").contains(eingangskanal))
      return !produktzyklus.equals("Neuemission_Information") &&
          !produktzyklus.equals("Neuemission_offen") &&
          !produktzyklus.equals("n/a") &&
          !dienstleistungsart.equals("n/a") &&
          !orderart.equals("n/a");
    return true;
  }

  ////5
  //
  //IF [Eingangskanal] like "Onlinebanking*"
  //THEN [Dienstleistungsart]="beratungsfreies_Geschaeft"
  //AND [Produktzyklus] <> "n/a"
  //AND [Dienstleistungsart] <> "n/a"
  //AND [Orderart] <> "Storno"
  //AND [Orderart] <> "Berichtigungsauftrag"
  //AND [Orderart] <> "Kauf_(Beratungsdatum_gueltig)"
  //AND [Orderart] <> "n/a"
  //AND [Initiator] = "n/a"
  //AND [Auftragserteilung] = "n/a"
  //AND [Bereitstellungsdokumente] = "n/a"
  //AND [UDAL-Status] <>"Back_up-Betrieb";
  @Condition(constraint = true)
  public boolean constraint5(
      @From("eingangskanal") String eingangskanal,
      @From("orderart") String orderart,
      @From("produktzyklus") String produktzyklus,
      @From("dienstleistungsart") String dienstleistungsart,
      @From("initiator") String initiator,
      @From("auftragserteilung") String auftragserteilung,
      @From("bereitstellungsdokumente") String bereitstellungsdokumente,
      @From("udalStatus") String udalStatus
  ) {
    if (eingangskanal.startsWith("Onlinebanking"))
      // !dienstleistungsart.equals("n/a") is always true.
      //noinspection ConstantConditions
      return dienstleistungsart.equals("beratungsfreies_Geschaeft") &&
          !produktzyklus.equals("n/a") &&
          !dienstleistungsart.equals("n/a") &&
          !orderart.equals("Storno") &&
          !orderart.equals("Berichtigungsauftrag") &&
          !orderart.equals("Kauf_(Beratungsdatum_gueltig)") &&
          !orderart.equals("n/a") &&
          initiator.equals("n/a") &&
          auftragserteilung.equals("n/a") &&
          bereitstellungsdokumente.equals("n/a") &&
          !udalStatus.equals("Back_up-Betrieb");
    return true;
  }

  ////6
  //IF [Eingangskanal] IN {"EVA_Sonderweg", "EVA_Sparplan", "EVA_Direkteinstieg", "EVA Order"}
  //THEN [Produktzyklus] <>"Neuemission_Information"
  //AND [Produktzyklus] <>"Neuemission_offen"
  //AND [Produktzyklus] <>"n/a"
  //AND [Dienstleistungsart] <>"n/a"
  //AND [Orderart] <>"n/a";
  @Condition(constraint = true)
  public boolean constraint6(
      @From("eingangskanal") String eingangskanal,
      @From("orderart") String orderart,
      @From("produktzyklus") String produktzyklus,
      @From("dienstleistungsart") String dienstleistungsart
  ) {
    if (asList("EVA_Sonderweg", "EVA_Sparplan", "EVA_Direkteinstieg", "EVA Order").contains(eingangskanal))
      return !produktzyklus.equals("Neuemission_Information") &&
          !produktzyklus.equals("Neuemission_offen") &&
          !produktzyklus.equals("n/a") &&
          !dienstleistungsart.equals("n/a") &&
          !orderart.equals("n/a");
    return true;
  }

  //7
  //IF [Eingangskanal] IN {"EVA_Sonderweg", "EVA_Sparplan", "EVA_Direkteinstieg"}
  //THEN [Orderart] <>"Storno"
  //AND [Orderart] <>"Berichtigungsauftrag"
  //AND [Orderart] <>"Kauf_(Beratungsdatum_gueltig)"
  //AND [Produktzyklus] <>"n/a"
  //AND [Dienstleistungsart] <>"n/a"
  //AND [Orderart] <>"n/a";
  @Condition(constraint = true)
  public boolean constraint7(
      @From("eingangskanal") String eingangskanal,
      @From("orderart") String orderart,
      @From("produktzyklus") String produktzyklus,
      @From("dienstleistungsart") String dienstleistungsart
  ) {
    if (asList("EVA_Sonderweg", "EVA_Sparplan", "EVA_Direkteinstieg").contains(eingangskanal))
      return !orderart.equals("Storno") &&
          !orderart.equals("Berichtigungsauftrag") &&
          !orderart.equals("Kauf_(Beratungsdatum_gueltig)") &&
          !produktzyklus.equals("n/a") &&
          !dienstleistungsart.equals("n/a") &&
          !orderart.equals("n/a");
    return true;
  }

  //8
  //IF [Eingangskanal] in {"EVA_Anlageberatung", "EVA_Neuemission", "EVA_Order"}
  //THEN [Orderart] <>"Storno"
  //AND [Orderart] <>"Berichtigungsauftrag"
  //AND [Produktzyklus] <>"n/a"
  //AND [Dienstleistungsart] <>"n/a"
  //AND [Orderart] <>"n/a";
  @Condition(constraint = true)
  public boolean constraint8(
      @From("eingangskanal") String eingangskanal,
      @From("orderart") String orderart,
      @From("produktzyklus") String produktzyklus,
      @From("dienstleistungsart") String dienstleistungsart
  ) {
    if (asList("EVA_Anlageberatung", "EVA_Neuemission", "EVA_Order").contains(eingangskanal))
      return !orderart.equals("Storno") &&
          !orderart.equals("Berichtigungsauftrag") &&
          !produktzyklus.equals("n/a") &&
          !dienstleistungsart.equals("n/a") &&
          !orderart.equals("n/a");
    return true;
  }

  //9
  //
  //IF [Eingangskanal] IN {"Infobroker", "commerzbank_de_pib"}
  //THEN [Finanzinstrument] <> "EMISID"
  //AND [Produktzyklus] = "n/a"
  //AND [Dienstleistungsart] = "n/a"
  //AND [Orderart] = "n/a"
  //AND [Initiator] = "n/a"
  //AND [Auftragserteilung] = "n/a"
  //AND [Bereitstellungsdokumente]= "n/a"
  //AND [UDAL-Status] <> "Back_up-Betrieb";
  @Condition(constraint = true)
  public boolean constraint9(
      @From("eingangskanal") String eingangskanal,
      @From("finanzinstrument") String finanzinstrument,
      @From("orderart") String orderart,
      @From("produktzyklus") String produktzyklus,
      @From("dienstleistungsart") String dienstleistungsart,
      @From("initiator") String initiator,
      @From("auftragserteilung") String auftragserteilung,
      @From("bereitstellungsdokumente") String bereitstellungsdokumente,
      @From("udalStatus") String udalStatus
  ) {
    if (asList("Infobroker", "commerzbank_de_pib").contains(eingangskanal))
      return !finanzinstrument.equals("EMISID") &&
          produktzyklus.equals("n/a") &&
          dienstleistungsart.equals("n/a") &&
          orderart.equals("n/a") &&
          initiator.equals("n/a") &&
          auftragserteilung.equals("n/a") &&
          bereitstellungsdokumente.equals("n/a") &&
          !udalStatus.equals("Back_up-Betrieb");
    return true;
  }
  /*/

  @Test
  public void test(
      @From("eingangskanal") String eingangskanal,
      @From("finanzinstrument") String finanzinstrument,
      @From("orderart") String orderart,
      @From("produktzyklus") String produktzyklus,
      @From("dienstleistungsart") String dienstleistungsart,
      @From("initiator") String initiator,
      @From("auftragserteilung") String auftragserteilung,
      @From("bereitstellungsdokumente") String bereitstellungsdokumente,
      @From("udalStatus") String udalStatus,
      @From("abruf_Infoblaetter") String abruf_Infoblaetter
  ) {
    System.out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
        eingangskanal,
        finanzinstrument,
        orderart,
        produktzyklus,
        dienstleistungsart,
        initiator,
        auftragserteilung,
        bereitstellungsdokumente,
        udalStatus,
        abruf_Infoblaetter
    );
  }

  public static void main(String... args) {
    long before = System.currentTimeMillis();
    new JUnitCore().run(Issue13.class);
    System.out.println(System.currentTimeMillis() - before);
  }
}
