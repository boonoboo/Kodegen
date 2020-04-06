package dk.cachet.rad.core

import com.google.auto.service.AutoService
import com.google.gson.reflect.TypeToken
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec

import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import org.koin.core.module.Module
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@KotlinPoetMetadataPreview
@AutoService(Processor::class)
//@SupportedSourceVersion(SourceVersion.RELEASE_13)
//@SupportedOptions(RadProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
//@SupportedAnnotationTypes("dk.cachet.rad.core.RadService")
class RadProcessor : AbstractProcessor() {
	companion object {
		const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
	}

	override fun getSupportedAnnotationTypes(): Set<String> {
		return setOf("dk.cachet.rad.core.RadService", "dk.cachet.rad.core.RadAuthenticate")
	}

	override fun getSupportedSourceVersion(): SourceVersion {
		return SourceVersion.latestSupported()
	}

	override fun getSupportedOptions(): Set<String> {
		return setOf(KAPT_KOTLIN_GENERATED_OPTION_NAME)
	}

	/**
	 * Processes all elements annotated with "RadService"
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

		val koinModulesList: MutableSet<MemberName> = mutableSetOf()

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
			// An ElementsClassInspector is used to assist in analyzing the classes when generating the TypeSpec
			val classInspector = ElementsClassInspector.create(processingEnv.elementUtils, processingEnv.typeUtils)
			val serviceTypeSpec = serviceElement.toTypeSpec(classInspector)

			// Generate the request object, module and client
			generateRequestObjects(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			generateModule(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			generateClient(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			koinModulesList.add(MemberName("$servicePackage.rad", "${serviceElement.simpleName.toString()}Client"))
		}

		// Avoid overwriting the configuration content if process is called again
		// with no annotated elements
		if(koinModulesList.size != 0) {
			// Write file with configuration content
			val file = File(generatedSourcesRoot)
			file.mkdir()

			// Configuration function
			val koinModule = MemberName("org.koin.dsl", "module")

			val configureRadBuilder = FunSpec.builder("configureRad")
				.receiver(RadConfiguration::class)
				.beginControlFlow("val module = %M", koinModule)

			koinModulesList.forEach { serviceMemberName ->
				configureRadBuilder.addStatement("single { %M() }", serviceMemberName)
			}

			configureRadBuilder.endControlFlow()

			val startKoin = MemberName("org.koin.core.context", "startKoin")

			configureRadBuilder
				.beginControlFlow("%M", startKoin)
				.addStatement("modules(module)")
				.endControlFlow()

			FileSpec.builder("dk.cachet.rad.core", "RadConfiguration")
				.addFunction(configureRadBuilder.build())
				.build()
				.writeTo(file)
		}
		return false
	}

	private fun generateRequestObjects(serviceTypeSpec: TypeSpec, servicePackage: String, generatedSourcesRoot: String) {
		val targetPackage = "$servicePackage.rad"

		// Iterate through all function of the service that has at least one parameter
		// For each function, generate a request object
		serviceTypeSpec.funSpecs.filter { funSpec -> funSpec.parameters.isNotEmpty() }.forEach { funSpec ->
			// Define name and package of generated class
			val serviceClassName = ClassName(targetPackage, "${funSpec.name}Request")

			// Create class builder and set data modifier
			val classBuilder = TypeSpec.classBuilder(serviceClassName)
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

			// TODO
			//   This has to be moved elsewhere so that both parameter types
			//   and returned types have serializers generated
			// Generate serializers for each non-primitive type used
			/*
			funSpec.parameters.forEach serializationLoop@{ parameter ->
				val type = parameter.type

				// Check if extension function already exists or is unnecessary
				// Current method is a temporary solution that makes an effort to not generate unnecessary serializers
				// by ignoring types that are in the kotlin packages (primitives etc.)
				// However, the method is flawed, and a better solution should be used
				val className = ClassName.bestGuess(type.toString())
				val classNameCompanion = className.nestedClass("Companion")
				val defaultJsonMemberName = MemberName("dk.cachet.rad.serialization", "createDefaultJSON")
				val serializerMemberName = MemberName("kotlinx.serialization", "serializer")

				if(className.packageName == ("kotlin")) {
					return@serializationLoop
				}

				//val jsonPropertyBuilder = PropertySpec.builder("JSON", Json::class)
				//	.initializer("%M()", defaultJsonMemberName)

				val fromJsonBuilder = FunSpec.builder("fromJson")
					.receiver(classNameCompanion)
					.returns(type)
					.addParameter("json", String::class)
					.addStatement("val JSON = %M()", defaultJsonMemberName)
					.addStatement("return JSON.parse(%M(), json)", serializerMemberName)

				val toJsonBuilder = FunSpec.builder("toJson")
					.receiver(type)
					.returns(String::class)
					.addStatement("val JSON = %M()", defaultJsonMemberName)
					.addStatement("return JSON.stringify($type.serializer(), this)")

				// Build the file
				val file = File(generatedSourcesRoot)
				file.mkdir()

				FileSpec.builder(targetPackage, "${type}Serialization")
					//.addProperty(jsonPropertyBuilder.build())
					.addFunction(fromJsonBuilder.build())
					.addFunction(toJsonBuilder.build())
					.build()
					.writeTo(file)
			}
			*/

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
		val moduleFunctionBuilder = FunSpec.builder("${serviceTypeSpec.name}Module")
			.addModifiers(KModifier.PUBLIC)
			.receiver(Application::class)
			.returns(Unit::class)

		// Create service for calling service functions using lazy injection
		val service = MemberName(servicePackage, serviceTypeSpec.name!!)
		val inject = MemberName("org.koin.ktor.ext", "inject")
		moduleFunctionBuilder.addStatement("val service: %M by %M()", service, inject)

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
			val apiUrl = "/radApi/${funSpec.name}"
			moduleFunctionBuilder
				.beginControlFlow("%M(%S) {", post, apiUrl)

			// If the method takes parameters, attempt to receive the request object
			// 2nd step: Get request object
			val call = MemberName("io.ktor.application", "call")

			if(funSpec.parameters.isNotEmpty()) {
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
			funSpec.parameters.forEachIndexed { index, parameter ->
				resultStatement = "$resultStatement${parameter.name}"
				if (index != funSpec.parameters.lastIndex) {
					resultStatement = "$resultStatement, "
				}
			}
			resultStatement = "$resultStatement)"

			resultStatement = "val result = $resultStatement"

			// 5th step: Serialize and return the result
			val respond = MemberName("io.ktor.response", "respond")
			val responseStatement = "%M.%M(result)"

			// If the function to call is a suspended function, wrap the resultStatement and response in a "runBlocking" block
			if(funSpec.modifiers.contains(KModifier.SUSPEND)) {
				val runBlocking = MemberName("kotlinx.coroutines", "runBlocking")
				moduleFunctionBuilder
					.beginControlFlow("%M", runBlocking)
			}

			// Add the result and response statements
			moduleFunctionBuilder
				.addStatement(resultStatement)
				.addStatement(responseStatement, call, respond)

			// If suspend, end the runBlocking block
			if(funSpec.modifiers.contains(KModifier.SUSPEND)) {
				moduleFunctionBuilder.endControlFlow()
			}

			// End the route block
			moduleFunctionBuilder
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

		// Generate Client
		// Define name and package of generated class
		val className = ClassName(targetPackage, "${serviceTypeSpec.name!!}Client")

		// Create class builder and set data modifier
		val classBuilder = TypeSpec.classBuilder(className)

		// Get superinterface of the service and add it to the client
		// TODO: Consider how to handle services which implement multiple interfaces
		val interfaceTypeName = serviceTypeSpec.superinterfaces.keys.firstOrNull()
		if(interfaceTypeName != null) {
			classBuilder.addSuperinterface(interfaceTypeName)
		}



		// Iterate through functions, adding a client function for each
		serviceTypeSpec.funSpecs.forEach { funSpec ->
			val apiUrl = "http://localhost:8080/radApi/${funSpec.name}"

			// Create client function
			val clientFunctionBuilder = FunSpec.builder(funSpec.name)
				.addModifiers(KModifier.PUBLIC)
				.addModifiers(KModifier.OVERRIDE)
				.returns(funSpec.returnType!!)

			// If service interface declares the function suspendable, add suspend modifier
			if (funSpec.modifiers.contains(KModifier.SUSPEND)) {
				clientFunctionBuilder.addModifiers(KModifier.SUSPEND)
			}


			// For each parameter of the function, add them to the client function
			funSpec.parameters.forEach { parameter ->
				clientFunctionBuilder.addParameter(parameter)
			}

			// Create endpoint corresponding to the function
			// 1st step: Open a HTTP client
			val httpClient = MemberName("io.ktor.client", "HttpClient")
			clientFunctionBuilder
				.addStatement("val client = %M()", httpClient)

			// 2nd step: Convert the body, if any, to JSON
			val gson = MemberName("com.google.gson", "Gson")

			clientFunctionBuilder
				.addStatement("val gson = %M()", gson)

			if(funSpec.parameters.isNotEmpty()) {
				var jsonBodyStatement = "gson.toJson(${funSpec.name}Request("

				funSpec.parameters.forEachIndexed { index, parameter ->
					jsonBodyStatement = "$jsonBodyStatement${parameter.name} = ${parameter.name}"
					if(index != funSpec.parameters.lastIndex) {
						jsonBodyStatement = "$jsonBodyStatement, "
					}
				}
				jsonBodyStatement = "$jsonBodyStatement))"

				clientFunctionBuilder
					.addStatement("val jsonBody = $jsonBodyStatement")
			}

			// 3rd step: Make request
			val post = MemberName("io.ktor.client.request", "post")
			val url = MemberName("io.ktor.client.request", "url")
			val returnType = funSpec.returnType!!

			// If the function is not suspendable, wrap the network call in runBlocking
			// and set response to the result of this
			if (!funSpec.modifiers.contains(KModifier.SUSPEND)) {
				val runBlocking = MemberName("kotlinx.coroutines", "runBlocking")
				clientFunctionBuilder
					.beginControlFlow("val response = %M", runBlocking)
			}

			// If the function is suspendable, set response to the result of the post call
			var responseStatement = "client.%M<String>"
			if (funSpec.modifiers.contains((KModifier.SUSPEND))) {
				responseStatement = "val response = $responseStatement"
			}

			// Set the URL and ContentType
			clientFunctionBuilder
				.beginControlFlow(responseStatement, post)
				.addStatement("%M(%S)", url, apiUrl)

			// If the function call has any parameters, serialize a request object and add it
			if (funSpec.parameters.isNotEmpty()) {
				clientFunctionBuilder.addStatement("body = %T(jsonBody, %T.Application.Json)", TextContent::class, ContentType::class)
			}

			// End the post block
			clientFunctionBuilder
				.endControlFlow()

			// If the function is not suspendable, end the runBlocking block
			if (!funSpec.modifiers.contains(KModifier.SUSPEND)) {
				clientFunctionBuilder
					.endControlFlow()
			}

			// 4th: step: Close the client, parse the result and return it
			clientFunctionBuilder
				.addStatement("client.close()")

			if (returnType is ParameterizedTypeName) {
				var resultStatement = "val result = gson.fromJson<$returnType>(response, %T.getParameterized(" +
						"${returnType.rawType}::class.java, "

				returnType.typeArguments.forEachIndexed { index, typeName ->
					resultStatement = "$resultStatement${typeName}::class.java"
					if (index != returnType.typeArguments.lastIndex) {
						resultStatement = "$resultStatement, "
					}
				}
				resultStatement = "$resultStatement).type)"

				clientFunctionBuilder
					.addStatement(resultStatement, TypeToken::class)
			} else {
				clientFunctionBuilder
					.addStatement("val result = gson.fromJson<$returnType>(response, $returnType::class.java)")
			}

			clientFunctionBuilder
				.addStatement("return result")

			// 5th step: Add function to FileSpec builder
			classBuilder
				.addFunction(clientFunctionBuilder.build())
		}

		// Build the file
		val file = File(generatedSourcesRoot)
		file.mkdir()

		fileSpecBuilder
			.addType(classBuilder.build())
			.build()
			.writeTo(file)
	}

	private fun generateSerialization() {

	}
}