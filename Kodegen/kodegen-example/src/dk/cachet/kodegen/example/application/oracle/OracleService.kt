package dk.cachet.kodegen.example.application.oracle

import dk.cachet.kodegen.ApplicationService
import dk.cachet.kodegen.example.domain.oracle.Answer

@ApplicationService
interface OracleService {
    suspend fun ask8Ball(message: String): String
    suspend fun askOracle(message: String): Answer
}