package dk.cachet.kodegen.example.domain.oracle

import kotlinx.serialization.Serializable

@Serializable
class Answer(val response: String, val percentCertainty: Int)