package dk.cachet.rad.example.domain.oracle

interface AnswerRepository {
	fun getAnswer(question: String): Answer
}