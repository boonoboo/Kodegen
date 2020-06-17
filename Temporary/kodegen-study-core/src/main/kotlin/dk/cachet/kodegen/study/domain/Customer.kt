package dk.cachet.kodegen.study.domain

import kotlinx.serialization.Serializable

@Serializable
class Customer(val id: String, val name: String) {
    // ..domain logic
}