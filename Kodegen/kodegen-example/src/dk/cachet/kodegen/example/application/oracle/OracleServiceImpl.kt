package dk.cachet.kodegen.example.application.oracle

import dk.cachet.kodegen.example.application.oracle.OracleService
import dk.cachet.kodegen.example.domain.oracle.Answer
import dk.cachet.kodegen.example.domain.oracle.AnswerRepositoryImpl
import kotlin.random.Random

class OracleServiceImpl (val answerRepository: AnswerRepositoryImpl) : OracleService {
    override suspend fun ask8Ball(message: String): String {
        return answerRepository.getAnswer()
    }

    override suspend fun askOracle(message: String): Answer {
        val answer = answerRepository.getAnswer()
        return Answer(answer, Random.nextInt(0,101))
    }
}