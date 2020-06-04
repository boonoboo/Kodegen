package dk.cachet.rad.example.application.oracle

import dk.cachet.rad.ApplicationService
import dk.cachet.rad.example.domain.oracle.Answer

@ApplicationService
interface OracleService {
    suspend fun ask8Ball(message: String): String
    suspend fun askOracle(message: String): Answer
}