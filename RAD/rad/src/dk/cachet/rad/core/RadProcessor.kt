package dk.cachet.rad.core

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import io.ktor.application.Application
import java.io.File
import java.io.Serializable
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedOptions(RadProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedAnnotationTypes("dk.cachet.rad.core.RadMethod")
class RadProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    /**
     * Processes all elements annotated with "RadMethod"
     */
    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        // TODO: Restructure code generation so that end result is:
        // TODO: For each package, generate Application.module
        // TODO: For each module, add a single routing
        // TODO: For each RadMethod, add a post containing the RAD logic to this routing

        // TODO: Consider methods for splitting routing / modules, e.g. if authentication is necessary


        // SERVER API GENERATION
        roundEnv.getElementsAnnotatedWith(RadMethod::class.java).forEach {
            val methodElement = it as ExecutableElement

            // If annotated element is not a function, do nothing
            if (methodElement.kind != ElementKind.METHOD) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Error while processing element: $methodElement. Annotation \"RadMethod\" can only be applied to functions"
                )
                return false
            }

            val targetPackage = processingEnv.elementUtils.getPackageOf(methodElement).toString()
            generateRequestObject(methodElement, targetPackage)
            generateServerMethod(methodElement, targetPackage)
            generateClientMethod(methodElement, targetPackage)
        }
        return false
    }

    fun generateRequestObject(methodElement: ExecutableElement, packageOfMethod: String) {
        // Error checking
        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        if (generatedSourcesRoot.isEmpty()) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Can't find the target directory for generated Kotlin files."
            )
            return
        }
        // If method takes no parameters, the request object is not necessary
        if(methodElement.parameters.size == 0) return;

        // Create data class for request object
        // Set request object name
        val requestObjectName = ClassName(packageOfMethod, "${methodElement.simpleName}Request")

        // Create request object class builder
        val requestObjectBuilder = TypeSpec.classBuilder(requestObjectName)

        // Add serializable annotation
        requestObjectBuilder.addAnnotation(kotlinx.serialization.Serializable::class)


        // Create request object constructor function
        val constructorFunctionBuilder = FunSpec.constructorBuilder()

        // For each parameter in methodElement, add parameter to request object
        // and add property to the request object
        // TODO: Kotlin Poet is unable to correctly identify if parameter.asType().asTypeName() should be the Java or Kotlin type
        methodElement.parameters.forEach { parameter ->
            constructorFunctionBuilder.addParameter(parameter.simpleName.toString(), parameter.asType().asTypeName())
            requestObjectBuilder.addProperty(
                PropertySpec.builder("${parameter.simpleName}", parameter.asType().asTypeName())
                    .initializer("${parameter.simpleName}")
                    .build()
            )
        }

        // Set constructor and add data modifier
        requestObjectBuilder
            .primaryConstructor(constructorFunctionBuilder.build())
        if (methodElement.parameters.count() > 0) requestObjectBuilder.addModifiers(KModifier.DATA)

        // Build the file
        val file = File(generatedSourcesRoot)
        file.mkdir()

        FileSpec.builder("$packageOfMethod.${methodElement.simpleName}", "${methodElement.simpleName}Request")
            .addType(requestObjectBuilder.build())
            .build()
            .writeTo(file)
    }

    fun generateServerMethod(methodElement: ExecutableElement, packageOfMethod: String) {
        // Error checking
        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        if (generatedSourcesRoot.isEmpty()) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Can't find the target directory for generated Kotlin files."
            )
            return
        }

        // Create extension function "module" for Application
        val moduleFunctionBuilder = FunSpec.builder("${methodElement.simpleName}Module")
            .addModifiers(KModifier.PUBLIC)
            .receiver(Application::class)
            .returns(Unit::class)

        // TODO: If authentication is required from any service, install Authentication

        // Install content negotiation
        /*
        val installMemberName = MemberName("io.ktor.application", "install")
        val contentNegotiationMemberName = MemberName("io.ktor.features", "ContentNegotiation")
        val serializationMemberName = MemberName("io.ktor.serialization", "serialization")
        moduleFunctionBuilder
            .beginControlFlow("%M(%M)", installMemberName, contentNegotiationMemberName)
            .addStatement("%M()", serializationMemberName)
            .endControlFlow()
        */
        // Initiate routing
        val routingMemberName = MemberName("io.ktor.routing", "routing")
        moduleFunctionBuilder
            .beginControlFlow("%M {", routingMemberName)


        // Create endpoint corresponding to the methodElement
        // 1st step: Route on HTTP Method (Only POST for now)
        val postMemberName = MemberName("io.ktor.routing", "post")

        // TODO / PROTOTYPE: API corresponds to package name
        // TODO: Consider alternatives for generating API but avoiding conflicts
        // TODO: e.g. require unique function names, randomly generated API UUID, single URI endpoint only
        val apiUrl = "/radApi/${packageOfMethod.replace(".", "/")}/${methodElement.simpleName}"
        moduleFunctionBuilder
            .beginControlFlow("%M(%S) {", postMemberName, apiUrl)

        // If the method takes parameters, attempt to receive the request object
        // 2nd step: Get request object
        val callMemberName = MemberName("io.ktor.application", "call")
        val recieveMemberName = MemberName("io.ktor.request", "receive")
        val requestObjectName =
            ClassName("$packageOfMethod.${methodElement.simpleName}", "${methodElement.simpleName}Request")

        if(methodElement.parameters.size != 0) {
            moduleFunctionBuilder
                .addStatement("val request = %M.%M<$requestObjectName>()", callMemberName, recieveMemberName)

            // 3rd step: Get all parameters from request object
            methodElement.parameters.forEach {
                val parameterName = it.simpleName
                moduleFunctionBuilder
                    .addStatement("val $parameterName = request.$parameterName")
            }
        }

        // 4th step: Call the function
        val functionMemberName = MemberName(packageOfMethod, methodElement.simpleName.toString())
        var resultStatement = "val result = %M("
        methodElement.parameters.forEach { it ->
            resultStatement = "$resultStatement${it.simpleName}"
            if (methodElement.parameters.indexOf(it) != methodElement.parameters.lastIndex) {
                resultStatement = "$resultStatement, "
            }
        }
        resultStatement = "$resultStatement)"

        // TODO: Consider suspension function calls
        moduleFunctionBuilder
            .addStatement(resultStatement, functionMemberName)

        // 5th step: Serialize and return the result
        val respondMemberName = MemberName("io.ktor.response", "respond")
        moduleFunctionBuilder
            .addStatement("%M.%M(result)", callMemberName, respondMemberName)
            .endControlFlow() // post end
            .endControlFlow() //routing end

        // Build the file
        val file = File(generatedSourcesRoot)
        file.mkdir()

        FileSpec.builder("$packageOfMethod.${methodElement.simpleName}", "${methodElement.simpleName}ServerEndpoint")
            .addFunction(moduleFunctionBuilder.build())
            .build()
            .writeTo(file)
    }

    fun generateClientMethod(methodElement: ExecutableElement, packageOfMethod: String) {
        val apiURL = methodElement.simpleName.toString()

        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        if (generatedSourcesRoot.isEmpty()) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Can't find the target directory for generated Kotlin files."
            )
            return
        }

        // Create client function
        val clientFunctionBuilder = FunSpec.builder("${methodElement.simpleName}ClientEndpoint")
            .addModifiers(KModifier.PUBLIC)
            .addModifiers(KModifier.SUSPEND)
            .returns(methodElement.returnType.asTypeName())

        // Create endpoint corresponding to the methodElement
        // 1st step: Open client
        val httpClientMemberName = MemberName("io.ktor.client", "HttpClient")
        clientFunctionBuilder.addStatement("val client = %M()", httpClientMemberName)

        // 2nd step: Make request
        val postMemberName = MemberName("io.ktor.client.request", "post")
        val returnType = methodElement.returnType.asTypeName()
        clientFunctionBuilder.addStatement("val response = client.%M<$returnType>(%S)", postMemberName, apiURL)

        // TODO: Add parameters to request


        // 3rd step: Close client
        clientFunctionBuilder.addStatement("client.close()")

        // 4th step: Return the result
        clientFunctionBuilder.addStatement("return response")

        // Build the file
        val file = File(generatedSourcesRoot)
        file.mkdir()

        FileSpec.builder("$packageOfMethod.${methodElement.simpleName}", "${methodElement.simpleName}ClientEndpoint")
            .addFunction(clientFunctionBuilder.build())
            .build()
            .writeTo(file)
    }
}