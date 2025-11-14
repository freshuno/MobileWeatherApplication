# Aplikacja Pogodowa na Androida

## Część Teoretyczna
### Źródła
- [Dokumentacja Android Developer](https://developer.android.com/docs)
- [Weather API](https://openweathermap.org/api)
- [JsonViewer](https://jsonviewer.stack.hu)
### Prezentacja
- [Link do prezentacji](https://github.com/Luckownia/WeatherForecastApp/blob/master/Aplikacja%20pogodowa%20na%20Androida.pptx)
- [Link do prezentacji w przeglądarce](https://www.canva.com/design/DAGEwcgYc-o/AsfCxI0Z0bzzugm9d2XWvg/view?utm_content=DAGEwcgYc-o&utm_campaign=designshare&utm_medium=link&utm_source=editor)
## Część Praktyczna

## Zadanie 1: 

**Cel:** Nauka podstawowych elementów Android Stuido

### Kroki:
1. **Stwórz nowy projekt:**
  - Kliknij New Project
  - Wybierz Empty Views Activity, jako język wybierz Java.
  - Poczekaj, aż zakończy się początkowa konfiguracja (pasek postępu na dole ekranu).
    
2. **Dodaj tekst:**
  - Otwórz plik `activity_main.xml`.
  - Dodaj dowolny tekst za pomocą kodu podanego poniżej, albo za pomocą edytora designu dostępnego w prawym górnym rogu lub po użyciu kombinacji klawiszy Alt+Shift+Left
   ```xml
   <TextView
        android:id="@+id/textView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="176dp"
        android:layout_marginTop="184dp"
        android:text="Zadanie"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
   ```
  - Jeżeli używasz edytora designu, pamiętaj aby w Attributes -> Layout -> Constraint Widget ustawić Constraints, inaczej będzie się źle wyświetlało w aplikacji.
  - Zmień kolor tekstu na zielony, użyj do tego dowolnego sposobu.
3. **Sprawdź czy działa:**
  - Uruchom aplikacje w emulatorze i zobacz czy twój tekst poprawnie się wyświetla. Jeśli się wyświetla to wyślij zrzut ekranu na Upel.

## Zadanie 2: 

**Cel:** Nauka podstaw obsługi zdarzeń i interakcji z użytkownikiem

### Kroki:
1. **Dodaj przycisk:**
  - Otwórz plik `activity_main.xml`.
  - Dodaj przycisk za pomocą edytora designu, lub użyj kodu poniżej.
   ```xml
   <Button
        android:id="@+id/button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="45dp"
        android:layout_marginEnd="163dp"
        android:text="Button"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/textView" />
   ```
    
2. **Spraw by kliknięcie guzika zmieniało treść napisu:**
  - Otwórz plik `MainActivity.java`.
  - Spraw aby po kliknięciu przycisku zmieniał się tekst dodany w poprzednim zadaniu na dowolny inny. Zastosuj metodę pokazaną na prezentacji, lub zadeklaruj funkcję w `MainActivity.java` i przypisz ją do guzika w `activity_main.xml` używając Attributes -> onClick
  - Pamiętaj o zaimportowaniu odpowiednich bibliotek, wykorzystaj findViewById. Metoda guzika odpowiadająca za zmienianie tekstu musi znajdować się w metodzie onCreate().
3. **Sprawdź czy działa:**
  - Uruchom aplikacje w emulatorze i zobacz czy twój przycisk działa poprawnie. Jeżeli działa to wyślij zrzut ekranu na Upel.

## Zadanie 3: 

**Cel:** Nauka podstaw obsługi danych z Api.

### Kroki:
1. **Otwórz gotowy projekt, który już będzie zawierał część potrzebnego kodu:**
  - Pobierz [archiwum](https://github.com/Luckownia/WeatherForecastApp/blob/master/Zadanie3.rar)
  - Rozpakuj je i otwórz w Android Studio (poczekaj aż wszystko się zsynchronizuje).
  - Zapoznaj się z kodem, projekt zawiera działające Api pobierające dane o aktualnej pogodzie w Krakowie.
2. **Uruchom aplikacje i zobacz jak wygląda otrzymana odpowiedź Json:**
  - Uruchom aplikacje i kliknij w przycisk. W konsoli Logcat (jeżeli nie wiesz jak ją uruchomić to zajrzyj do prezentacji) pojawił się link do odpowiedzi Json. Wejdź w ten link.
  - Żeby uzyskać czytelniejszą formę odpowiedzi udaj się na [jsonviewer.stack.hu](https://jsonviewer.stack.hu/).
  - Zobacz co zawiera ta odpowiedź.
3. **Spraw by po kliknięciu przycisku napis Hello World! zamieniał się na opis aktualnej pogody w Krakowie:**
  - Wróć do pliku `MainActivity.java`.
  - Odszukaj fragment, w którym musisz wstawić swój kod.
  - Napisz kod, który wyłuska dane o opisie (description) pogody z obiektu JSON i przypisze je do zmiennej. Sprawdź w jakiej tablicy opis pogody się znajduje.
  - Skorzystaj z metod getJSONArray(), getJSONObject(). Możesz wykorzystać ten [poradnik](https://www.baeldung.com/java-jsonobject-get-value#getting-values-directly).
  - Korzystając z metody setText() i zmiennej Description (która zawiera element interfejsu TextView o ID TextView) spraw, by tekst na ekranie zmienił się na aktualny opis pogody.
  - W razie problemów zajrzyj do [prezentacji](https://github.com/Luckownia/WeatherForecastApp/blob/master/Aplikacja%20pogodowa%20na%20Androida.pptx).
4. **Sprawdź czy działa:**
  - Uruchom ponownie aplikacje i zobacz czy wszystko działa jak powinno. Jeżeli działa to wyślij zrzut ekranu na Upel.

