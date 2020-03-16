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
@SupportedSourceVersion(SourceVersion.RELEASE_13)
@SupportedOptions(RadProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedAnnotationTypes("dk.cachet.rad.core.RadService")
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

		// TODO: Generate serializer for each domain object
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
			// TODO: Implement use of a Class Inspector
			//val serviceTypeSpec = serviceElement.toTypeSpec(ReflectiveClassInspector.create())
			val serviceTypeSpec = serviceElement.toTypeSpec()

			// Generate the request object, module and client
			generateRequestObjects(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			generateModule(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			generateClient(serviceTypeSpec, servicePackage, generatedSourcesRoot)
			//registerKoinDependency
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

		// Create service for calling service functions using lazy injection
		val service = MemberName(servicePackage, serviceTypeSpec.name!!)
		val inject = MemberName("org.koin.ktor.ext", "inject")
		moduleFunctionBuilder.addStatement("val service: %M by %M()", service, inject)


		/*
		var serviceStatement = "val service = %M("
		// For each parameter the service takes, add the parameter using the name of the parameter
		if(serviceTypeSpec.primaryConstructor != null) {
			serviceTypeSpec.primaryConstructor!!.parameters.forEach { parameter ->
				serviceStatement = "$serviceStatement ${parameter.name}"
				if (serviceTypeSpec.primaryConstructor!!.parameters.indexOf(parameter)
					!= serviceTypeSpec.primaryConstructor!!.parameters.lastIndex
				) {
					serviceStatement = "$serviceStatement, "
				}
			}
		}
		serviceStatement = "$serviceStatement)"

		moduleFunctionBuilder.addStatement(serviceStatement, service)
		*/
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
		//val superInterface = serviceTypeSpec.superinterfaces.values.first()
		//classBuilder.addSuperinterface(superInterface!!::class)

		// Iterate through functions, adding a client function for each
		serviceTypeSpec.funSpecs.forEach { funSpec ->
			val apiUrl = "/radApi/${servicePackage.replace(".", "/")}/${funSpec.name}"

			// Create client function
			val clientFunctionBuilder = FunSpec.builder(funSpec.name)
				.addModifiers(KModifier.PUBLIC)
				.returns(funSpec.returnType!!)

			// If service interface declares the function suspendable, add suspend modifier
			if(funSpec.modifiers.contains(KModifier.SUSPEND)) {
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

			// 3rd step: Make request
			val post = MemberName("io.ktor.client.request", "post")
			val url = MemberName("io.ktor.client.request", "url")
			val returnType = funSpec.returnType!!

			// If the function is not suspendable, wrap the network call in runBlocking
			// and set response to the result of this
			if(!funSpec.modifiers.contains(KModifier.SUSPEND)) {
				val runBlocking = MemberName("kotlinx.coroutines", "runBlocking")
				clientFunctionBuilder
					.beginControlFlow("val response = %M", runBlocking)
			}

			// If the function is suspendable, set response to the result of the post call
			var responseStatement = "client.%M<$returnType>"
			if(funSpec.modifiers.contains((KModifier.SUSPEND)))
			{
				responseStatement = "val response = $responseStatement"
			}

			clientFunctionBuilder
				.beginControlFlow(responseStatement, post)
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

			// End the post block
			clientFunctionBuilder
				.endControlFlow()

			// If the function is not suspendable, end the runBlocking block
			if(!funSpec.modifiers.contains(KModifier.SUSPEND))
			{
				clientFunctionBuilder
					.endControlFlow()
			}

			// 4th: step: Close the client and return the result
			clientFunctionBuilder
				.addStatement("client.close()")

			clientFunctionBuilder
				.addStatement("return response")

			// 5th step: Add function to FileSpec builder
			classBuilder
				.addFunction(clientFunctionBuilder.build())
		}

		// TODO: Add Koin module
		// TODO: This should go in some configuration function that is always run
		val koinModule = MemberName("org.koin.dsl", "module")
		val koinModuleBuilder = FunSpec.builder("${serviceTypeSpec.name!!}TodoMoveSomewhereElse")
			.beginControlFlow("val koinModule = %M", koinModule)
			.addStatement("single { ${className.simpleName}() }")
			.endControlFlow()

		/*
		val startKoin = MemberName("org.koin.core.context", "startKoin")

		koinModuleBuilder
			.beginControlFlow("%M", startKoin)

		// modulesStatement = "modules(
		// for each module..
		//    modulesStatement += moduleName
		//    modulesStatement += ", "
		// modulesStatement += ")"
		*/
		// Build the file
		val file = File(generatedSourcesRoot)
		file.mkdir()

		fileSpecBuilder
			.addType(classBuilder.build())
			.addFunction(koinModuleBuilder.build())
			.build()
			.writeTo(file)
	}
}