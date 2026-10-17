# 03 - Validare și Activity Result API

## Obiective

- să înțelegeți diferența dintre a **porni** o Activity și a **aștepta un rezultat** de la ea;
- să validați datele din formular înainte de a le accepta;
- să cunoașteți **Activity Result API** (varianta actuală) și API-ul vechi (`startActivityForResult` / `onActivityResult`);
- să transmiteți modelul prin Intent cu **`Serializable` sau `Parcelable`** și să folosiți `setResult` / `finish`.

## De unde porniți

- Starea de la finalul Seminarului 2 (tag `Seminar_2-grupa1086` sau `Seminar_2-grupa1088`, ori branch-ul grupei: `git pull` sau **Clone Repository** din Android Studio - vezi [README](../README.md)).
- Seminarul 2 finalizat (model și `AddActivity`).

## 1. Problema: formularul trebuie să întoarcă date

În Seminarul 2, `AddActivity` construia un `Anunt` sau un `Abonament` și îl afișa în Log sau Toast. Ecranul principal nu „primea” obiectul înapoi. În practică, fluxul este:

1. `MainActivity` deschide `AddActivity` și **așteaptă** un rezultat;
2. utilizatorul completează formularul și apasă Salvează sau Anulează;
3. `AddActivity` închide ecranul și transmite un cod de rezultat (`RESULT_OK` sau `RESULT_CANCELED`) plus, opțional, date în Intent;
4. `MainActivity` reacționează doar dacă rezultatul este pozitiv.

Acest tipar se numește _start activity for result_. Implementarea modernă este **Activity Result API**.

## 2. Așteptarea rezultatului: API vechi și API actual

Ambele variante pornesc aceeași idee: `MainActivity` deschide `AddActivity` și reacționează când aceasta se închide. Diferența este **unde** și **cum** primiți rezultatul.

La oră folosim API-ul actual. La examen sau la probele practice puteți folosi și API-ul vechi: este deprecated, dar rămâne valid din punct de vedere didactic. Alegeți **una** dintre variante și țineți-o consistent în proiect.

### API vechi (`startActivityForResult` / `onActivityResult`)

Totul de mai jos stă în **`MainActivity`**: acolo apelați `startActivityForResult` (de exemplu din click-ul pe FAB) și tot acolo faceți **override** la `onActivityResult`. `AddActivity` nu suprascrie `onActivityResult`; ea doar întoarce rezultatul cu `setResult` și `finish` (secțiunea 4).

Definiți un **cod de cerere** (`REQUEST_CODE`), ca să deosebiți mai multe fluxuri care se întorc în aceeași metodă:

```java
// în MainActivity
private static final int REQUEST_ADD = 1;

// la click pe FAB:
Intent intent = new Intent(this, AddActivity.class);
startActivityForResult(intent, REQUEST_ADD);

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode != REQUEST_ADD) {
        return;
    }
    if (resultCode == RESULT_OK && data != null) {
        // extrageți obiectul din extras (vezi secțiunea 3)
    }
}
```

Dacă mai târziu deschideți și un ecran de editare, adăugați un al doilea `REQUEST_...` și încă un `if` în aceeași metodă din `MainActivity`. De aici vine confuzia tipică a API-ului vechi: **toate** rezultatele trec prin `onActivityResult`.

### API actual (Activity Result API)

Și aici declarați launcher-ul în **`MainActivity`**. Fiecare flux (adaugă, eventual editează mai târziu) are un launcher propriu, cu contract și callback dedicat. Înainte de a lipi codul, merită despachetat fiecare piesă.

#### Ce este un callback

În PAW, la WinForms (.NET Framework 4.8), un buton nu aștepta într-un `while` să fie apăsat. Scriați ceva de forma `button1.Click += button1_Click;` (sau din Properties → Events). **Delegatul** / handler-ul era metoda pe care framework-ul o apela **mai târziu**, când evenimentul avea loc. Voi nu apelați rezultatul; sistemul vă **chema înapoi** (_call back_) când evenimentul avea loc.

Pe Android, ideea e aceeași. După ce `AddActivity` se închide, nu rămâneți blocați în `launch(...)`. Platforma termină ecranul secundar, apoi apelează codul pe care l-ați înregistrat: acesta este **callback-ul**. În Java modern apare adesea ca **lambda**: `result -> { ... }`, echivalentul unui handler scurt, fără o metodă separată cu nume.

#### `ActivityResultLauncher<Intent>`

`ActivityResultLauncher` este obiectul prin care **porniți** fluxul care așteaptă un rezultat. Câmpul `addLauncher` îl țineți în `MainActivity` și, la click pe FAB, apelați `addLauncher.launch(...)`.

Parametrul generic `<Intent>` spune ce tip de valoare îi dați la `launch`. Pentru „deschide o Activity și așteaptă rezultat”, intrarea este un `Intent` (cine e ținta: `AddActivity`). De aceea tipul este `ActivityResultLauncher<Intent>`, nu un launcher „gol”. Alte contracte pot cere alt tip de intrare (de exemplu un `Uri`); tipul din `<>` urmează contractul.

#### `registerForActivityResult(...)`

Metoda aparține Activity-ului (sau Fragment-ului). Face două lucruri:

1. leagă acest flux de ciclul de viață al `MainActivity` (sistemul știe cui să îi livreze rezultatul chiar dacă ecranul a fost recreat);
2. vă întoarce launcher-ul pe care îl veți folosi la `launch`.

Primul argument este **contractul**, al doilea este **callback-ul**. Echivalent aproximativ în WinForms: „abonează-te la evenimentul X cu handler-ul Y”, plus faptul că aici primiți și un obiect (`addLauncher`) cu care declanșați operația.

#### `ActivityResultContracts` și `StartActivityForResult`

`ActivityResultContracts` este o colecție de **contracte** gata făcute: tipare standard „cerere → răspuns” pe care le înțelege platforma (deschidere Activity, alegere imagine din galerie etc.).

`new ActivityResultContracts.StartActivityForResult()` este contractul: „pornesc o Activity cu un `Intent` și primesc înapoi un `ActivityResult`” (cod `RESULT_OK` / `RESULT_CANCELED` plus, opțional, un `Intent` cu extras). Îl folosiți aici pentru că exact asta faceți cu `AddActivity`. Fără acest contract, `registerForActivityResult` nu ar ști ce tip de intrare așteaptă `launch` și ce tip de rezultat ajunge în callback.

#### De ce e nevoie de callback și ce face

`launch` doar **pornește** `AddActivity`. Rezultatul nu e disponibil în linia următoare: utilizatorul încă completează formularul. Callback-ul rulează **când** ecranul secundar s-a închis și sistemul are un rezultat. În el:

- verificați `result.getResultCode()` (`RESULT_OK` vs `RESULT_CANCELED`);
- citiți `result.getData()` (Intent-ul cu extras, dacă există);
- actualizați UI-ul din `MainActivity` (Toast acum; lista într-un seminar viitor).

Dacă nu ați înregistra un callback, ați deschide formularul, dar `MainActivity` nu ar ști ce s-a întors.

#### Codul la un loc

```java
// în MainActivity
private final ActivityResultLauncher<Intent> addLauncher =
        registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // extrageți obiectul din extras (vezi secțiunea 3)
                    }
                });

// la click pe FAB:
addLauncher.launch(new Intent(this, AddActivity.class));
```

Pe scurt: `registerForActivityResult` = abonare (contract + handler); `addLauncher` = cum porniți cererea; `launch(intent)` = echivalentul declanșării; lambda = ce faceți când vine răspunsul.

|                   | API vechi                             | API actual                    |
| ----------------- | ------------------------------------- | ----------------------------- |
| Pornire           | `startActivityForResult(intent, cod)` | `launcher.launch(intent)`     |
| Răspuns           | `onActivityResult(...)` (comun)       | callback-ul legat de launcher |
| Identificare flux | `requestCode`                         | un launcher separat pe flux   |
| Status în SDK     | **deprecated**                        | recomandat                    |

Documentație: [Get a result from an activity](https://developer.android.com/training/basics/intents/result)

**Important:** înregistrați launcher-ul ca **câmp al Activity-ului**, la inițializarea clasei sau în `onCreate`, înainte ca Activity-ul să treacă în starea STARTED. Nu creați launcher-ul în interiorul unui `onClick`: platforma poate opri aplicația. În WinForms ați fi putut uneori atașa handler-ul târziu; aici înregistrarea e legată de ciclul de viață Android și trebuie făcută din timp.

## 3. Serializable și Parcelable

Intent-ul transportă date prin _extras_. Tipurile simple (`String`, `int`, `boolean`) se pun direct. Un obiect propriu (`Anunt` / `Abonament`) **nu** poate trece așa: trebuie serializat prin `Serializable` sau `Parcelable`. În acest seminar alegeți **una** dintre cele două pe clasa model. Nu transmiteți câmpurile pe rând ca primitive: scopul este să exersați serializarea obiectului întreg.

|              | `Serializable`                               | `Parcelable`                                                     |
| ------------ | -------------------------------------------- | ---------------------------------------------------------------- |
| Origine      | interfață Java (`java.io`)                   | interfață Android (`android.os`)                                 |
| Efort        | de regulă o linie: `implements Serializable` | metode `writeToParcel`, constructor din `Parcel`, câmp `CREATOR` |
| Pe Android   | funcționează; overhead mai mare (reflecție)  | proiectat pentru Binder; de obicei mai eficient                  |
| În laborator | acceptat                                     | acceptat                                                         |

Exemplele de mai jos sunt pe `Anunt` (1086). La 1088 urmați același tipar pe `Abonament`, cu câmpurile grupei.

### Varianta `Serializable`

```java
package ro.ase.semdam.domeniu;

import java.io.Serializable;
import java.time.LocalDate;

public class Anunt implements Serializable {
    private String titlu;
    private String descriere;
    private double pret;
    private int cantitate;
    private Categorie categorie;
    private boolean nou;
    private LocalDate dataPublicare;

    // constructor, getter/setter, toString - ca în Seminarul 2
}
```

`Categorie` (enum) este serializabilă implicit. Nu este nevoie de metode suplimentare pe `Anunt`.

### Varianta `Parcelable`

La `Serializable`, Java „se descurcă” singur (prin reflecție) să transforme obiectul în octeți. La `Parcelable`, **voi** spuneți sistemului cum se scrie și cum se citește obiectul într-un `Parcel`: un buffer de date primitiv pe care Android îl folosește eficient pe canalul Binder. De aici cele trei piese obligatorii din interfață (plus constructorul care citește din `Parcel`).

#### `writeToParcel(Parcel dest, int flags)`

Este „serializarea” voastră manuală. Când `MainActivity` pune anunțul în Intent (`putExtra`), sistemul apelează această metodă și umple `dest` cu valorile câmpurilor: `writeString`, `writeDouble`, `writeInt` etc.

Ordinea contează: ce scrieți aici trebuie citit **în aceeași ordine** în constructorul `Anunt(Parcel in)`. Dacă inversați două câmpuri, citiți aiurea (sau primiți o excepție), deoarece fiecare câmp are un anumit offset. `LocalDate` și enum-ul nu au metode dedicate pe `Parcel`; le treceți ca `String` (ISO, respectiv `name()`). Parametrul `flags` îl lăsați de obicei nefolosit în laborator; îl cere semnătura metodei.

#### `describeContents()`

Spune dacă `Parcel`-ul conține tipuri speciale de descriptor de fișier (`FileDescriptor`). Pentru un model obișnuit (`String`, numere, enum, dată) **nu** e cazul: întoarceți `0`. Metoda există ca parte din contractul `Parcelable`; fără ea clasa nu compilează. În aproape tot ce faceți la seminar, corpul rămâne `return 0;`.

#### Instanța `CREATOR`

Problema la citire: sistemul are un `Parcel` plin cu octeți, dar nu știe singur să apeleze `new Anunt(...)` cu parametrii potriviți. Are nevoie de un obiect **cunoscut după nume**, static, căruia să-i ceară: „din parcelul ăsta, fă-mi un `Anunt`”. Acesta este `public static final Creator<Anunt> CREATOR`.

`Creator` are două metode:

- `createFromParcel(Parcel in)` - construiește **un** obiect (de obicei `return new Anunt(in);`, deci constructorul protected care face `readString` / `readInt` ...);
- `newArray(int size)` - alocă un **vector** `Anunt[size]`, folosit când sistemul deserializază un tablou de obiecte. Chiar dacă voi trimiteți un singur element, interfața cere metoda; implementarea tipică este `return new Anunt[size];`.

Fără `CREATOR`, `putExtra` / `getParcelableExtra` nu au cum să reconstruiască tipul vostru: Android caută exact acest câmp public static pe clasă. Numele `CREATOR` este convenție obligatorie a API-ului, nu un nume la alegere.

#### Codul la un loc

Scrieți câmpurile în `Parcel` **în aceeași ordine** în care le citiți:

```java
package ro.ase.semdam.domeniu;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDate;

public class Anunt implements Parcelable {
    private String titlu;
    private String descriere;
    private double pret;
    private int cantitate;
    private Categorie categorie;
    private boolean nou;
    private LocalDate dataPublicare;

    // constructor, getter/setter, toString - ca în Seminarul 2

    protected Anunt(Parcel in) {
        titlu = in.readString();
        descriere = in.readString();
        pret = in.readDouble();
        cantitate = in.readInt();
        categorie = Categorie.valueOf(in.readString());
        nou = in.readByte() != 0;
        dataPublicare = LocalDate.parse(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(titlu);
        dest.writeString(descriere);
        dest.writeDouble(pret);
        dest.writeInt(cantitate);
        dest.writeString(categorie.name());
        dest.writeByte((byte) (nou ? 1 : 0));
        dest.writeString(dataPublicare.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Anunt> CREATOR = new Creator<Anunt>() {
        @Override
        public Anunt createFromParcel(Parcel in) {
            return new Anunt(in);
        }

        @Override
        public Anunt[] newArray(int size) {
            return new Anunt[size];
        }
    };
}
```

Android Studio poate genera scheletul Parcelable (Alt+Insert / Generate). Verificați totuși ordinea câmpurilor și conversiile pentru enum și `LocalDate`.

### `IntentCompat` și versiunea `appcompat`

Metodele tipizate de pe `Intent` (`getSerializableExtra(String, Class)` etc.) există abia de la API 33. Pe `minSdk 24` folosiți `IntentCompat` din AndroidX: aceleași apeluri, sigure pe dispozitive mai vechi.

`IntentCompat.getSerializableExtra(..., Class)` e disponibil din `androidx.core` **1.13.0**. Nu trebuie să adăugați `core` separat: vine tranzitiv odată cu `appcompat`. Șablonul Empty Views Activity poate avea încă `appcompat = "1.6.1"`, care trage un `core` prea vechi, deci IDE-ul nu găsește metoda.

În `gradle/libs.versions.toml`, la `[versions]`, ridicați `appcompat` la **1.8.0** (sau mai nou), apoi Sync:

```toml
appcompat = "1.8.0"
```

### Scriere și citire în Intent

Aceeași cheie (`"element"`) pe ambele ecrane. Scrierea e neschimbată:

```java
data.putExtra("element", anunt); // Serializable sau Parcelable
```

#### Varianta deprecated (doar ca să o recunoașteți)

În materiale și proiecte vechi apare citirea fără tip:

```java
Anunt anunt = (Anunt) data.getSerializableExtra("element");
Anunt anunt = data.getParcelableExtra("element");
```

Aceste overload-uri (fără `Class`) sunt **deprecated**. Cast-ul `(Anunt)` nu e verificat de compilator: dacă în Intent e alt tip de obiect, aflați abia la rulare (`ClassCastException`). IDE-ul marchează apelul ca învechit tocmai ca să vă împingă spre API-ul tipizat.

#### Varianta actuală în laborator: `IntentCompat`

După Sync:

```java
import androidx.core.content.IntentCompat;

Anunt anunt = IntentCompat.getSerializableExtra(data, "element", Anunt.class);
Anunt anunt = IntentCompat.getParcelableExtra(data, "element", Anunt.class);
```

De ce e mai bună:

- primiți tipul așteptat (`Anunt.class`) fără cast manual;
- dacă extras-ul lipsește sau nu e de tipul cerut, rezultatul este `null`, nu neapărat o excepție de cast;
- pe API 34+ folosește intern overload-ul tipizat al platformei; pe API mai vechi face citirea clasică plus verificarea tipului, deci rulați la fel pe emulatorul cu API 24 și pe telefonul nou;
- evitați atât warning-ul de deprecated, cât și apelul direct la `Intent.getSerializableExtra(String, Class)`, care pe dispozitive sub API 33 nu există la rulare.

În laborator folosim `IntentCompat`. Varianta cu cast o cunoașteți pentru examen și pentru cod vechi, dar nu o folosim ca soluție principală.

Când o Activity trimite un Intent către alta, Android nu „copiază magic” obiectul în memoria comună. Datele din extras trec printr-un canal al sistemului numit **Binder**: un buffer cu dimensiune limitată (ordinul a câtorva sute de KB pe tranzacție, în funcție de versiune). Dacă puneți în Intent ceva prea mare (o listă cu sute de elemente, imagini ca byte array etc.), sistemul poate opri aplicația cu o eroare de tip `TransactionTooLargeException`.

De aceea, prin Intent trimiteți obiecte **mici**: un singur `Anunt` / `Abonament` e în limite. Listele mari și fișierele stau în memorie, pe disc sau într-o bază de date; Intent-ul poate duce doar un identificator sau obiectul tocmai editat.

Documentație:

- [Serializable (Java SE)](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/Serializable.html)
- [Parcelable](https://developer.android.com/reference/android/os/Parcelable)
- [Parcelables and bundles](https://developer.android.com/guide/components/activities/parcelables-and-bundles)
- [IntentCompat](https://developer.android.com/reference/androidx/core/content/IntentCompat)

## 4. Trimiterea rezultatului

### Activitate în `MainActivity`

1. Alegeți API-ul de rezultat din secțiunea 2 (actual sau vechi) și declarați launcher-ul / `REQUEST_ADD` corespunzător.
2. În callback (sau în `onActivityResult`), extrageți obiectul cu `IntentCompat`, după tipul ales pe model. Exemplu cu API actual și `Serializable`:

```java
private final ActivityResultLauncher<Intent> addLauncher =
        registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Anunt anunt = IntentCompat.getSerializableExtra(
                                result.getData(), "element", Anunt.class);
                        if (anunt != null) {
                            Toast.makeText(this, anunt.toString(), Toast.LENGTH_SHORT).show();
                            // lista apare într-un seminar viitor
                        }
                    }
                });
```

3. La deschiderea formularului (FAB): `addLauncher.launch(new Intent(this, AddActivity.class));`, sau `startActivityForResult(...)` dacă ați ales API-ul vechi.

### Activitate în `AddActivity`

1. Pe model (`Anunt` / `Abonament`): implementați **`Serializable` sau `Parcelable`**, conform secțiunii 3. Nu înlocuiți asta cu `putExtra` pe fiecare câmp în parte.
2. La Salvează, după o validare reușită, transmiteți obiectul prin Intent:

```java
Intent data = new Intent();
data.putExtra("element", anunt); // sau abonament
setResult(RESULT_OK, data);
finish();
```

3. Pentru Anulează (sau la renunțare): `setResult(RESULT_CANCELED); finish();`

`finish()` cere închiderea Activity-ului curent. Fără `finish()`, rămâneți pe ecranul de adăugare chiar dacă ați setat rezultatul. Urmăriți în Logcat metodele de lifecycle: la Salvează, `AddActivity` trece prin `onPause` / `onStop` / `onDestroy`, iar `MainActivity` revine prin `onRestart` / `onStart` / `onResume` (în funcție de starea anterioară).

## 5. Validare pe formular

**Validarea** înseamnă a verifica dacă datele introduse sunt acceptabile **înainte** de a le salva sau transmite. Pe Android, `EditText.setError("mesaj")` afișează un indicator vizual lângă câmp. Dacă un câmp este invalid, apelați `setError` pe el și ieșiți din listener cu `return`, astfel încât `setResult(RESULT_OK, ...)` și `finish()` să nu ruleze.

În listener-ul de pe **Salvează** citiți deja valorile și construiți obiectul. Validarea vine imediat după citire (sau odată cu ea) și **înainte** de `setResult` / `finish`.

Validarea pe client nu înlocuiește validarea pe server în aplicații reale, dar este o practică utilă pentru a îmbunătăți experiența utilizatorului.

### Activitate

Exemplu pentru 1086 (titlu și preț). La 1088 folosiți `etNume` / `etCost` și aceleași idei. Completați apoi celelalte câmpuri (live assignment, mai jos).

```java
binding.btnSalveaza.setOnClickListener(v -> {
    String titlu = binding.etTitlu.getText().toString().trim();
    if (titlu.isEmpty()) {
        binding.etTitlu.setError("Titlul este obligatoriu");
        return;
    }

    double pret;
    try {
        pret = Double.parseDouble(binding.etPret.getText().toString().trim());
    } catch (NumberFormatException e) {
        binding.etPret.setError("Introduceți un număr valid");
        return;
    }
    if (pret <= 0) {
        binding.etPret.setError("Prețul trebuie să fie strict pozitiv");
        return;
    }

    // completați validările pentru cantitate, dată, Spinner și RadioGroup

    String descriere = binding.etDescriere.getText().toString().trim();
    int cantitate = Integer.parseInt(binding.etCantitate.getText().toString().trim());
    Categorie categorie = Categorie.dinEticheta(
            binding.spCategorie.getSelectedItem().toString());
    boolean nou = binding.rgStare.getCheckedRadioButtonId() == binding.rbNou.getId();
    LocalDate dataPublicare = LocalDate.parse(
            binding.etDataPublicare.getText().toString().trim(),
            DateTimeFormatter.ISO_LOCAL_DATE);

    Anunt anunt = new Anunt(titlu, descriere, pret, cantitate, categorie, nou, dataPublicare);
    Intent data = new Intent();
    data.putExtra("element", anunt);
    setResult(RESULT_OK, data);
    finish();
});
```

### Live assignment

Primul live assignment din semestru (seminariile cu live assignments: 3, 5, 7 și 11; vezi [README](../README.md)). Completați validarea pentru câmpurile care lipsesc din exemplul de mai sus, pe același tipar (`setError` + `return`). **Cerința exactă (reguli pe câmpuri) se anunță la oră** pe Moodle, separat pe fiecare grupă.

### Evaluare

- **Pondere:** 5% din nota finală (unul din cele patru live assignments; împreună 20%).
- **Mod:** verificare pe calculatorul vostru, la oră, cu un demo propus de mine.
- **Condiție pentru a primi punctaj:** treceți demo-ul (cazuri invalide blochează salvarea; un caz valid întoarce obiectul în `MainActivity`).
- **Fără punctaj intermediar:** live assignment-ul este totul sau nimic. Ori primiți cei 5%, ori 0 pentru acest exercițiu.

## Verificare

- [ ] Salvarea validă transmite **obiectul** către Main (Toast sau Log), nu doar câmpuri separate.
- [ ] Modelul implementează `Serializable` sau `Parcelable` (o variantă, consistent).
- [ ] Anularea nu introduce date noi.
- [ ] Live assignment (anunțat la oră): validările pe câmpurile rămase; fără `RESULT_OK` la date invalide.
- [ ] Dacă folosiți API-ul actual: launcher-ul nu este creat în interiorul unui click.
- [ ] Puteți explica diferența dintre API-ul vechi și Activity Result API, plus `Serializable` vs `Parcelable`.

## Bibliografie

- [Activity Result API](https://developer.android.com/training/basics/intents/result)
- [Intents and Intent Filters](https://developer.android.com/guide/components/intents-filters)
- [Serializable (Java SE)](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/Serializable.html)
- [Parcelable](https://developer.android.com/reference/android/os/Parcelable)
- [Parcelables and bundles](https://developer.android.com/guide/components/activities/parcelables-and-bundles)
- [IntentCompat](https://developer.android.com/reference/androidx/core/content/IntentCompat)

## Indexul ghidurilor

| Fișier                                                                     | Temă                                    |
| -------------------------------------------------------------------------- | --------------------------------------- |
| [01-proiect-activity-view-binding.md](01-proiect-activity-view-binding.md) | Proiect, Activity, View Binding, Intent |
| [02-formular-si-model.md](02-formular-si-model.md)                         | Formular, controale, model (POJO)       |
| [03-validare-activity-result.md](03-validare-activity-result.md)           | Validare, Activity Result API           |
| [04-lista-crud-memorie.md](04-lista-crud-memorie.md)                       | ListView, CRUD în memorie               |
| [05-adapter-meniu-fragmente.md](05-adapter-meniu-fragmente.md)             | Adapter personalizat, meniu, fragmente  |
| [06-retea-xml-bnr.md](06-retea-xml-bnr.md)                                 | Rețea asincronă, XML BNR                |
| [07-json-sharedpreferences.md](07-json-sharedpreferences.md)               | JSON, SharedPreferences                 |
| [08-proba1.md](08-proba1.md)                                               | Proba practică 1                        |
| [09-room.md](09-room.md)                                                   | Room (SQLite)                           |
| [10-firebase.md](10-firebase.md)                                           | Firebase Realtime Database              |
| [11-grafica-canvas.md](11-grafica-canvas.md)                               | Grafică 2D pe Canvas                    |
| [12-proba2.md](12-proba2.md)                                               | Proba practică 2                        |
| [13-recapitulare-publicare.md](13-recapitulare-publicare.md)               | Recapitulare, hărți (demo), publicare   |
