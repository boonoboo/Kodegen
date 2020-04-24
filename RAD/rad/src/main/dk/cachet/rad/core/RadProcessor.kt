package dk.cachet.rad.core

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec

import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import kotlinx.serialization.json.JsonConfiguration
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
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

		val koinModules= mutableSetOf<MemberName>()

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
			// TODO: Consider logic for determining target package:
			//   set in the annotation: @RadPackage("org.example.api")
			//   inferred from the service: org.example.domain -> org.example.domain.rad
			val servicePackage = processingEnv.elementUtils.getPackageOf(serviceElement).toString()

			// Generate a KotlinPoet TypeSpec from the TypeClass, allowing Kotlin-specific information to be derived
			// This is necessary because using javax Annotation Processing and Kotlin Poet to derive parameter
			// information for methods may result in use of Java types instead of Kotlin types,
			// e.g. using java.lang.String instead of kotlin.String
			// An ElementsClassInspector is used to assist in analyzing the classes when generating the TypeSpec
			val classInspector = ElementsClassInspector.create(processingEnv.elementUtils, processingEnv.typeUtils)
			val serviceTypeSpec = serviceElement.toTypeSpec(classInspector)

			// TODO
			//   If going with "iterate over service types" approach:
			//   Iterate over all service type specs
			//   For each type used, add it to the domainTypes Set
			//   As a TypeSpec is needed, it may be necessary to get the elements from roundEnv,
			//   and from there generate a TypeSpec for each

			/*
			serviceTypeSpec.funSpecs.forEach { funSpec ->
				funSpec.parameters.forEach { parameter ->
					// TODO
					//  This might work?
					val typeName = parameter.type
					processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Parameter TypeName: ${parameter.type}")
					val typeElement = processingEnv.elementUtils.getAllTypeElements(parameter.type.toString()).first()
					processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Parameter TypeElement: ${typeElement}")
					if(typeElement != null) {
						val typeSpec = typeElement.toTypeSpec(classInspector)
						domainTypes.add(Pair(typeName, typeSpec))
					}
				}
				if(funSpec.returnType != null) {
					val typeName = funSpec.returnType!!
					processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Return type: ${funSpec.returnType}")
					val typeElement = processingEnv.elementUtils.getAllTypeElements(funSpec.returnType!!.toString()).first()
					if(typeElement != null) {
						val typeSpec = typeElement
							.toTypeSpec(classInspector)
						domainTypes.add(Pair(typeName, typeSpec))
					}
				}
			}
			*/

			// Generate the request object, module and client
			generateRequestObjects(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			generateModule(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			generateClient(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			koinModules.add(MemberName("$servicePackage.rad", "${serviceElement.simpleName}Client"))
		}

		// Temporary fix
		// Kapt seemingly calls "process" twice, but only iterates over the RadService annotations the first time
		// To avoid overwriting the configuration if the process is called again,
		// it is checked if the list of modules is empty, and if so, nothing is done
		if (koinModules.isNotEmpty()) {
			generateConfiguration(koinModules, generatedSourcesRoot)
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
			val json = MemberName("kotlinx.serialization.json", "Json")

			clientFunctionBuilder
				.addStatement("val json = %M(%T.Stable)", json, JsonConfiguration::class)

			if(funSpec.parameters.isNotEmpty()) {
				var jsonBodyStatement = "json.toJson(${funSpec.name}Request.serializer(), ${funSpec.name}Request("

				funSpec.parameters.forEachIndexed { index, parameter ->
					jsonBodyStatement = "$jsonBodyStatement${parameter.name} = ${parameter.name}"
					if(index != funSpec.parameters.lastIndex) {
						jsonBodyStatement = "$jsonBodyStatement, "
					}
				}
				jsonBodyStatement = "$jsonBodyStatement))"

				clientFunctionBuilder
					.addStatement("val jsonBody = $jsonBodyStatement.toString()")
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
				// TODO
				//   Parameterization currently only works one level deep
				//   A recursive function should be made to check if a typeArgument is also parameterized

				// TODO
				//   When type is primitive, .serializer() must be imported from
				//   kotlinx.serialization.builtins.PrimitiveSerializersKt
				//   A naive way of doing this would be to check if returnType is equal to a Primitive
				when (returnType.rawType) {
					ClassName("kotlin.collections", "List") -> {
						val list = MemberName("kotlinx.serialization.builtins", "list")
						clientFunctionBuilder
							.addStatement("val result = json.parse<$returnType>" +
									"(${returnType.typeArguments.first()}.serializer().%M, response)", list)
					}
					ClassName("kotlin.collections", "Map") -> {
						val map = MemberName("kotlinx.serialization.builtins", "map")
						clientFunctionBuilder
							.addStatement("val result = json.parse<$returnType>" +
									"(${returnType.typeArguments.first()}.serializer().%M, response)", map)
					}
					ClassName("kotlin.collections", "Set") -> {
						val set = MemberName("kotlinx.serialization.builtins", "set")
						clientFunctionBuilder
							.addStatement("val result = json.parse<$returnType>" +
									"(${returnType.typeArguments.first()}.serializer().%M, response)", set)
					}
					else -> {
						var resultStatement =
							"val result = json.parse<$returnType>($returnType.serializer("

						returnType.typeArguments.forEachIndexed { index, typeName ->
							resultStatement = "$resultStatement${typeName}.serializer()"
							if (index != returnType.typeArguments.lastIndex) {
								resultStatement = "$resultStatement, "
							}
						}
						resultStatement = "$resultStatement), response)"

						clientFunctionBuilder
							.addStatement(resultStatement)
					}
				}
			} else {
				// TODO
				//   DEBUG
				if(returnType.toString() in PRIMITIVE_TYPENAMES) {
					val serializer = MemberName("kotlinx.serialization.builtins", "serializer")

					clientFunctionBuilder
						.addStatement("val result = json.parse<$returnType>($returnType.%M(), response)", serializer)
				}
				else {
					clientFunctionBuilder
						.addStatement("val result = json.parse<$returnType>($returnType.serializer(), response)")
				}
				// TODO
				//   END DEBUG
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

	private fun generateConfiguration(koinModules: MutableSet<MemberName>, generatedSourcesRoot: String) {
		// Configuration function
		val koinModule = MemberName("org.koin.dsl", "module")

		val configureRadBuilder = FunSpec.builder("configureRad")
			.receiver(RadConfiguration::class)
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

		FileSpec.builder("dk.cachet.rad.core", "RadConfiguration")
			.addFunction(configureRadBuilder.build())
			.build()
			.writeTo(file)

	}

	// TODO
	//   INCOMPLETE
	private fun getReturnTypeSerializer(returnType: ParameterizedTypeName): List<Pair<String, MemberName?>> {
		val results = mutableListOf<Pair<String, MemberName?>>()
		returnType.typeArguments.forEach { typeArgument ->
			// If typeargument is also parameterized, call the function recursively to find inner serializers
			if(typeArgument is ParameterizedTypeName) {
				return getReturnTypeSerializer(typeArgument)
			}
			// TODO
			//    Consider adding the .list, .map, .set to the end of existing serializer strings instead
			//    e.g. if returnType is List -> typeArgument.append(".list")
			// If TypeArgument is parameterized by list, map or set, use their special serializer
			if (returnType.rawType == ClassName("kotlin.collections", "List")) {
				val list = MemberName("kotlinx.serialization.builtins", "list")
				results.add(Pair("${typeArgument}.serializer().%M)", list))
			} else if (returnType.rawType == ClassName("kotlin.collections", "Map")) {
				val map = MemberName("kotlinx.serialization.builtins", "map")
				results.add(Pair("${typeArgument}.serializer().%M)", map))
			} else if (returnType.rawType == ClassName("kotlin.collections", "Set")) {
				val set = MemberName("kotlinx.serialization.builtins", "set")
				results.add(Pair("${typeArgument}.serializer().%M)", set))
			}
			// Otherwise, return a normal serializer
			else {
				results.add(Pair("${typeArgument}.serializer()", null))
			}
		}
		return results
	}
}