# Implementation Plan - Dialektická Multi-Agentní Aréna s Konsenzem v Mission Control Kokpitu

Integrace autonomní **Dialektické multi-agentní arény s konsenzem** přímo do hlavního panelu **Mission Control Kokpitu** (`OmnisCockpitView.kt`) v kognitivním systému O.M.N.I.S. v4.2. Umožňuje operátorům i běžným uživatelům přímo z kokpitu spouštět a sledovat argumentační duely mezi specializovanými agentními archetypy, automaticky syntetizovat protichůdné pohledy na systémová rozhodnutí, měřit divergenci/konsensus a aplikovat ověřená doporučení.

---

## Uživatelské rozhodnutí (Zadání)

> [!IMPORTANT]
> **Vybraná specifikace z upřesňujícího dotazování:**
> - **Funkcionalita**: Dialektická multi-agentní aréna s konsenzem.
> - **Umístění**: Přímo v panelu Mission Control Kokpitu (`OmnisCockpitView.kt`).

---

## 1. Přehled architektury & Kognitivní principy

1. **Multi-Agentní Dialektický Model**:
   - Využívá knihovnu kognitivních archetypů (`AgentArchetype`):
     - **Konzervativní Analytik**: Normativní garance, NIS2/ISO shoda, striktní minimalizace rizik ($\text{Riziko} = 0.15$).
     - **Radikální Inovátor**: Maximální akcelerace, tenzorový výkon a prolomení limitů ($\text{Riziko} = 0.88$).
     - **Skeptik & Auditor**: Detekce entropie, kaskádových trhlin a černých labutí ($\text{Rigor} = 0.98$).
     - **Pragmatický Inženýr**: Exekuční stabilita, CSR matice a ZK-SNARK audity ($\text{Rigor} = 0.90$).
     - **Eko-Systémový Etik**: Společenská soudržnost a dlouhodobá udržitelnost.
2. **Třífázový argumentační protokol**:
   - **Kolo 1 (Teze & Antiteze)**: Agent A představí primární tezi, Agent B formuluje protitezi a systémová rizika.
   - **Kolo 2 (Křížové námitky & Stresový test)**: Detailní konfrontace v kritických 8D doménách (např. SYS vs LAW, SEC vs ECON).
   - **Kolo 3 (Syntéza & Konsensuální konvergence)**: Formulace výsledného kompromisu a akčních doporučení.
3. **Matematické metriky konsenzu & Divergence**:
   - **Index divergence**: $D = \frac{1}{N} \sum_{i=1}^N |s_{A, i} - s_{B, i}|$
   - **Index konsenzu**: $C = 1.0 - D$ (při $C \ge 0.70$ je konsenzus označen jako „ROBUSTNÍ“, při $C < 0.70$ jako „DIVERGENTNÍ / POLARIZOVANÝ“).
   - **Vážený tenzorový kompromis**: $S_i = \frac{s_{A,i} \cdot w_A + s_{B,i} \cdot w_B}{w_A + w_B}$ (kde váhy $w$ reflektují epistemickou přísnost archetypů).
   - **ZK-SNARK kryptografický otisk konsenzu**: SHA-256 hash svazující identitu agentů, výrok a syntetizovaná data.

---

## 2. Návrh Uživatelského Rozhraní v Mission Control Kokpitu

### 2.1 Nový interaktivní modul v `OmnisCockpitView.kt`:
- **Panel Mission Control Arény** (`cockpit_multiagent_arena_card`):
  - Umístěn v logickém toku Kokpitu (mezi 8D Maticí / Cíli a Telemetrií).
  - **Kompaktní & Rozbalovací režim**:
    - Ve sbaleném stavu: Přehled posledního dosaženého konsenzu, rychlé tlačítko *„Spustit kognitivní duel“* a indikátory dvou vybraných agentů.
    - V rozbaleném/aktivním stavu: Plně interaktivní aréna přímo v těle Kokpitu.
  - **Rychlý výběr duelantů**:
    - Možnost přepínat agenty A a B (barevné odznaky, role a ikony).
    - Rychlé předvolby: *„Inovace vs. Bezpečnost“*, *„Rychlost vs. Statutární audit“*, *„Etika vs. Ekonomika“*.
  - **Kontextové zadání problému**:
    - Automatické tlačítko: *„Vyřešit nejslabší 8D dimenzi (${lowestDim.first})“*.
    - Volba řešení aktivního cíle z Mission Control.
    - Volné pole pro zadání vlastního dilematu.
  - **Živý vizualizér debaty**:
    - Průběh kol v reálném čase s animovanými argumentačními bublinami.
    - Zobrazení aktuálně namáhaných 8D domén (SYS, SEC, LAW, ...).
    - Měřič konsenzu s barevným gradientem (zelená při konvergenci, jantarová při tření).
  - **Výstupní syntéza & Akce**:
    - Akční doporučení s možností:
      - *„Odeslat syntézu do Chatu“* (vloží konsensuální plán k okamžité exekuci).
      - *„Otevřít plnou Arénu (Detail)“* (přepnutí do dedikované záložky `OmnisTab.DIALECTICS`).

### 2.2 Rozšíření `QuickActionStrip`:
- Doplnění tlačítka **„Aréna“** s ikonou `CompareArrows` přímo do vodorovného pruhu rychlých akcí v horní části Kokpitu pro okamžitý přechod na dialektiku.

---

## 3. Plán implementace po souborech

### Krok 1: Vytvoření komponenty `CockpitAgentArenaCard.kt`
- **Cesta**: `app/src/main/java/com/example/ui/cockpit/CockpitAgentArenaCard.kt`
- **Odpovědnost**:
  - Samostatná, modulární a vysoce responzivní komponenta pro Mission Control.
  - Vlastní interní stav pro probíhající debatu, rychlý výběr agentů a zobrazení výsledků.
  - Integrovaný haptický feedback, testovací tagy (`cockpit_arena_start_button`, `cockpit_arena_synthesize_button`) a animované přechody.
  - Podpora jak pro standardní uživatele (srozumitelné lidské shrnutí), tak pro expertní uživatele (8D rozptyl, divergence a ZK otisk).

### Krok 2: Začlenění do `OmnisCockpitView.kt`
- **Cesta**: `app/src/main/java/com/example/ui/cockpit/OmnisCockpitView.kt`
- **Úpravy**:
  - Přidání `SectionHeader("DIALEKTICKÁ ARÉNA & KONSENZUS", Icons.AutoMirrored.Filled.CompareArrows)` a vložení `CockpitAgentArenaCard`.
  - Předání kontextu: `lowestDim`, `activeGoals`, `onSendToChat`, `onTabSwitch`.
  - Aktualizace `QuickActionStrip` o položku `OmnisTab.DIALECTICS` ("Aréna").

### Krok 3: Synchronizace s `AgentArenaEngine.kt`
- **Cesta**: `app/src/main/java/com/example/dialectics/AgentArenaEngine.kt`
- **Úpravy**:
  - Ověření, že `debateHistoryState` a spouštění přes `executeArenaDebate` je bezpečné pro souběžné volání z Kokpitu i ze záložky Dialectics.
  - Podpora generování doporučení vázaných na konkrétní zadanou 8D dimenzi.

---

## 4. Ověření a testování

1. **Kompilace a statická kontrola**:
   - Spuštění `compile_applet` pro ověření 100% čisté kompilace v Kotlin DSL.
2. **Funkční kontrola UI**:
   - Ověření existence a klikatelnosti `testTag("cockpit_multiagent_arena_card")`.
   - Kontrola, že spuštění debaty z Mission Control plynule zobrazuje kola bez zamrzání hlavního vlákna (spuštěno v coroutine).
   - Kontrola exportu výsledků do Chatu přes `onSendToChat`.
