package main

import java.io.File

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: lsl [script]")
        return
    }
    else if (args.size == 1) {
        runFile(args[0])
    }
    else {
        runRepl()
    }
}

fun runFile(path: String) {
    val source = File(path).readText()
    run(source, false)
}

fun runRepl() {
    println("==========================================")
    println("  Welcome to LoL Script Language (LSL)")
    println("==========================================")
    println()
    println("Type statements ending with ';'")
    println("Type 'exit' or 'quit' to exit")
    println("Type 'paste' to enter multi-line paste mode")
    println("Type 'run <filename>' to execute a script")
    println("Type 'clear' to reset the environment")
    println("Type 'help' for more commands")
    println()

    var evaluator = Evaluator()

    while (true) {
        print("LSL> ")
        val line = readLine() ?: break
        val trimmed = line.trim()
        val trimmedLower = trimmed.lowercase()

        when {
            trimmedLower == "exit" || trimmedLower == "quit" -> {
                println("Thanks for using LSL! Good luck on the Rift!")
                break
            }
            trimmedLower == "clear" -> {
                evaluator = Evaluator()
                println("Environment cleared!")
            }
            trimmedLower == "help" -> {
                showHelp()
            }
            trimmedLower == "paste" -> {
                handlePasteMode(evaluator)
            }
            trimmedLower.startsWith("run ") -> {
                val filename = trimmed.substring(4).trim()
                try {
                    runFile(filename)
                } catch (e: Exception) {
                    println("Error loading file '$filename': ${e.message}")
                }
            }
            trimmed.isEmpty() -> { }
            else -> run(line, true, evaluator)
        }
    }
}

fun showHelp() {
    println()
    println("=== LSL REPL Commands ===")
    println("  exit, quit     - Exit the REPL")
    println("  paste          - Enter multi-line paste mode (type 'END' to finish)")
    println("  run <file>     - Execute a script file")
    println("  clear          - Reset the environment")
    println("  help           - Show this help message")
    println()
    println("=== Example Code ===")
    println("  var champion champ = ChampionEntity(\"Lux\");")
    println("  champ.cast(\"Light Binding\", 50.0);")
    println("  print champ;")
    println()
}

fun handlePasteMode(evaluator: Evaluator) {
    println()
    println("=== Multi-line Paste Mode ===")
    println("Paste your code below.")
    println("Type 'END' on a new line when finished.")
    println("Type 'CANCEL' to abort.")
    println()

    val codeLines = mutableListOf<String>()

    while (true) {
        print("... ")
        val line = readLine() ?: break

        when (line.trim().uppercase()) {
            "END" -> {
                if (codeLines.isEmpty()) {
                    println("No code to execute.")
                    return
                }

                val code = codeLines.joinToString("\n")
                println()
                println("=== Executing Pasted Code ===")
                println()

                try {
                    run(code, false, evaluator)
                    println()
                    println("=== Execution Complete ===")
                } catch (e: Exception) {
                    println("Error executing code: ${e.message}")
                }
                return
            }
            "CANCEL" -> {
                println("Paste mode cancelled.")
                return
            }
            else -> {
                codeLines.add(line)
            }
        }
    }
}

fun run(source: String, isRepl: Boolean = false, evaluator: Evaluator? = null) {
    try {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()

        val parser = Parser(tokens)
        val statements = parser.parseStatements()

        val eval = evaluator ?: Evaluator()

        for (stmt in statements) {
            val result = eval.evaluateStatement(stmt)
            if (isRepl && stmt is Stmt.Expression && result != null) {
                println(result)
            }
        }
    } catch (e: RuntimeError) {
        println("[line ${e.line}] Runtime error: ${e.message}")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}