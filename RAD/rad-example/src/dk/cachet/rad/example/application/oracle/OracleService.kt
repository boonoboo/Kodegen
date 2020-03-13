package dk.cachet.rad.example.application.oracle

interface OracleService {
    fun ask8Ball(message: String): String

    fun askOracle(message: String): String
}