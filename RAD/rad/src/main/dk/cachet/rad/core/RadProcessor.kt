package dk.cachet.rad.core

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import dk.cachet.rad.serialization.ExceptionWrapper

import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@KotlinPoetMetadataPreview
@AutoService(Processor::class)
class RadProcessor : AbstractProcessor() {
	companion object {
		const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
	}

	val PRIMITIVE_TYPENAMES = setOf("kotlin.Char", "kotlin.String", "kotlin.Byte",
		"kotlin.Short", "kotlin.Int", "kotlin.Long", "kotlin.Float", "kotlin.Double",
		"kotlin.Boolean")

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

		val clientServiceMemberNames= mutableSetOf<MemberName>()
		val serviceTypeSpecs = mutableSetOf<TypeSpec>()

		roundEnv.getElementsAnnotatedWith(RadService::class.java).forEach {
			val serviceElement = it as TypeElement

			// Get the package of the service which will be used for the generated components
			val servicePackage = processingEnv.elementUtils.getPackageOf(serviceElement).toString()

			// Generate a KotlinPoet TypeSpec from the TypeElement, allowing Kotlin-specific information to be derived
			// Using Javax Annotation Processing and Kotlin Poet to derive parameter information for methods
			// may result in use of Java types instead of Kotlin types
			// Generating TypeSpecs ensures that the correct types are used
			// An ElementsClassInspector is used to assist in analyzing the classes when generating the TypeSpec
			val classInspector = ElementsClassInspector.create(processingEnv.elementUtils, processingEnv.typeUtils)
			val serviceTypeSpec = serviceElement.toTypeSpec(classInspector)

			// Generate the components
			generateRequestObjects(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			generateModule(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			generateClient(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			clientServiceMemberNames.add(MemberName("$servicePackage.rad", "${serviceElement.simpleName}Client"))
			serviceTypeSpecs.add(serviceTypeSpec)
		}

		// Temporary fix
		// Kapt seemingly calls "process" twice, but only iterates over the RadService annotations the first time
		// To avoid overwriting the configuration if the process is called again,
		// it is checked if the list of modules is empty, and if so, nothing is done
		if (clientServiceMemberNames.isNotEmpty()) {
			generateClientConfiguration(clientServiceMemberNames, generatedSourcesRoot)
		}

		if(serviceTypeSpecs.isNotEmpty()) {
			generateServerMain(serviceTypeSpecs, generatedSourcesRoot)
		}

		return false
	}

	private fun generateRequestObjects(serviceTypeSpec: TypeSpec, servicePackage: String, generatedSourcesRoot: String) {
		val targetPackage = "$servicePackage.rad"
		val fileName = "${serviceTypeSpec.name!!.capitalize()}RequestObjects"

		val file = File(generatedSourcesRoot)
		file.mkdir()

		val fileSpec = FileSpec.builder(targetPackage, fileName)

		// Iterate through all function of the service
		// For each function, generate a request object if it takes at least one parameter,
		// and a response object if it has a return type
		serviceTypeSpec.funSpecs.forEach { funSpec ->
			if(funSpec.parameters.isNotEmpty())
			{
				val requestObjectName = "${funSpec.name.capitalize()}Request"
				// Define name and package of generated class
				val serviceClassName = ClassName(targetPackage, requestObjectName)

				val typeBuilder = TypeSpec.classBuilder(serviceClassName)
					.addModifiers(KModifier.DATA)
					.addAnnotation(kotlinx.serialization.Serializable::class)

				val constructorBuilder = FunSpec.constructorBuilder()

				// Add the parameters of the function to the request object's properties and constructor parameters
				funSpec.parameters.forEach { parameter ->
					constructorBuilder.addParameter(parameter.name, parameter.type)
					typeBuilder.addProperty(
						PropertySpec.builder(parameter.name, parameter.type)
							.initializer(parameter.name)
							.build()
					)
				}

				typeBuilder
					.primaryConstructor(constructorBuilder.build())

				fileSpec.addType(typeBuilder.build())
			}
			if (funSpec.returnType != null) {
				val responseObjectName = "${funSpec.name.capitalize()}Response"
				val responseTypeBuilder = TypeSpec.classBuilder(ClassName(targetPackage, responseObjectName))
					.addModifiers(KModifier.DATA)
					.addAnnotation(kotlinx.serialization.Serializable::class)
					.primaryConstructor(FunSpec.constructorBuilder()
						.addParameter("result", funSpec.returnType!!)
						.build())
					.addProperty(PropertySpec.builder("result", funSpec.returnType!!)
						.initializer("result")
						.build())
				fileSpec.addType(responseTypeBuilder.build())
			}
		}

		fileSpec
			.build()
			.writeTo(file)
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
			val requestObjectName = "${funSpec.name.capitalize()}Request"
			// Create endpoint corresponding to the funSpec
			// 1st step: Route on HTTP Method (POST)
			val post = MemberName("io.ktor.routing", "post")

			val apiUrl = "/radApi/${funSpec.name}"
			moduleFunctionBuilder
				.beginControlFlow("%M(%S) {", post, apiUrl)

			// If the method takes parameters, attempt to receive the request object
			// 2nd step: Get request object
			val call = MemberName("io.ktor.application", "call")

			if(funSpec.parameters.isNotEmpty()) {
				val requestClassName = ClassName("$servicePackage.rad", requestObjectName)
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

			// 5th step: Wrap the result in a response object and return the result
			val responseObjectName = "${funSpec.name.capitalize()}Response"
			val respond = MemberName("io.ktor.response", "respond")
			val responseStatement = "%M.%M($responseObjectName(result = result))"

			// If the function to call is a suspended function, wrap the resultStatement and response in a "runBlocking" block
			if(funSpec.modifiers.contains(KModifier.SUSPEND)) {
				val runBlocking = MemberName("kotlinx.coroutines", "runBlocking")
				moduleFunctionBuilder
					.beginControlFlow("%M", runBlocking)
			}

			// Add function call and response statements in try / catch block
			moduleFunctionBuilder
				.beginControlFlow("try")
				.addStatement(resultStatement)
				.addStatement(responseStatement, call, respond)
				.endControlFlow()
				.beginControlFlow("catch (exception: %T)", Exception::class)
				.addStatement("val exceptionWrapper = %T(exception)", ExceptionWrapper::class)
				.addStatement("%M.%M(exceptionWrapper)", call, respond)
				.endControlFlow()

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

		// Generate default serializer function and property with this value
		classBuilder
			.addProperty(PropertySpec.builder("json", Json::class)
				.mutable()
				.initializer("Json(%T.Stable)", JsonConfiguration::class)
				.build())

		classBuilder.addProperty(PropertySpec.builder("client", HttpClient::class)
			.initializer("HttpClient()")
			.build())

		fun createDefaultJSON( module: SerialModule = EmptyModule ): Json
		{
			val configuration = JsonConfiguration.Stable.copy(useArrayPolymorphism = true)
			return Json(configuration, module)
		}

		// Iterate through functions, adding a client function for each
		serviceTypeSpec.funSpecs.forEach { funSpec ->
			val apiUrl = "http://localhost:8080/radApi/${funSpec.name}"
			val requestObjectName = "${funSpec.name.capitalize()}Request"

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
			// DEPRECATED
			// val httpClient = MemberName("io.ktor.client", "HttpClient")
			// clientFunctionBuilder
			//	.addStatement("val client = %M()", httpClient)

			// 2nd step: Convert the body, if any, to JSON
			if(funSpec.parameters.isNotEmpty()) {
				var jsonBodyStatement = "json.stringify($requestObjectName.serializer(), $requestObjectName("

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

			// 4th: step: Parse the result and return it
			val responseType = "${funSpec.name.capitalize()}Response"

			clientFunctionBuilder
				.addStatement("val result = json.parse<$responseType>($responseType.serializer(), response).result")
			/*
			if (returnType is ParameterizedTypeName) {
				val serializer = getReturnTypeSerializer(returnType)

				clientFunctionBuilder
					.addStatement("val result = json.parse<$returnType>($serializer, response)")
			} else {
				clientFunctionBuilder
					.addStatement("val result = json.parse<$returnType>($returnType.serializer(), response)")
			}
			*/
			clientFunctionBuilder
				.addStatement("return result")

			// 5th step: Add function to FileSpec builder
			classBuilder
				.addFunction(clientFunctionBuilder.build())
		}

		// Build the file
		val file = File(generatedSourcesRoot)
		file.mkdir()

		// Because of prototyping constraints, serialization builtins are imported directly
		// Rather than through the use of MemberNames
		fileSpecBuilder
			.addType(classBuilder.build())
			.addImport("kotlinx.serialization.builtins", "ArraySerializer")
			.addImport("kotlinx.serialization.builtins", "ListSerializer")
			.addImport("kotlinx.serialization.builtins", "SetSerializer")
			.addImport("kotlinx.serialization.builtins", "MapSerializer")
			.addImport("kotlinx.serialization.builtins", "PairSerializer")
			.addImport("kotlinx.serialization.builtins", "TripleSerializer")
			.addImport("kotlinx.serialization.builtins", "MapEntrySerializer")
			.addImport("kotlinx.serialization.builtins", "serializer")
			.build()
			.writeTo(file)
	}

	// TODO
	//   Generate server main function, containing:
	//   Dependency injection configuration
	//   Jetty engine function
	//   Content negotiation installation
	//   Authentication installation
	private fun generateServerMain(serviceTypeSpecs: Set<TypeSpec>, generatedSourcesRoot: String) {
		val applicationEngineEnvironment = MemberName("io.ktor.server.engine", "applicationEngineEnvironment")
		val embeddedServer = MemberName("io.ktor.server.engine", "embeddedServer")
		val jetty = MemberName("io.ktor.server.jetty", "Jetty")
		val serverConnector = MemberName("org.eclipse.jetty.server", "ServerConnector")
		val install = MemberName("io.ktor.application", "install")
		val contentNegotiation = MemberName("io.ktor.features", "ContentNegotiation")
		val contentType = MemberName("io.ktor.http", "ContentType")
		val serializationConverter = MemberName("io.ktor.serialization", "SerializationConverter")
		val json = MemberName("kotlinx.serialization.json", "Json")
		val defaultJsonConfiguration = MemberName("io.ktor.serialization", "DefaultJsonConfiguration")
		val koinModule = MemberName("org.koin.dsl", "module")
		val startKoin = MemberName("org.koin.core.context", "startKoin")


		// Main module (for installation of shared features) builder
		val mainModule = FunSpec.builder("radMainModule")
			.receiver(Application::class)

		mainModule
			.beginControlFlow("%M(%M)", install, contentNegotiation)
			.addStatement("register(%M.Application.Json, %M(%M(%M)))",
				contentType, serializationConverter, json, defaultJsonConfiguration)
			.endControlFlow()

		// Rad main function builder
		val radMain = FunSpec.builder("radMain")
			.addParameter("args", Array<Any>::class.parameterizedBy(String::class))

		// Create the engine environment
		radMain
			.beginControlFlow("val environment = %M", applicationEngineEnvironment)
			.beginControlFlow("module")
			.addStatement("radMainModule()")

		// TODO
		//   Register each Ktor module
		//services.forEach { serviceMemberName ->
		//	radMain.addStatement("single { %M() }", serviceMemberName)
		//}

		// End module block
		radMain.endControlFlow()

		// End environment block
		radMain.endControlFlow()

		// TODO
		//   Find a method to register the dependencies of each service
		val toRegisterServices = mutableSetOf<TypeName>()
		serviceTypeSpecs.forEach { typeSpec ->
			typeSpec.primaryConstructor!!.parameters.forEach { parameter ->
				toRegisterServices.add(parameter.type)
			}
		}

		radMain
			.beginControlFlow("val module = %M", koinModule)

		toRegisterServices.forEach {
			//radMain.addStatement("$it()")
		}

		// Start Koin
		radMain
			.endControlFlow()
			.beginControlFlow("%M()", startKoin)
			.addStatement("modules(module)")
			.endControlFlow()

		// Start the engine
		radMain
			.beginControlFlow("val server = %M(%M, environment)", embeddedServer, jetty)
			.beginControlFlow("configureServer =")

		// Set connector
		// TODO
		//   Only simple HTTP has been implemented
		//   Consider how to integrate HTTPS (will require developer to link SSL certificate)
		radMain.addStatement("this.addConnector(%M(this).apply { port = 80 })", serverConnector)

		// End configureServer block
		// End embeddedEngine block
		radMain
			.endControlFlow()
			.endControlFlow()


		// Start the engine
		radMain.addStatement("server.start(wait = true)")


		val file = File(generatedSourcesRoot)
		file.mkdir()

		FileSpec.builder("dk.cachet.rad.core", "RadEngine")
			.addAliasedImport(koinModule, "koinModule")
			.addFunction(mainModule.build())
			.addFunction(radMain.build())
			.build()
			.writeTo(file)
	}

	private fun generateClientConfiguration(koinModules: MutableSet<MemberName>, generatedSourcesRoot: String) {
		// Configuration function
		val koinModule = MemberName("org.koin.dsl", "module")

		val configureRadBuilder = FunSpec.builder("configureRad")
			.beginControlFlow("val module = %M", koinModule)

		koinModules.forEach { serviceMemberName ->
			configureRadBuilder.addStatement("single { %M() }", serviceMemberName)
		}

		configureRadBuilder.endControlFlow()

		val startKoin = MemberName("org.koin.core.context", "startKoin")

		configureRadBuilder
			.beginControlFlow("%M", startKoin)
			.addStatement("modules(module)")
			.endControlFlow()

		// Write file with configuration content
		val file = File(generatedSourcesRoot)
		file.mkdir()

		FileSpec.builder("dk.cachet.rad.core", "RadClientConfiguration")
			.addFunction(configureRadBuilder.build())
			.build()
			.writeTo(file)
	}

	// TODO
	//   Not tested: Multilevel parameterization
	//   Tested and not working: Types parameterized with "Any"
	private fun getReturnTypeSerializer(returnType: ParameterizedTypeName): String {
		val result: String
		var inner = ""
		returnType.typeArguments.forEachIndexed { index, typeArgument ->
			// If typeargument is also parameterized, call the function recursively to find inner serializers
			if (typeArgument is ParameterizedTypeName) {
				inner += getReturnTypeSerializer(typeArgument)
			} else {
				inner += "$typeArgument.serializer()"
			}
			if (index != returnType.typeArguments.lastIndex) {
				inner += ", "
			}
		}
		// If TypeArgument is parameterized by a class the requires use of builtin serializers,
		// use this instead
		if (returnType.rawType == ClassName("kotlin.collections", "List")) {
			result = "ListSerializer($inner)"
		} else if (returnType.rawType == ClassName("kotlin.collections", "Map")) {
			result = "MapSerializer($inner)"
		} else if (returnType.rawType == ClassName("kotlin.collections", "Set")) {
			result = "SetSerializer($inner)"
		} else if (returnType.rawType == ClassName("kotlin", "Array")) {
			result = "ArraySerializer($inner)"
		} else if (returnType.rawType == ClassName("kotlin", "Pair")) {
			result = "PairSerializer($inner)"
		} else if (returnType.rawType == ClassName("kotlin", "Triple")) {
			result = "TripleSerializer($inner)"
		} else if (returnType.rawType == ClassName("kotlin.collections", "Map", "Entry")) {
			result = "MapEntrySerializer($inner)"
		}
		// Otherwise, use normal .serializer()
		else {
			result = "${returnType}.serializer($inner)"
		}
		return result
	}
}