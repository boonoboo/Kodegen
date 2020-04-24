package dk.cachet.rad.core

// This object exists to serve as the target of an extension function configureRad
// Building a project creates another extension function configureRad that should shadow this one
object RadConfiguration {

}

// Placeholder function for configuring Rad
// This function should be shadowed by a generated function configureRad
fun RadConfiguration.configureRad() {
	throw IllegalStateException("Rad Configuration was not generated.")
}