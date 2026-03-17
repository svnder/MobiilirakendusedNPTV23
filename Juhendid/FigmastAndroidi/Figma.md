## Eesmärk

See dokument kirjeldab, kuidas võtta Figma failist välja arenduseks vajalik **koodiinfo**, **mõõdud**, **värvid**, **spacing**, **state’id** ja **code snippet’id**, nii et disain oleks arendajale üheselt loetav. Figma Dev Mode on mõeldud disaini navigeerimiseks ja koodiks tõlgendamiseks ning selle kaudu saab vaadata inspect-andmeid ja genereeritud code snippet’e.

---

## 1. Faili struktuur Figma sees

Loo Figma faili kaks page’i:

- `Screens`
- `Components`

### `Screens`
Siia lähevad valmis ekraanid:
- `LoginScreen`
- `RegisterScreen`
- `CreateScreen`

### `Components`
Siia lähevad korduvkasutatavad komponendid:
- `Input`
- `TextArea`
- `Button`

---

## 2. Ekraanide ehitamine

Iga ekraan tehakse **Frame** sisse.

### Sammud
1. Vajuta `F`
2. Loo phone frame
3. Pane mõõdud:
   - `W = 360`
   - `H = 800`
4. Nimeta frame näiteks:
   - `LoginScreen`
   - `RegisterScreen`
   - `CreateScreen`

### Soovitus
Lisa igale ekraanile **Auto Layout**:
- Direction: `Vertical`
- Spacing: `16`
- Padding:
  - Top: `24`
  - Right: `16`
  - Bottom: `24`
  - Left: `16`

---

## 3. Komponentide ehitamine

## 3.1 Input

### Loo inputi frame
1. Mine page’ile `Components`
2. Vajuta `F`
3. Pane mõõdud:
   - `W = 328`
   - `H = 36`
4. Nimeta:
   `Input / State=Default`

### Lisa Auto Layout
- Direction: `Horizontal`
- Vertical align: `Center`
- Padding:
  - Left: `12`
  - Right: `12`
  - Top: `4`
  - Bottom: `4`

### Lisa stiil
- Fill: `#FFFFFF`
- Stroke: `#E5E5E5`
- Stroke width: `1`
- Corner radius: `6`

### Lisa placeholder tekst
1. Vajuta `T`
2. Kirjuta näiteks:
   `Email`
3. Pane näiteks:
   - Font size: `14`
   - Weight: `Regular`
   - Color: `#9E9E9E`

### Tee component
1. Vali kogu input
2. Vajuta **Create component**

### Tee variandid
Tee koopiad ja nimeta:
- `Input / State=Default`
- `Input / State=Focused`
- `Input / State=Error`
- `Input / State=Disabled`

Muuda iga variandi stroke/fill vastavalt state’ile.

Siis:
1. Vali kõik variandid
2. Vajuta **Combine as variants**

---

## 3.2 TextArea

### Loo tekstikasti frame
1. Kopeeri `Input`
2. Muuda mõõdud:
   - `W = 328`
   - `H = 114`
3. Nimeta:
   `TextArea / State=Default`

### Placeholder
Muuda placeholder tekst näiteks:
`Kirjuta siia...`

### Variandid
Tee samad state’id:
- Default
- Focused
- Error
- Disabled

Siis:
1. Vali kõik
2. **Combine as variants**

---

## 3.3 Button

### Loo nupu frame
1. Vajuta `F`
2. Pane mõõdud:
   - `W = 328`
   - `H = 36`
3. Nimeta:
   `Button / State=Default`

### Lisa Auto Layout
- Horizontal
- Center align
- Center justify

### Lisa stiil
- Fill: näiteks `#1E88E5`
- Corner radius: `6`

### Lisa tekst
1. Vajuta `T`
2. Kirjuta:
   `Salvesta postitus`
3. Tekstivärv:
   `#FFFFFF`

### Variandid
Tee:
- `Button / State=Default`
- `Button / State=Pressed`
- `Button / State=Disabled`

Siis:
1. Vali kõik
2. **Combine as variants**

---

## 4. Komponentide lisamine ekraanidele

Mine tagasi page’ile `Screens`.

### Komponendi lisamine
1. Vajuta `Shift + I`
2. Otsi component nime järgi
3. Lisa ekraanile instance

### Näide `CreateScreen`
Lisa:
- `Input`
- `TextArea`
- `Button`

### Muuda instance tekstid
- `Input` → `Pealkiri`
- `TextArea` → `Kirjuta postituse sisu siia...`
- `Button` → `Salvesta postitus`

---

## 5. Dev Mode kasutamine

### Dev Mode avamine
1. Vali canvasel element
2. Vajuta `Shift + D`

### Dev Mode’is kontrolli
- Width
- Height
- Fill
- Stroke
- Stroke width
- Corner radius
- Auto Layout padding
- Spacing
- Typography
- Variables

---

## 6. Koodi vaatamine Dev Mode’is

### Koodinäitude avamine
1. Vali element
2. Ava Dev Mode
3. Paremal ava **Code** või **Inspect** osa

### Mida sealt võtta
Võta välja:
- mõõdud
- värvid
- radius
- border
- padding
- typography

### Mida mitte teha
Ära kasuta Figma snippet’it lõpliku rakenduse koodina.
Kasuta seda stiili- ja mõõduallikana.

---

## 7. Copy as code

Kui tahad ühe objekti koodi kiiresti kopeerida:

1. Vali objekt
2. Paremklõps
3. `Copy/Paste as...`
4. `Copy as code`
5. Vali:
   - CSS
   - iOS
   - Android

---

## 8. Variables

Variables hoiavad korduvkasutatavaid väärtusi, näiteks:
- värvid
- spacing
- radius
- suurused

### Loo variables
1. Ava variables paneel
2. Loo collection, näiteks:
   `Primitives`
3. Lisa väärtused:
   - `Background/White = #FFFFFF`
   - `Border/Default = #E5E5E5`
   - `Border/Focus = #3B82F6`
   - `Border/Error = #EF4444`
   - `Text/Placeholder = #9E9E9E`

---

## 9. Code syntax variables’ele

Kui tahad, et Dev Mode näitaks arendajale puhtamaid nimesid:

1. Ava variable
2. Leia **Code syntax**
3. Vajuta **Add code syntax**
4. Vali:
   - Web
   - Android
   - iOS
5. Kirjuta sinna sobiv nimi

### Näide
- Variable nimi Figma’s: `Border/Default`
- Android code syntax: `field_border`
- Web code syntax: `--field-border`

---

## 10. Mida dokumenteerida iga komponendi kohta

Iga komponendi kohta pane kirja:

### Component name
Näiteks:
- `Input`
- `TextArea`
- `Button`

### States
Näiteks:
- Default
- Focused
- Error
- Disabled

### Layout
- Width
- Height
- Padding
- Spacing

### Style
- Fill color
- Border color
- Border width
- Radius

### Typography
- Font size
- Weight
- Line height
- Placeholder color

### Code handoff
- Dev Mode snippet olemas
- Copy as code testitud
- Variables seotud
- Code syntax lisatud

---

## 11. Näide dokumenteeritud komponendist

### Input

- Name: `Input`
- States:
  - Default
  - Focused
  - Error
  - Disabled
- Width: `328`
- Height: `36`
- Padding:
  - Left: `12`
  - Right: `12`
  - Top: `4`
  - Bottom: `4`
- Fill: `#FFFFFF`
- Stroke: `#E5E5E5`
- Stroke width: `1`
- Radius: `6`
- Placeholder:
  - Font size: `14`
  - Color: `#9E9E9E`

### Handoff steps
1. Vali component
2. Ava Dev Mode
3. Kontrolli layout ja style omadused
4. Ava Code paneel
5. Vajadusel kasuta `Copy as code`
6. Kontrolli, kas variables ja code syntax kuvatakse õigesti

---

## 12. Code Connect

Kui eesmärk on siduda Figma komponent otse päris koodikomponendiga, siis selle jaoks on Figma’l **Code Connect**.
See ei ole sama asi mis tavaline Dev Mode snippet; Code Connect seob Figma komponendid sinu päris repo komponentidega.

---

## 13. Miinimumvalmisolek enne handoffi

Enne arendajale üleandmist kontrolli, et:

- kõik ekraanid on `Screens` page’il
- kõik korduvad osad on `Components` page’il
- komponentidel on state variandid
- nimetused on loetavad
- Dev Mode’is on mõõdud loetavad
- variables on kasutusel või vähemalt värvid on ühtsed
- vajadusel on code syntax lisatud
- `Copy as code` töötab vajalikel elementidel

---

## 14. Kokkuvõte

Figma “koodi osa” koosneb kolmest põhilisest asjast:

1. **Dev Mode → Code snippets**
2. **Right click → Copy as code**
3. **Variables → Code syntax**