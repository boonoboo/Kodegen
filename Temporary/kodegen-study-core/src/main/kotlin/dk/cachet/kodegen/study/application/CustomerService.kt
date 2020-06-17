package dk.cachet.kodegen.study.application

import dk.cachet.kodegen.ApplicationService
import dk.cachet.kodegen.RequireAuthentication
import dk.cachet.kodegen.study.domain.Customer

@ApplicationService
interface CustomerService {
    @RequireAuthentication
    suspend fun register(name: String)

    suspend fun getCustomerById(id: String): Customer
}