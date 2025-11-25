// Fixed Scanner.kt - Add semicolon handling

package main

class RuntimeError(message: String, val line: Int = 0) : RuntimeException(message)

class Evaluator {

    private var environment = Environment()

    init {
        // Register built-in constructors
        environment.define("ChampionEntity", BuiltinFunction("ChampionEntity") { args ->
            val name = args.getOrNull(0)?.toString()
            ChampionEntity(name)
        })

        environment.define("AbilityEntity", BuiltinFunction("AbilityEntity") { args ->
            val name = args.getOrNull(0)?.toString()
            AbilityEntity(name)
        })

        environment.define("ItemEntity", BuiltinFunction("ItemEntity") { args ->
            val name = args.getOrNull(0)?.toString()
            val cost = (args.getOrNull(1) as? Double)?.toInt() ?: 0
            ItemEntity(name, cost)
        })

        environment.define("BuffEntity", BuiltinFunction("BuffEntity") { args ->
            val name = args.getOrNull(0)?.toString()
            BuffEntity(name)
        })
    }

    private fun evaluate(expr: Expr): Any? {
        return when (expr) {
            is Expr.EventHandler -> evaluateEventHandler(expr)
            is Expr.Call -> evaluateCall(expr)
            is Expr.Binary -> evaluateBinary(expr)
            is Expr.Unary -> evaluateUnary(expr)
            is Expr.Literal -> expr.value
            is Expr.Variable -> environment.get(expr.name)
            is Expr.Assign -> evaluateAssign(expr)
            is Expr.Grouping -> evaluate(expr.expression)
            is Expr.MethodCall -> evaluateMethodCall(expr)
        }
    }

    private fun evaluateAssign(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    // Method call evaluation
    private fun evaluateMethodCall(call: Expr.MethodCall): Any? {
        val obj = evaluate(call.obj)
        val methodName = call.methodName.lexeme
        val args = call.args.map { evaluate(it) }

        // Handle ChampionEntity methods
        if (obj is ChampionEntity) {
            return when (methodName) {
                "onCastHealth" -> {
                    if (args.size != 2) throw RuntimeError("onCastHealth requires 2 arguments: threshold and action", call.methodName.line)
                    val threshold = args[0] as? Double ?: throw RuntimeError("First argument must be a number", call.methodName.line)
                    val action = args[1]?.toString() ?: ""
                    obj.onCastHealth(threshold, action)
                }
                "onCastMana" -> {
                    if (args.size != 2) throw RuntimeError("onCastMana requires 2 arguments: threshold and action", call.methodName.line)
                    val threshold = args[0] as? Double ?: throw RuntimeError("First argument must be a number", call.methodName.line)
                    val action = args[1]?.toString() ?: ""
                    obj.onCastMana(threshold, action)
                }
                "recall" -> obj.recall()
                "cast" -> {
                    if (args.size != 2) throw RuntimeError("cast requires 2 arguments: ability name and mana cost", call.methodName.line)
                    val abilityName = args[0]?.toString() ?: ""
                    val manaCost = args[1] as? Double ?: throw RuntimeError("Second argument must be a number", call.methodName.line)
                    obj.cast(abilityName, manaCost)
                }
                "attack" -> {
                    if (args.isEmpty()) throw RuntimeError("attack requires a target", call.methodName.line)
                    val target = args[0]?.toString() ?: ""
                    obj.attack(target)
                }
                "moveTo" -> {
                    if (args.isEmpty()) throw RuntimeError("moveTo requires a location", call.methodName.line)
                    val location = args[0]?.toString() ?: ""
                    obj.moveTo(location)
                }
                "useItem" -> {
                    if (args.isEmpty()) throw RuntimeError("useItem requires an item name", call.methodName.line)
                    val itemName = args[0]?.toString() ?: ""
                    obj.useItem(itemName)
                }
                "addAbility" -> {
                    if (args.isEmpty()) throw RuntimeError("addAbility requires an ability name", call.methodName.line)
                    val abilityName = args[0]?.toString() ?: ""
                    obj.addAbility(abilityName)
                }
                "checkEvents" -> {
                    obj.checkEvents()
                    obj
                }
                else -> throw RuntimeError("Unknown method '$methodName' on Champion", call.methodName.line)
            }
        }

        // Handle AbilityEntity methods
        if (obj is AbilityEntity) {
            return when (methodName) {
                "setCooldown" -> {
                    if (args.isEmpty()) throw RuntimeError("setCooldown requires a cooldown value", call.methodName.line)
                    val cd = args[0] as? Double ?: throw RuntimeError("Argument must be a number", call.methodName.line)
                    obj.setCooldown(cd)
                }
                "reduceCooldown" -> {
                    if (args.isEmpty()) throw RuntimeError("reduceCooldown requires an amount", call.methodName.line)
                    val amount = args[0] as? Double ?: throw RuntimeError("Argument must be a number", call.methodName.line)
                    obj.reduceCooldown(amount)
                }
                else -> throw RuntimeError("Unknown method '$methodName' on Ability", call.methodName.line)
            }
        }

        // Handle ItemEntity methods
        if (obj is ItemEntity) {
            return when (methodName) {
                "addStat" -> {
                    if (args.size != 2) throw RuntimeError("addStat requires 2 arguments: stat name and value", call.methodName.line)
                    val statName = args[0]?.toString() ?: ""
                    val value = args[1] as? Double ?: throw RuntimeError("Second argument must be a number", call.methodName.line)
                    obj.addStat(statName, value)
                }
                else -> throw RuntimeError("Unknown method '$methodName' on Item", call.methodName.line)
            }
        }

        throw RuntimeError("Cannot call method on non-entity object", call.methodName.line)
    }

    private fun evaluateChampionStatement(championStmt: Stmt.Champion): Any? {
        println("Champion: ${championStmt.name.lexeme}")
        championStmt.events.forEach { eventHandlerExpr ->
            evaluate(eventHandlerExpr)
        }
        return null
    }

    private fun evaluateEventHandler(handler: Expr.EventHandler): Any? {
        val eventName = handler.eventType.lexeme
        val params = handler.params.joinToString(", ") { it.lexeme }
        println("  Event: $eventName($params)")

        handler.body.forEach { stmt ->
            evaluateStatement(stmt)
        }
        return null
    }

    fun evaluateStatement(stmt: Stmt): Any? {
        return when (stmt) {
            is Stmt.Champion -> evaluateChampionStatement(stmt)
            is Stmt.If -> evaluateIf(stmt)
            is Stmt.While -> evaluateWhile(stmt)
            is Stmt.Combo -> evaluateCombo(stmt)
            is Stmt.Expression -> evaluate(stmt.expression)
            is Stmt.Block -> {
                val previous = environment
                try {
                    environment = Environment(previous)
                    stmt.statements.forEach { evaluateStatement(it) }
                    null
                } finally {
                    environment = previous
                }
            }
            is Stmt.Print -> {
                val value = evaluate(stmt.expression)
                println(stringify(value))
                null
            }
            is Stmt.Var -> {
                val value = if (stmt.initializer != null) {
                    evaluate(stmt.initializer)
                } else {
                    null
                }
                environment.define(stmt.name.lexeme, value)
                null
            }
        }
    }

    private fun evaluateIf(ifStmt: Stmt.If): Any? {
        val condition = evaluate(ifStmt.condition)
        return if (isTruthy(condition)) {
            ifStmt.thenBranch.forEach { evaluateStatement(it) }
            null
        } else if (ifStmt.elseBranch != null) {
            ifStmt.elseBranch.forEach { evaluateStatement(it) }
            null
        } else {
            null
        }
    }

    private fun evaluateWhile(whileStmt: Stmt.While): Any? {
        while (isTruthy(evaluate(whileStmt.condition))) {
            whileStmt.body.forEach { evaluateStatement(it) }
        }
        return null
    }

    private fun evaluateCombo(combo: Stmt.Combo): Any? {
        println("    Combo executing...")
        combo.actions.forEach { evaluateStatement(it) }
        return null
    }

    private fun evaluateCall(call: Expr.Call): Any? {
        val callee = environment.get(call.callee)
        val args = call.args.map { evaluate(it) }

        if (callee is BuiltinFunction) {
            return callee.implementation(args)
        }

        println("    Calling: ${call.callee.lexeme}(${args.joinToString(", ")})")
        return null
    }

    private fun evaluateBinary(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)
        val operator = expr.operator.lexeme

        return when (operator) {
            "+" -> {
                when {
                    left is Double && right is Double -> left + right
                    left is String && right is String -> left + right
                    left is Double && right is String -> left.toString() + right
                    left is String && right is Double -> left + right.toString()
                    else -> throw RuntimeError("Operands must be two numbers or two strings.", expr.operator.line)
                }
            }
            "-" -> {
                requireNumbers(left, right, expr.operator)
                (left as Double) - (right as Double)
            }
            "*" -> {
                requireNumbers(left, right, expr.operator)
                (left as Double) * (right as Double)
            }
            "/" -> {
                requireNumbers(left, right, expr.operator)
                if (right == 0.0) {
                    throw RuntimeError("Division by zero.", expr.operator.line)
                }
                (left as Double) / (right as Double)
            }
            "%" -> {
                requireNumbers(left, right, expr.operator)
                (left as Double) % (right as Double)
            }
            ">" -> {
                requireNumbers(left, right, expr.operator)
                (left as Double) > (right as Double)
            }
            ">=" -> {
                requireNumbers(left, right, expr.operator)
                (left as Double) >= (right as Double)
            }
            "<" -> {
                requireNumbers(left, right, expr.operator)
                (left as Double) < (right as Double)
            }
            "<=" -> {
                requireNumbers(left, right, expr.operator)
                (left as Double) <= (right as Double)
            }
            "==" -> isEqual(left, right)
            "and" -> isTruthy(left) && isTruthy(right)
            "or" -> isTruthy(left) || isTruthy(right)
            else -> throw RuntimeError("Unknown operator: $operator", expr.operator.line)
        }
    }

    private fun evaluateUnary(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)
        val operator = expr.operator.lexeme

        return when (operator) {
            "-" -> {
                requireNumber(right, expr.operator)
                -(right as Double)
            }
            "not" -> !isTruthy(right)
            else -> throw RuntimeError("Unknown unary operator: $operator", expr.operator.line)
        }
    }

    private fun isTruthy(value: Any?): Boolean {
        if (value == null) return false
        if (value is Boolean) return value
        return true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null || b == null) return false
        return a == b
    }

    private fun requireNumber(value: Any?, token: Token) {
        if (value !is Double) {
            throw RuntimeError("Operand must be a number.", token.line)
        }
    }

    private fun requireNumbers(left: Any?, right: Any?, token: Token) {
        if (left !is Double || right !is Double) {
            throw RuntimeError("Operands must be numbers.", token.line)
        }
    }

    private fun stringify(value: Any?): String {
        if (value == null) return "nil"
        if (value is Double) {
            val text = value.toString()
            if (text.endsWith(".0")) {
                return text.substring(0, text.length - 2)
            }
            return text
        }
        return value.toString()
    }
}