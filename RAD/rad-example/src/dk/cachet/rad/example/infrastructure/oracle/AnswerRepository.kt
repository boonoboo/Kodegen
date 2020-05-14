package dk.cachet.rad.example.infrastructure.oracle

import dk.cachet.rad.example.domain.oracle.AnswerRepository

class AnswerRepository : AnswerRepository {
	companion object {
		val answers = listOf(
			"As I see it, yes", "Ask again later", "Better not tell you now", "Cannot predict now.",
			"Concentrate and ask again.", "Don’t count on it.", "It is certain.", "It is decidedly so.",
			"Most likely.", "My reply is no.", "My sources say no.", "Outlook not so good.",
			"Outlook good.", "Reply hazy, try again.", "Signs point to yes.", "Very doubtful.", "Without a doubt.",
			"Yes.", "Yes – definitely.", "You may rely on it."
		)
	}
	override fun getAnswer(): String
	{
		return answers.shuffled().first()
	}
}