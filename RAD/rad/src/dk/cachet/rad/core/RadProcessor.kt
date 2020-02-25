package dk.cachet.rad.core

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import io.ktor.application.Application
import java.io.File
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
            generateServerMethod(methodElement, targetPackage)
            generateClientMethod(methodElement, targetPackage)
        }
        return false
    }

    fun generateServerMethod(methodElement: ExecutableElement, packageOfMethod: String) {
        val targetFunctionName = methodElement.simpleName.toString() + "ServerEndpoint"

        // Error checking
        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        if (generatedSourcesRoot.isEmpty()) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Can't find the target directory for generated Kotlin files."
            )
            return
        }

        // Create data class for request object
        // Set request object name
        val requestObjectName = ClassName(packageOfMethod, "${methodElement.simpleName}Request")

        // Create request object class builder
        val requestObjectBuilder = TypeSpec.classBuilder(requestObjectName)


        // Create request object constructor function
        val constructorFunctionBuilder = FunSpec.constructorBuilder()

        // For each parameter in methodElement, add parameter to request object
        // and add property to the request object
        methodElement.parameters.forEach {
            constructorFunctionBuilder.addParameter(it.simpleName.toString(), it.asType().asTypeName())
            requestObjectBuilder.addProperty(PropertySpec.builder("${it.simpleName}", it.asType().asTypeName())
                .initializer("${it.simpleName}")
                .build()
            )
        }

        // Set constructor and add data modifier
        requestObjectBuilder
            .primaryConstructor(constructorFunctionBuilder.build())
            .addModifiers(KModifier.DATA)

        // Create extension function "module" for Application
        val moduleFunctionBuilder = FunSpec.builder("module")
            .addModifiers(KModifier.PUBLIC)
            .receiver(Application::class)
            .returns(Unit::class)

        // TODO: If authentication is required from any service, install Authentication

        // TODO: if content negotiation is required, install content negotiation

        // Initiate routing
        val routingMemberName = MemberName("io.ktor.routing", "routing")
        moduleFunctionBuilder
            .addStatement("%M {", routingMemberName)


        // Create endpoint corresponding to the methodElement
        // 1st step: Route on HTTP Method (Only POST for now)
        val postMemberName = MemberName("io.ktor.routing", "post")
        val apiUrl = "/api/${methodElement.simpleName}"
        moduleFunctionBuilder
            .addStatement("%M(%S) {", postMemberName, apiUrl)

        // 2nd step: Get request object
        val callMemberName = MemberName("io.ktor.application", "call")
        val recieveMemberName = MemberName("io.ktor.request", "receive")
        moduleFunctionBuilder
            .addStatement("val request = %M.%M<$requestObjectName>()", callMemberName, recieveMemberName)

        // TODO: 3rd step: Get all parameters from request object
        methodElement.parameters.forEach {
            val parameterName = it.simpleName
            moduleFunctionBuilder
                .addStatement("val $parameterName = request.$parameterName")
        }

        // 4th step: Call the function
        var resultStatement = "val result = ${methodElement.simpleName}("
        methodElement.parameters.forEach { it ->
            resultStatement = "$resultStatement${it.simpleName}"
            if(methodElement.parameters.indexOf(it) != methodElement.parameters.lastIndex) {
                resultStatement = "$resultStatement, "
            }
        }
        resultStatement = "$resultStatement)"

        // TODO: Consider suspend if function call is asynchronous
        moduleFunctionBuilder
            .addStatement(resultStatement)

        // 5th step: Return the result
        val respondMemberName = MemberName("io.ktor.response", "respond")
        moduleFunctionBuilder
            .addStatement("%M.%M(result)", callMemberName, respondMemberName)
            .addStatement("}") // post end
            .addStatement("}") //routing end

        // Build the file
        val file = File(generatedSourcesRoot)
        file.mkdir()

        FileSpec.builder(packageOfMethod, "$targetFunctionName-AutoGenerated")
            .addType(requestObjectBuilder.build())
            .addFunction(moduleFunctionBuilder.build())
            .build()
            .writeTo(file)
    }

    fun generateClientMethod(methodElement: ExecutableElement, packageOfMethod: String) {
        val targetFunctionName = methodElement.simpleName.toString() + "ClientEndpoint"
        val apiURL = methodElement.simpleName.toString()

        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        if(generatedSourcesRoot.isEmpty()) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Can't find the target directory for generated Kotlin files.")
            return
        }

        // Create client function
        val clientFunctionBuilder = FunSpec.builder(targetFunctionName)
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

        // 3rd step: Close client
        clientFunctionBuilder.addStatement("client.close()")

        // 4th step: Return the result
        clientFunctionBuilder.addStatement("return response")

        // Build the file
        val file = File(generatedSourcesRoot)
        file.mkdir()

        FileSpec.builder(packageOfMethod, "$targetFunctionName-AutoGenerated")
            .addFunction(clientFunctionBuilder.build())
            .build()
            .writeTo(file)
    }
}
