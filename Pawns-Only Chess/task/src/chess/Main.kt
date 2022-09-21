package chess

import kotlin.system.exitProcess

fun main() {
    println("Pawns-Only Chess")
    println("First Player's name:")
    val firstPlayerName: String = readln()
    println("Second Player's name:")
    val secondPlayerName: String = readln()

    var gameIsOn: Boolean = true
    val chessboard = mutableListOf(
        MutableList(8) { "   " },
        MutableList(8) { " B " },
        MutableList(8) { "   " },
        MutableList(8) { "   " },
        MutableList(8) { "   " },
        MutableList(8) { "   " },
        MutableList(8) { " W " },
        MutableList(8) { "   " }
    )
    var lastPlayerMoveDigitString: String = ""
    var enPassantTrigger: Boolean = false

    fun generateChessboard() {
        var x: Int = 0
        var lineNumber: Int = 8
        for (line in chessboard) {
            println("  " + "+---".repeat(8) + "+")
            print("$lineNumber |")
            for (space in line) {
                print("$space|")
            }
            print("\n")
            x += 1
            lineNumber -= 1
        }
        println("  " + "+---".repeat(8) + "+")
        println("    a   b   c   d   e   f   g   h")
    }

    fun regexCheckIfInputHasCorrectForm(moveUserInput: String): Boolean {
        val regexTester = """^[a-h][1-8][a-h][1-8]$""".toRegex()
        return regexTester.containsMatchIn(moveUserInput)
    }
    fun fromRegexCompatibleFormToDigitString(moveUserInput: String): String {
        fun letterCoordinatesToDigit(char: Char): String = when (char) {
            'a' -> "0"
            'b' -> "1"
            'c' -> "2"
            'd' -> "3"
            'e' -> "4"
            'f' -> "5"
            'g' -> "6"
            'h' -> "7"
            else -> "?"
        }

        var moveCoordinatesForm: String = ""
        moveCoordinatesForm += 8 - moveUserInput[1].digitToInt()
        moveCoordinatesForm += letterCoordinatesToDigit(moveUserInput[0])
        moveCoordinatesForm += 8 - moveUserInput[3].digitToInt()
        moveCoordinatesForm += letterCoordinatesToDigit(moveUserInput[2])
        //println(moveCoordinatesForm)
        return moveCoordinatesForm
    }
    fun moveLegalityCheck(player: String, moveInputForm: String = "",
                          moveCoordinatesForm: String, silentMode: Boolean = false): Boolean {
        val pawnForErrors: String = if (player == firstPlayerName) "white" else "black"
        val pawnForChessboard: String = if (player == firstPlayerName) " W " else " B "
        var errorMsg: String = ""
        var captureTrigger: Boolean = false
        //println(moveCoordinatesForm)

        fun moveInRangeValidator (moveCoordinatesForm: String): Boolean {
            return if (moveCoordinatesForm.length == 4) true else false
        }
        fun isStartSpaceOccupiedByCorrectPawn(moveCoordinatesForm: String): Boolean {
            return if (chessboard[moveCoordinatesForm[0].digitToInt()][moveCoordinatesForm[1].digitToInt()] == pawnForChessboard) {
                true
            } else {
                if (!silentMode) errorMsg = "No $pawnForErrors pawn at ${moveInputForm[0]}${moveInputForm[1]}"
                false
            }
        }
        fun isEndSpaceEmpty(moveCoordinatesForm: String): Boolean {
            return if (chessboard[moveCoordinatesForm[2].digitToInt()][moveCoordinatesForm[3].digitToInt()] == "   ") {
                true
            } else if (captureTrigger) {
                true
            } else {
                errorMsg = "Invalid Input"
                false
            }
        }
        fun isMoveGoingToDestroyEnemyPawn(player: String, moveCoordinatesForm: String): Boolean {
            return if (player == firstPlayerName && chessboard[moveCoordinatesForm[2].digitToInt()][moveCoordinatesForm[3].digitToInt()] == " B ") {
                true
            } else if (player == secondPlayerName && chessboard[moveCoordinatesForm[2].digitToInt()][moveCoordinatesForm[3].digitToInt()] == " W ") {
                true
            } else {
                false
            }
        }
        fun isEnPassantPossible(moveCoordinatesForm: String): Boolean {
            var tempChecksum: String = ""
            var tempNumber = 0
            //println(lastPlayerMoveDigitString)
            if (lastPlayerMoveDigitString != "") {
                for (i in moveCoordinatesForm) {
                    tempChecksum += moveCoordinatesForm[tempNumber] - lastPlayerMoveDigitString[tempNumber]
                    tempNumber += 1
                }
            }
            return if (tempChecksum == "21-10" || tempChecksum == "-2-110" || tempChecksum == "2-1-10" || tempChecksum == "-2110") {
                enPassantTrigger = true
                //println("EnPassantPossible")
                true
            } else {
                //println("EnPassantNotPossible")
                false
            }
        }
        fun isMoveLegalInTermsOfStayingInLine(player: String, moveCoordinatesForm: String): Boolean {
            return if (moveCoordinatesForm[1] == moveCoordinatesForm[3]) {
                true
            } else if (isMoveGoingToDestroyEnemyPawn(player, moveCoordinatesForm) &&
                (moveCoordinatesForm[1].digitToInt() - moveCoordinatesForm[3].digitToInt() == 1 ||
                moveCoordinatesForm[1].digitToInt() - moveCoordinatesForm[3].digitToInt() == -1) &&
                ((player == firstPlayerName && moveCoordinatesForm[0].digitToInt() - moveCoordinatesForm[2].digitToInt() == 1) ||
                (player == secondPlayerName && moveCoordinatesForm[0].digitToInt() - moveCoordinatesForm[2].digitToInt() == -1))) {
                //println("Testcond")
                captureTrigger = true
                true
            } else if (isEnPassantPossible(moveCoordinatesForm)){
                //println("EnPassant")
                true
            } else {
                errorMsg = "Invalid Input"
                false
            }
        }
        fun isMoveForward(player: String, moveCoordinatesForm: String): Boolean {
            return if (player == firstPlayerName && moveCoordinatesForm[0] > moveCoordinatesForm[2]) {
                true
            }
            else if (player == secondPlayerName && moveCoordinatesForm[0] < moveCoordinatesForm[2]) {
                true
            } else {
                // println("Move not forward")
                errorMsg = "Invalid Input"
                false
            }
        }
        fun isMovementForTwoSpaces(moveCoordinatesForm: String): Boolean {
            return moveCoordinatesForm[0] - moveCoordinatesForm[2] == 2 || moveCoordinatesForm[2] - moveCoordinatesForm[0] == 2
        }
        fun isMoveForThreeOrMoreSpaces (player: String, moveCoordinatesForm: String): Boolean {
            return if (player == firstPlayerName && moveCoordinatesForm[0] - moveCoordinatesForm[2] >= 3) {
                errorMsg = "Invalid Input"
                false
            } else if (player == secondPlayerName && moveCoordinatesForm[2] - moveCoordinatesForm[0] >= 3) {
                errorMsg = "Invalid Input"
                false
            } else {
                true
            }
        }
        fun isMovementForTwoSpacesPossible(moveCoordinatesForm: String): Boolean {
            return if (moveCoordinatesForm[0] == '6' || moveCoordinatesForm[0] == '1') {
                true
            } else {
                errorMsg = "Invalid Input"
                false
            }
        }
        fun runBasicChecks (player: String, moveCoordinatesForm: String): Boolean {
            return  (isStartSpaceOccupiedByCorrectPawn(moveCoordinatesForm) &&
                    isMoveForward(player, moveCoordinatesForm) &&
                    isMoveLegalInTermsOfStayingInLine(player, moveCoordinatesForm) &&
                    isMoveForThreeOrMoreSpaces(player, moveCoordinatesForm) &&
                    isEndSpaceEmpty(moveCoordinatesForm))
        }

        return if (!moveInRangeValidator(moveCoordinatesForm)) {
            false
        }
        else if (runBasicChecks(player, moveCoordinatesForm) && !isMovementForTwoSpaces(moveCoordinatesForm)) {
            true
        }
        else if (runBasicChecks(player, moveCoordinatesForm) && isMovementForTwoSpaces(moveCoordinatesForm) &&
            isMovementForTwoSpacesPossible(moveCoordinatesForm)) {
            true
        }
        else if (runBasicChecks(player, moveCoordinatesForm) && !isMovementForTwoSpaces(moveCoordinatesForm) &&
                isEnPassantPossible(moveCoordinatesForm)) {
            true
        }
        else {
            if (silentMode == false) println(errorMsg)
            false
        }
    }
    fun gameOverConditionTester(): Boolean {
        fun areThereAnyBlackPawnsLeft(): Boolean {
            var blackPawnCounter = 0
            for (list in chessboard) {
                for (element in list) {
                    if (element == " B ") {
                        blackPawnCounter += 1
                    }
                }
            }
            return if (blackPawnCounter == 0) false else true
        }
        fun areThereAnyWhitePawnsLeft(): Boolean {
            var whitePawnCounter = 0
            for (list in chessboard) {
                for (element in list) {
                    if (element == " W ") {
                        whitePawnCounter += 1
                    }
                }
            }
            return if (whitePawnCounter == 0) false else true
        }
        fun checkForStalemate(): Boolean {
            val resultListWhite = mutableListOf<Int>()
            val resultListBlack = mutableListOf<Int>()
            var isStalemate = false
            for (y in 1..6) {
                for (x in 7 downTo 0) {
                    if (chessboard[y][x] == " B " && x == 0) {
                        if (moveLegalityCheck(
                                player = secondPlayerName,
                                moveCoordinatesForm = "${y * 10 + x}${y * 10 + x + 10}",
                                silentMode = true) ||
                            moveLegalityCheck(
                                player = secondPlayerName,
                                moveCoordinatesForm = "$${y * 10 + x}${y * 10 + x + 11}",
                                silentMode = true
                            )
                        ) {resultListBlack.add(1)
                            }
                        else {resultListBlack.add(0)
                            }
                    }
                    else if (chessboard[y][x] == " B " && x == 7) {
                        if (moveLegalityCheck(
                                player = secondPlayerName,
                                moveCoordinatesForm = "${y * 10 + x}${y * 10 + x + 10}",
                                silentMode = true) ||
                            moveLegalityCheck(
                                player = secondPlayerName,
                                moveCoordinatesForm = "${y * 10 + x}${y * 10 + x + 9}",
                                silentMode = true
                            )
                        ) {
                            resultListBlack.add(1)
                        }
                        else {
                            resultListBlack.add(0)
                        }
                    }
                    else if (chessboard[y][x] == " B ") {
                        if (moveLegalityCheck(
                                player = secondPlayerName,
                                moveCoordinatesForm = "${y * 10 + x}${y * 10 + x + 10}",
                                silentMode = true) ||
                            moveLegalityCheck(
                                player = secondPlayerName,
                                moveCoordinatesForm = "${y * 10 + x}${y * 10 + x + 11}",
                                silentMode = true
                            ) ||
                            moveLegalityCheck(
                                player = secondPlayerName,
                                moveCoordinatesForm = "${y * 10 + x}${y * 10 + x + 9}",
                                silentMode = true
                            )
                        ) {
                            resultListBlack.add(1)
                        }
                        else {
                            resultListBlack.add(0)
                        }
                    }
                    else if (chessboard[y][x] == " W " && x == 0) {
                        if (moveLegalityCheck(
                                player = firstPlayerName,
                                moveCoordinatesForm = "${y * 10 + x}${y * 10 + x - 10}",
                                silentMode = true) ||
                            moveLegalityCheck(
                                player = firstPlayerName,
                                moveCoordinatesForm = "${y * 10 + x}${y * 10 + x - 9}",
                                silentMode = true
                            )
                        ) {
                            resultListWhite.add(1)
                        }
                        else {
                            resultListWhite.add(0)
                        }
                    }
                    else if (chessboard[y][x] == " W " && x == 7) {
                        if (moveLegalityCheck(
                                player = firstPlayerName,
                                moveCoordinatesForm = "${y * 10 + x}${y * 10 + x - 10}",
                                silentMode = true) ||
                            moveLegalityCheck(
                                player = firstPlayerName,
                                moveCoordinatesForm = "${y * 10 + x}${y * 10 + x - 11}",
                                silentMode = true
                            )
                        ) {
                            resultListWhite.add(1)
                        }
                        else {
                            resultListWhite.add(0)
                        }
                    }
                    else if (chessboard[y][x] == " W ") {
                        if (moveLegalityCheck(
                                player = firstPlayerName,
                                moveCoordinatesForm = "${y * 10 + x}${y * 10 + x - 10}",
                                silentMode = true) ||
                            moveLegalityCheck(
                                player = firstPlayerName,
                                moveCoordinatesForm = "${y * 10 + x}${y * 10 + x - 11}",
                                silentMode = true
                            ) ||
                            moveLegalityCheck(
                                player = firstPlayerName,
                                moveCoordinatesForm = "${y * 10 + x}${y * 10 + x - 9}",
                                silentMode = true
                            )
                        ) {
                            resultListWhite.add(1)
                        }
                        else {
                            resultListWhite.add(0)
                        }
                    }
                }
            }
            if (resultListWhite.contains(1) && resultListBlack.contains(1)) {
                isStalemate = false
            } else if (!resultListWhite.contains(1)) {
                //println("Black Wins!")
                isStalemate = true
            } else if (!resultListBlack.contains(1)) {
                //println("White Wins!")
                isStalemate = true
            }
            return isStalemate
        }

        val firstRow = mutableListOf<String>()
        val lastRow = mutableListOf<String>()
        for (y in 0..7) {
            firstRow.add(chessboard[0][y])
            lastRow.add(chessboard[7][y])
        }
        //println(firstRow)
        //println(lastRow)
        if (firstRow.contains(" W ") || !areThereAnyBlackPawnsLeft()) {
            println("White Wins!")
            return true
        } else if (lastRow.contains(" B ") || !areThereAnyWhitePawnsLeft()) {
            println("Black Wins!")
            return true
        } else if (checkForStalemate()) {
            println("Stalemate!")
            return true
        } else {
            return false
        }
    }
    fun updateChessboard(player: String, moveCoordinatesForm: String) {
        val pawn: String = if (player == firstPlayerName) " W " else " B "

        chessboard[moveCoordinatesForm[0].digitToInt()][moveCoordinatesForm[1].digitToInt()] = "   "
        chessboard[moveCoordinatesForm[2].digitToInt()][moveCoordinatesForm[3].digitToInt()] = pawn
        if (enPassantTrigger) {
            chessboard[lastPlayerMoveDigitString[2].digitToInt()][lastPlayerMoveDigitString[3].digitToInt()] = "   "
        }
        generateChessboard()
    }

    fun playerTurn(player: String) {
        println("$player's turn:")
        val move: String = readln()
        if (move == "exit") {
            println("Bye!")
            gameIsOn = false
            exitProcess(0)
        } else {
            regexCheckIfInputHasCorrectForm(move)
            val moveCoordinatesForm: String = fromRegexCompatibleFormToDigitString(move)
            //println(lastPlayerMoveDigitString)
            if (moveLegalityCheck(player = player, moveInputForm = move, moveCoordinatesForm = moveCoordinatesForm)) {
                updateChessboard(player, moveCoordinatesForm)
                lastPlayerMoveDigitString = fromRegexCompatibleFormToDigitString(move)
                if (gameOverConditionTester()) {
                    println("Bye!")
                    gameIsOn = false
                    exitProcess(0)
                }
            } else {
                playerTurn(player)
            }
        }
    }
    generateChessboard()
    while (gameIsOn) {
        playerTurn(firstPlayerName)
        playerTurn(secondPlayerName)
    }
}

