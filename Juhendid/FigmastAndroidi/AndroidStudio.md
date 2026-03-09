# Step 2 — Figma custom komponentide viimine Android Studiosse

See dokument kirjeldab **ainult** seda osa, mis oli seotud Figma põhjal tehtud custom komponentidega:

- `AppTextField`
- `AppTextArea`
- `AppPrimaryButton`

Siin **ei ole** authi ega Firebase'i.

---

## 1. `Color.kt`

**Faili asukoht:**

```text
app/src/main/java/com/example/blogi/ui/theme/Color.kt
```

**Lisa need read faili sisse:**

```kotlin
val FieldBackground = Color(0xFFFFFFFF)
val FieldBorder = Color(0xFFE5E5E5)
val FieldPlaceholder = Color(0xFF9E9E9E)

val PrimaryButtonBackground = Color(0xFF1E88E5)
val PrimaryButtonText = Color(0xFFFFFFFF)
```

**Milleks:**
- `FieldBackground` = inputi ja textarea taust
- `FieldBorder` = inputi ja textarea border
- `FieldPlaceholder` = placeholder teksti värv
- `PrimaryButtonBackground` = nupu taust
- `PrimaryButtonText` = nupu teksti värv

---

## 2. `AppTextField.kt`

**Faili asukoht:**

```text
app/src/main/java/com/example/blogi/ui/components/AppTextField.kt
```

**Kogu faili sisu:**

```kotlin
package com.example.blogi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.blogi.ui.theme.FieldBackground
import com.example.blogi.ui.theme.FieldBorder
import com.example.blogi.ui.theme.FieldPlaceholder

object InputFieldTokens {
    val borderRadius: Dp = 6.dp
    val borderWidth: Dp = 1.dp
    val paddingHorizontal: Dp = 12.dp
    val paddingVertical: Dp = 4.dp
    val fieldHeight: Dp = 36.dp
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(InputFieldTokens.fieldHeight)
            .background(
                color = FieldBackground,
                shape = RoundedCornerShape(InputFieldTokens.borderRadius)
            )
            .border(
                width = InputFieldTokens.borderWidth,
                color = FieldBorder,
                shape = RoundedCornerShape(InputFieldTokens.borderRadius)
            )
            .padding(
                horizontal = InputFieldTokens.paddingHorizontal,
                vertical = InputFieldTokens.paddingVertical
            )
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = FieldPlaceholder
                    )
                }
                innerTextField()
            }
        )
    }
}
```

**Figma väärtused, mis siia läksid:**
- height = `36`
- radius = `6`
- border width = `1`
- border color = `#E5E5E5`
- background = `#FFFFFF`
- padding horizontal = `12`
- padding vertical = `4`

---

## 3. `AppTextArea.kt`

**Faili asukoht:**

```text
app/src/main/java/com/example/blogi/ui/components/AppTextArea.kt
```

**Kogu faili sisu:**

```kotlin
package com.example.blogi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogi.ui.theme.FieldBackground
import com.example.blogi.ui.theme.FieldBorder
import com.example.blogi.ui.theme.FieldPlaceholder

@Composable
fun AppTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(114.dp)
            .background(
                color = FieldBackground,
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 1.dp,
                color = FieldBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(12.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = FieldPlaceholder
                    )
                }
                innerTextField()
            }
        )
    }
}
```

**Figma väärtused, mis siia läksid:**
- height = `114`
- radius = `6`
- border width = `1`
- border color = `#E5E5E5`
- background = `#FFFFFF`
- inner padding = `12`

---

## 4. `AppPrimaryButton.kt`

**Faili asukoht:**

```text
app/src/main/java/com/example/blogi/ui/components/AppPrimaryButton.kt
```

**Kogu faili sisu:**

```kotlin
package com.example.blogi.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.blogi.ui.theme.PrimaryButtonBackground
import com.example.blogi.ui.theme.PrimaryButtonText

object PrimaryButtonTokens {
    val buttonHeight: Dp = 36.dp
    val borderRadius: Dp = 6.dp
}

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(PrimaryButtonTokens.buttonHeight),
        shape = RoundedCornerShape(PrimaryButtonTokens.borderRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryButtonBackground,
            contentColor = PrimaryButtonText
        )
    ) {
        Text(text = text)
    }
}
```

**Figma väärtused, mis siia läksid:**
- height = `36`
- radius = `6`
- background = `#1E88E5`
- text color = `#FFFFFF`

---

## 5. `CreateScreen.kt` import-read

**Faili asukoht:**

```text
app/src/main/java/com/example/blogi/feature_create/ui/CreateScreen.kt
```

**Faili ülaossa lisa need import-read:**

```kotlin
import com.example.blogi.ui.components.AppPrimaryButton
import com.example.blogi.ui.components.AppTextArea
import com.example.blogi.ui.components.AppTextField
```

---

## 6. `CreateScreen.kt` input plokk

**Sama fail:**

```text
app/src/main/java/com/example/blogi/feature_create/ui/CreateScreen.kt
```

**`Column { ... }` sisse lisa see plokk:**

```kotlin
AppTextField(
    value = title,
    onValueChange = { if (it.length <= 100) title = it },
    placeholder = "Pealkiri"
)
```

**Milleks:**
- see kuvab custom inputi
- see kasutab Figma põhjal tehtud `AppTextField` komponenti

---

## 7. `CreateScreen.kt` textarea plokk

**Sama fail:**

```text
app/src/main/java/com/example/blogi/feature_create/ui/CreateScreen.kt
```

**`Column { ... }` sisse lisa see plokk:**

```kotlin
AppTextArea(
    value = content,
    onValueChange = { if (it.length <= 2000) content = it },
    placeholder = "Kirjuta postituse sisu siia..."
)
```

**Milleks:**
- see kuvab custom suurema tekstikasti
- see kasutab Figma põhjal tehtud `AppTextArea` komponenti

---

## 8. `CreateScreen.kt` tekstiloendur

**Sama fail:**

```text
app/src/main/java/com/example/blogi/feature_create/ui/CreateScreen.kt
```

**`Column { ... }` sisse lisa see plokk:**

```kotlin
Text(
    text = "${content.length} / 2000",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

**Milleks:**
- see näitab, mitu märki kasutaja on sisestanud

---

## 9. `CreateScreen.kt` button plokk

**Sama fail:**

```text
app/src/main/java/com/example/blogi/feature_create/ui/CreateScreen.kt
```

**`Column { ... }` sisse lisa see plokk:**

```kotlin
AppPrimaryButton(
    text = "Salvesta postitus",
    enabled = isValid,
    onClick = {
        onSavePost(title, content)
        title = ""
        content = ""
    }
)
```

**Milleks:**
- see kuvab custom nupu
- see kasutab Figma põhjal tehtud `AppPrimaryButton` komponenti

---

## 10. Figma → Android Studio seos

## Input
Figma väärtused:
- width `328`
- height `36`
- border radius `6`
- border `1`
- border color `#E5E5E5`
- background `#FFFFFF`
- padding horizontal `12`
- padding vertical `4`

Need läksid siia faili:
- `AppTextField.kt`

---

## TextArea
Figma väärtused:
- width `328`
- height `114`
- border radius `6`
- border `1`
- border color `#E5E5E5`
- background `#FFFFFF`

Need läksid siia faili:
- `AppTextArea.kt`

---

## Button
Figma väärtused:
- width `328`
- height `36`
- border radius `6`
- fill näiteks `#1E88E5`
- text color `#FFFFFF`

Need läksid siia faili:
- `AppPrimaryButton.kt`

---

## 11. Kokkuvõte

Step 2 käigus tegime Android Studiosse kolm custom komponenti:

- `AppTextField.kt`
- `AppTextArea.kt`
- `AppPrimaryButton.kt`

Lisaks lisasime värvid `Color.kt` faili ja kasutasime neid `CreateScreen.kt` sees.

Selles etapis me **ei teinud authi ega Firebase'it**.
