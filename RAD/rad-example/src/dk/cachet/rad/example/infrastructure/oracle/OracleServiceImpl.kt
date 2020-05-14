package dk.cachet.rad.example.infrastructure.oracle

import dk.cachet.rad.core.RadService
import dk.cachet.rad.example.application.oracle.OracleService
import dk.cachet.rad.example.domain.oracle.Answer
import dk.cachet.rad.example.domain.oracle.AnswerRepository
import kotlin.random.Random

@RadService
class OracleServiceImpl (val answerRepository: AnswerRepository) : OracleService {
    override suspend fun ask8Ball(message: String): String {
        return answerRepository.getAnswer()
    }

    override suspend fun askOracle(message: String): Answer {
        val answer = answerRepository.getAnswer()
        return Answer(answer, Random.nextInt(0,101))
    }
}