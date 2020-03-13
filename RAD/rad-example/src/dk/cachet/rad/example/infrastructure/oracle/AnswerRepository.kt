package dk.cachet.rad.example.infrastructure.oracle

import dk.cachet.rad.example.domain.oracle.Answer
import dk.cachet.rad.example.domain.oracle.AnswerRepository
import kotlin.random.Random

class AnswerRepository : AnswerRepository {
	override fun getAnswer(question: String): Answer
	{
		return Answer("Sure", Random.nextInt(0,101))
	}
}