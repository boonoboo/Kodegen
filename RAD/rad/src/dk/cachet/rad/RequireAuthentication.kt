package dk.cachet.rad

/**
 * Indicates that the caller of the method should be authenticated first.
 *
 * Used by [KtorProcessor] for code generation.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class RequireAuthentication