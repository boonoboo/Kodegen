package dk.cachet.kodegen

/**
 * Indicates that the caller of the method should be authenticated first.
 *
 * Used by [KodegenProcessor] for code generation.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class RequireAuthentication