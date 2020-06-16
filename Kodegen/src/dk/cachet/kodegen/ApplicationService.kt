package dk.cachet.kodegen

/**
 * Indicates that the type is an application service.
 *
 * Used by [KodegenProcessor] for code generation.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class ApplicationService