package com.joseruiz.calculadoracompose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Stack
import kotlin.math.pow

/**
 * variable que almacena los simbolos de los botones que se usarán en la calculadora.
 */
val buttonsList = listOf(
    "C", "(", ")", "^",
    "7", "8", "9", "*",
    "4", "5", "6", "-",
    "1", "2", "3", "+",
    "0", "=", "/",
)

// Funciones que realizan la logica de la calculadora

/**
 * Método para determinar la precedencia de un operador.
 *
 * @param c El operador cuya precedencia se desea conocer.
 * @return El nivel de precedencia del operador, o -1 si el operador no es reconocido.
 */
private fun precedence(c: Char): Int {
    return when (c) {
        '+', '-' -> 1
        '*', '/' -> 2
        '^' -> 3
        else -> -1
    }
}

/**
 * Convierte una expresión infix a postfix.
 *
 * @param expresion La expresión en formato infix.
 * @return La expresión convertida en formato postfix.
 */
private fun infixToPostfix(expresion: String): String {
    val result = StringBuilder()
    val stack = Stack<Char>()

    try {
        val cleanedExpression = expresion.replace("\\s+".toRegex(), "")

        var i = 0
        while (i < cleanedExpression.length) {
            val c = cleanedExpression[i]

            if (c.isLetterOrDigit()) {
                while (i < cleanedExpression.length && cleanedExpression[i].isLetterOrDigit()) {
                    result.append(cleanedExpression[i])
                    i++
                }
                result.append(' ')
                i--
            } else if (c == '(') {
                stack.push(c)
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(')
                    result.append(stack.pop()).append(' ')
                if (!stack.isEmpty() && stack.peek() != '(') {
                    return "Expresión inválida"
                } else {
                    stack.pop()
                }
            } else {
                while (!stack.isEmpty() && precedence(c) <= precedence(stack.peek()))
                    result.append(stack.pop()).append(' ')
                stack.push(c)
            }
            i++
        }

        while (!stack.isEmpty()) {
            if (stack.peek() == '(')
                return "Expresión inválida"
            result.append(stack.pop()).append(' ')
        }

        return result.toString().trim()
    } catch (e: Exception) {
        return "Expresión inválida"
    }
}

/**
 * Método para convertir de string a lista
 */
private fun stringToList(expression: String): List<String> {
    return expression.split(" ").filter { it.isNotEmpty() }
}

/**
 * Método para evaluar la operación postfix
 */
private fun evaluatePostfix(postfix: List<String>): Double {
    val stack = Stack<Double>()

    for (token in postfix) {
        when {
            token.toDoubleOrNull() != null -> stack.push(token.toDouble())
            else -> {
                val b = stack.pop()
                val a = stack.pop()
                stack.push(
                    when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> a / b
                        "^" -> a.pow(b)
                        else -> throw IllegalArgumentException("Unknown operator: $token")
                    }
                )
            }
        }
    }
    return stack.pop()
}

fun isValidInfixExpression(expression: String): Boolean {
    // Eliminamos espacios para simplificar la validación
    val cleanedExpression = expression.replace("\\s+".toRegex(), "")

    // Expresión regular para verificar números y operadores
    val singleOperatorRegex = Regex("^[+\\-*/^]$")
    val doubleSignRegex = Regex("([+\\-*/^]{2,}|\\d+[+\\-*/^]{2,}|[+\\-*/^]{2,}\\d+)")

    // Verificamos si la expresión contiene solo un símbolo matemático
    if (singleOperatorRegex.matches(cleanedExpression)) {
        return false // Solo un símbolo matemático no es una expresión válida
    }

    // Verificamos si la expresión contiene un número con dos signos consecutivos
    if (doubleSignRegex.containsMatchIn(cleanedExpression)) {
        return false // Contiene números con dos signos consecutivos
    }

    // Si no se encontró ninguna de las condiciones inválidas, la expresión es válida
    return true
}

/**
 * Método para evaluar la operación Infix
 */
fun evaluate(expression: String): String {
    return try {
        if (!isValidInfixExpression(expression)) {
            "Error!"
        } else {
            val postfixExpression = infixToPostfix(expression)
            if (postfixExpression == "Expresión inválida") {
                "Error!"
            } else {
                val result = evaluatePostfix(stringToList(postfixExpression))
                result.toString()
            }
        }
    } catch (e: Exception) {
        "Error!"
    }
}

// Funcion que se encarga de manejar las acciones que ocurren cuando el usuario presiona un boton
fun handleButtonClick(
    btnName: String,
    currentExpression: String,
    onExpressionChange: (String) -> Unit, // Aquí va a setear { newExpr -> expression = newExpr }
    onResultChange: (String) -> Unit // Aquí va a setear { res -> result = res }
) {
    when (btnName) {
        "C" -> {
            onExpressionChange("")
            onResultChange("")
        }

        "=" -> {
            // Evaluar la expresión y mostrar el resultado
            val result = evaluate(currentExpression)
            onResultChange(result)
        }

        else -> {
            val newExpression = currentExpression + btnName
            onExpressionChange(newExpression)
        }
    }
}

// Funcion encagada de crear los botones
@Composable
fun calculatorButtons(btnName: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.padding(10.dp)
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            containerColor = getColorButton(btnName)
        ) {
            Text(
                text = btnName,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = getColorText(btnName)
            )
        }
    }
}

// Función que crea el esqueleto de la aplicacion
@Composable
fun calculator(modifier: Modifier = Modifier) {
    // Estado para la expresión actual y el resultado
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Box(modifier = modifier) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            // Mostrar la expresión y el resultado
            Text(
                text = expression,
                style = TextStyle(
                    fontSize = 60.sp,
                    textAlign = TextAlign.End
                ),
                maxLines = 2,
                modifier = Modifier.padding(top = 180.dp, start = 10.dp)
            )
            if (result.isNotEmpty()) {
                Text(
                    text = result,
                    style = TextStyle(
                        fontSize = 40.sp,
                        textAlign = TextAlign.End
                    ),
                    maxLines = 1,
                    modifier = Modifier.padding(top = 10.dp, start = 10.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4)
            ) {
                items(buttonsList) {
                    calculatorButtons(
                        btnName = it,
                        onClick = {
                            handleButtonClick(
                                it, // Nombre del boton
                                expression, // Expresión actual del boton
                                { newExpr -> expression = newExpr }, // Lambda para actualizar expresión
                                { res -> result = res } // Lambda para actualizar resultado
                            )
                        })
                }
            }
        }
    }
}

// Función para cambiar el color del boton
fun getColorButton(btn: String): Color {
    if (btn == "(" || btn == ")" || btn == "^" || btn == "*" || btn == "-" || btn == "+" || btn == "/" || btn == "=")
        return Color.Blue
    if (btn == "C")
        return Color.Gray

    return Color.White
}

// Función para cambiar el color del texto
fun getColorText(btn: String): Color {
    if (btn == "(" || btn == ")" || btn == "^" || btn == "*" || btn == "-" || btn == "+" || btn == "/" || btn == "=" || btn == "C")
        return Color.White

    return Color.Black
}