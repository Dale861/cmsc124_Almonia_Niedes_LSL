package main

class AstPrinter(expr: Expr) {

    init {
        println(print(expr))
    }

    private fun print(expr: Expr): String {
        return when (expr) {
            is Expr.Binary -> "(${expr.operator.lexeme} ${print(expr.left)} ${print(expr.right)})"
            is Expr.Unary -> "(${expr.operator.lexeme} ${print(expr.right)})"
            is Expr.Literal -> expr.value?.toString() ?: "nil"
            is Expr.Variable -> expr.name.lexeme
            is Expr.Assign -> "(= ${expr.name.lexeme} ${print(expr.value)})"
            is Expr.MethodCall -> {
                val argsStr = expr.args.joinToString(" ") { print(it) }
                "(${print(expr.obj)}.${expr.methodName.lexeme} $argsStr)"
            }
            is Expr.Grouping -> print(expr.expression)
            is Expr.Call -> {
                val argsStr = expr.args.joinToString(" ") { print(it) }
                "(${expr.callee.lexeme} $argsStr)"
            }
            is Expr.EventHandler -> {
                // Your existing logic or simplified version
                "(eventHandler ...)"
            }
            else -> "unknown"
        }
    }
}


