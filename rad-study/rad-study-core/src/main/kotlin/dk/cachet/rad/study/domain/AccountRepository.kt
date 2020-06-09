package dk.cachet.rad.study.domain

interface AccountRepository {
    // Returns the account associated with a given card, if any
    fun getAccountAssociatedWithCard(card: Card): Account

    // Adds the amount to the balance (which may be negative) of the account and returns the new balance
    fun updateBalance(account: Account, amount: Int): Int
}