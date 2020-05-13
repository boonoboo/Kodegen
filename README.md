# RADDD

<img src="./RAD/logo.png" width="150px" style="margin-left: auto; margin-right: auto; display: block;">

RADDD is a library for generating Web APIs and API clients for Kotlin web services.

## Usage

In your `build.gradle`, add rad to your dependencies:

```gradle
dependencies {
    implementation("dk.cachet.rad:rad:1.0.0")
}
```

To generate endpoints, add the dependencies using the kapt configuration:

```gradle
dependencies {
    kapt("dk.cachet.rad:rad:1.0.0")
}
````

## Examples

### Basic use

Given the following function:

```kotlin
@RadService
class ExampleServiceImpl() : ExampleService {
    override suspend fun foo(bar: Baz): Qux {
        ...
    }
}
```

RADDD will, on compilation, generate the following Ktor route:

```kotlin
post("/rad/foo/") {
    val request = call.receive<FooRequest>()
    val bar: Baz = request.bar
    val result = foo(bar)
    call.respond(result)
}
```

and the following client service invoker:

```kotlin
class ExampleServiceImplInvoker(val client: HttpClient, val json: Json, val baseUrl: String) : ExampleService {
    override suspend fun foo(bar: Baz): Qux {
    val response = client.post<FooResponse>("api/foo") {
        body = json.write(FooRequest(bar))
    }
    return response.result
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
The ```RadAuthenticate``` annotation is used to authenticate specific endpoints using a Ktor authentication scheme.

Adding the ```RadAuthenticate``` annotation to ```foo```:

```kotlin
@RadAuthenticate(["basic", "digest])
fun foo(bar: Baz) : Boolean
{
    ...
}
```

adds a requirement for authentication using the scheme ```"basic"``` or ```"digest"```:

```kotlin
authenticate("basic", "digest") {
    post("/rad/foo/") {
        ...
    }
}
```
