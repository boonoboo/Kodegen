package dk.cachet.rad.core

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec

import io.ktor.application.Application
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@KotlinPoetMetadataPreview
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
		// Check if generated source root exists, and if not, return with an error message
		val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
		if (generatedSourcesRoot.isEmpty()) {
			processingEnv.messager.printMessage(
				Diagnostic.Kind.ERROR,
				"Can't find the target directory for generated Kotlin files."
			)
			return false
		}

		// TODO: Restructure code generation so that end result is:
		// TODO: For each service, generate Application.serviceNameModule
		// TODO: For each module, add a single routing
		// TODO: For each method in the service, add a post containing the RAD logic for this routing

		// TODO: Consider methods for splitting routing / modules, e.g. if authentication is necessary

		roundEnv.getElementsAnnotatedWith(RadService::class.java).forEach {
			val serviceElement = it as TypeElement
			// If annotated element is not a class, skip past it
			if (serviceElement.kind != ElementKind.CLASS) {
				processingEnv.messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Error while processing element: $serviceElement. Annotation @RadService can only be applied to classes."
				)
				return false
			}

			// Get the package of the service, which will be used when generating the associated API
			// TODO: Consider if target package should be specified somehow, e.g. in the annotation
			// todo: e.g. @RadPackage("org.example.api"), or if it should be inferred from the service,
			// todo: e.g. org.example.domain -> org.example.domain.rad
			val servicePackage = processingEnv.elementUtils.getPackageOf(serviceElement).toString()

			// Generate a KotlinPoet TypeSpec from the TypeClass, allowing Kotlin-specific information to be derived
			// This is necessary because using javax Annotation Processing and Kotlin Poet to derive parameter
			// information for methods may result in use of Java types instead of Kotlin types,
			// e.g. using java.lang.String instead of kotlin.String
			val serviceTypeSpec = serviceElement.toTypeSpec()

			// Generate the request object, module and client
			generateRequestObjects(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			generateModule(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			generateClient(serviceTypeSpec, servicePackage, generatedSourcesRoot)
		}
		return false
	}

	private fun generateRequestObjects(serviceTypeSpec: TypeSpec, servicePackage: String, generatedSourcesRoot: String) {
		// TODO: Consider what target package should be
		val targetPackage = "$servicePackage.rad"

		// Iterate through the methods of the service, generating a request object for each
		serviceTypeSpec.funSpecs.forEach { funSpec ->
			// TODO: Consider generating serializers for polymorphic / complex objects
			// todo: where the @Serializable tag does not suffice

			// If method takes no parameters, the request object is not necessary
			if(funSpec.parameters.isEmpty()) return@forEach

			// Define name and package of generated class
			val className = ClassName(targetPackage, "${funSpec.name}Request")

			// Create class builder and set data modifier
			val classBuilder = TypeSpec.classBuilder(className)
				.addModifiers(KModifier.DATA)

			// Add serializable annotation
			classBuilder.addAnnotation(kotlinx.serialization.Serializable::class)

			// Create constructor builder
			val constructorBuilder = FunSpec.constructorBuilder()


			funSpec.parameters.forEach { parameter ->
				constructorBuilder.addParameter(parameter.name, parameter.type)
				classBuilder.addProperty(
					PropertySpec.builder(parameter.name, parameter.type)
						.initializer(parameter.name)
						.build()
				)
			}

			// Set constructor and build the class
			classBuilder
				.primaryConstructor(constructorBuilder.build())

			// Build the file
			val file = File(generatedSourcesRoot)
			file.mkdir()

			FileSpec.builder(targetPackage, "${funSpec.name}Request")
				.addType(classBuilder.build())
				.build()
				.writeTo(file)
		}
	}

	private fun generateModule(serviceTypeSpec: TypeSpec, servicePackage: String, generatedSourcesRoot: String) {
		val targetPackage = "$servicePackage.rad"

		// Create module extension function for Application class
		// TODO: Consider if one module for all services should be generated or one module per service
		val moduleFunctionBuilder = FunSpec.builder("${serviceTypeSpec.name}Module")
			.addModifiers(KModifier.PUBLIC)
			.receiver(Application::class)
			.returns(Unit::class)

		// TODO: If authentication is required from any service, install Authentication

		// TODO: If content negotiation is necessary, install content negotiation
		/*
		val installMemberName = MemberName("io.ktor.application", "install")
		val contentNegotiationMemberName = MemberName("io.ktor.features", "ContentNegotiation")
		val serializationMemberName = MemberName("io.ktor.serialization", "serialization")
		moduleFunctionBuilder
			.beginControlFlow("%M(%M)", installMemberName, contentNegotiationMemberName)
			.addStatement("%M()", serializationMemberName)
			.endControlFlow()
		*/


		// Create service for calling service functions
		// TODO: Consider cases with no service class / static class / class with parameterized constructor
		val service = MemberName(servicePackage, serviceTypeSpec.name!!)
		moduleFunctionBuilder.addStatement("val service = %M()", service)

		// Initiate routing
		val routingMemberName = MemberName("io.ktor.routing", "routing")
		moduleFunctionBuilder
			.beginControlFlow("%M {", routingMemberName)


		// Iterate through functions in service, generating a route for each
		serviceTypeSpec.funSpecs.forEach { funSpec ->
			// Create endpoint corresponding to the funSpec
			// 1st step: Route on HTTP Method (POST)
			val post = MemberName("io.ktor.routing", "post")

			// TODO: Consider alternatives for URL but avoiding conflicts, e.g.:
			//  require unique function names
			//  use a randomly generated UUID for each function
			//  use a single URI endpoint only and place the function invocation in the body (RPC style)

			val apiUrl = "/radApi/${servicePackage.replace(".", "/")}/${funSpec.name}"
			moduleFunctionBuilder
				.beginControlFlow("%M(%S) {", post, apiUrl)

			// If the method takes parameters, attempt to receive the request object
			// 2nd step: Get request object
			val call = MemberName("io.ktor.application", "call")

			if(funSpec.parameters.isNotEmpty()) {
				// TODO: Currently no static check that ClassName is the same as in generated request object
				//  The ClassName Reference should be reused
				val requestClassName = ClassName("$servicePackage.rad", "${funSpec.name}Request")
				val receive = MemberName("io.ktor.request", "receive")

				moduleFunctionBuilder
					.addStatement("val request = %M.%M<$requestClassName>()", call, receive)

				// 3rd step: Get all parameters from request object
				funSpec.parameters.forEach { parameter ->
					moduleFunctionBuilder
						.addStatement("val ${parameter.name} = request.${parameter.name}")
				}
			}

			// 4th step: Call the function
			var resultStatement = "service.${funSpec.name}("
			funSpec.parameters.forEach { parameter ->
				resultStatement = "$resultStatement${parameter.name}"
				if (funSpec.parameters.indexOf(parameter) != funSpec.parameters.lastIndex) {
					resultStatement = "$resultStatement, "
				}
			}
			resultStatement = "$resultStatement)"

			// If the function to call is a suspended function, add an await to the function call
			if(funSpec.modifiers.contains(KModifier.SUSPEND)) {
				resultStatement = "await $resultStatement"
			}
			resultStatement = "val result = $resultStatement"

			moduleFunctionBuilder
				.addStatement(resultStatement)

			// 5th step: Serialize and return the result
			val respondMemberName = MemberName("io.ktor.response", "respond")
			moduleFunctionBuilder
				.addStatement("%M.%M(result)", call, respondMemberName)
				.endControlFlow()
		}

		// End routing
		moduleFunctionBuilder
			.endControlFlow()

		// Build the file
		val file = File(generatedSourcesRoot)
		file.mkdir()

		FileSpec.builder(targetPackage, "${serviceTypeSpec.name!!}Module")
			.addFunction(moduleFunctionBuilder.build())
			.build()
			.writeTo(file)
	}

	private fun generateClient(serviceTypeSpec: TypeSpec, servicePackage: String, generatedSourcesRoot: String) {
		val targetPackage = "$servicePackage.rad"

		// Prepare FileSpec builder
		val fileSpecBuilder = FileSpec.builder(targetPackage, "${serviceTypeSpec.name!!}Client")

		// Iterate through functions, adding a client function for each
		serviceTypeSpec.funSpecs.forEach { funSpec ->
			val apiUrl = "/radApi/${servicePackage.replace(".", "/")}/${funSpec.name}"

			// Create client function
			val clientFunctionBuilder = FunSpec.builder("${funSpec.name}")
				.addModifiers(KModifier.PUBLIC)
				.addModifiers(KModifier.SUSPEND)
				.returns(funSpec.returnType!!)

			// For each parameter of the function, add them to the client function
			funSpec.parameters.forEach { parameter ->
				clientFunctionBuilder.addParameter(parameter)
			}

			// Create endpoint corresponding to the function
			// 1st step: Open a HTTP client
			val httpClient = MemberName("io.ktor.client", "HttpClient")
			clientFunctionBuilder
				.addStatement("val client = %M()", httpClient)



			// 3rd step: Make request
			val post = MemberName("io.ktor.client.request", "post")
			val url = MemberName("io.ktor.client.request", "url")
			val returnType = funSpec.returnType!!

			clientFunctionBuilder
				.beginControlFlow("val response = client.%M<$returnType>", post)
				.addStatement("%M(%S)", url, apiUrl)

			// If the function call has any parameters, serialize a request object and add it
			if(funSpec.parameters.isNotEmpty())
			{
				var bodyStatement = "body = ${funSpec.name}Request("

				funSpec.parameters.forEach { parameter ->
					bodyStatement = "$bodyStatement${parameter.name} = ${parameter.name}"
					if (funSpec.parameters.indexOf(parameter) != funSpec.parameters.lastIndex) {
						bodyStatement = "$bodyStatement, "
					}
				}
				bodyStatement = "$bodyStatement)"
				clientFunctionBuilder
					.addStatement(bodyStatement)
			}

			clientFunctionBuilder
				.endControlFlow()

			// 4th: step: Close the client and return the result
			clientFunctionBuilder
				.addStatement("client.close()")
				.addStatement("return response")

			// 5th step: Add function to FileSpec builder
			fileSpecBuilder.addFunction(clientFunctionBuilder.build())
		}

		// Build the file
		val file = File(generatedSourcesRoot)
		file.mkdir()

		fileSpecBuilder
			.build()
			.writeTo(file)
	}
}