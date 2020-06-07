# rad

<img src="./RAD/logo.png" width="150px" style="margin-left: auto; margin-right: auto; display: block;">

rad is a library for automatic generation of web API endpoints and API client methods for Ktor applications.

## Usage

In your `build.gradle`, add rad to your dependencies:

```gradle
dependencies {
    implementation("dk.cachet:rad:1.0.1")
}
```

To generate code, add the dependencies using the kapt configuration:

```gradle
dependencies {
    kapt("dk.cachet.rad:rad:1.0.1")
}
````

## Examples

### Basic use

Given the following application service:

```kotlin
@ApplicationService
class ExampleService() {
    suspend fun foo(bar: Baz): Qux
}
```

rad will, on compilation, generate the following Ktor module:

```kotlin
fun Application.ExampleServiceModule(service: ExampleService, vararg authSchemes: String) {
  routing {
    post("/radApi/exampleService/foo/") {
      val request = call.receive<FooRequest>()
      val bar = request.bar
      val result = service.foo(bar)
      call.respond(result = result)
    }
  }
}
```

and the following client class:

```kotlin
class ExampleServiceInvoker(val client: HttpClient, val json: Json, val baseUrl: String) {
    suspend fun foo(bar: Baz): Qux {
    val jsonBody = json.stringify(FooRequest.serializer(), FooRequest(bar = bar))
    val response = client.post<String> {
        url("$baseUrl/radApi/dateService/foo")
        body = TextContent(jsonBody, ContentType.Application.Json)
    }
    val result = json.parse(FooResponse.serializer(), response).result
    return result
}
```

using the following request and response objects:

```kotlin
@Serializable
data class FooRequest(val bar: Baz)
```

```kotlin
@Serializable
data class FooResponse(val result: Qux)
```


### Authentication
The ```RequiresAuthentication``` annotation is used to indicate that a user should be authenticated before a method is called. Using it will add an authentication interceptor before routing to the endpoint. The authentication schemes to use for this are passed as parameters to the Ktor module.

Thus, adding ```RequiresAuthentication``` to the method ```foo```:

```kotlin
@RequiresAuthentication
fun foo(bar: Baz): Qux
{
    ...
}
```

results in the route:

```kotlin
authenticate(*authSchemes) {
    post("/radApi/exampleService/foo/") {
        ...
    }
}
```
