package dk.cachet.rad

/**
 * Indicates that the type is an application service.
 *
 * Used by [KtorProcessor] for code generation.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class ApplicationService