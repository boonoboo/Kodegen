package dk.cachet.kodegen.study

import dk.cachet.kodegen.study.application.CustomerService
import dk.cachet.kodegen.study.domain.Customer

class MockCustomerService : CustomerService {
    override suspend fun register(name: String) {
        return
    }

    override suspend fun getCustomerById(id: String): Customer {
        return Customer(id, "John Doe")
    }

}