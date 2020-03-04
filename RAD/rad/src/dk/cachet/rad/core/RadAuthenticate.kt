package dk.cachet.rad.core

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class RadAuthenticate(val roles: Array<String>)