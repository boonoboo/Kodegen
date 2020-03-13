# RADDD

<img src="./RAD/logo.png" width="150px" style="margin-left: auto; margin-right: auto; display: block;">

RADDD is a library for generating Web APIs and API clients for Kotlin web services.

## Usage

In your `build.gradle`, add RADDD to your plugins:

```gradle
plugins {
    id 'dk.cachet.raddd' version '1.0.0'
}
```

Then, add the `raddd` task to your dependencies:

```gradle
dependencies {
    raddd
}
````

To configure RADDD, use the ```raddd``` task:

```gradle
raddd {
    // Settings
    generateWebApi = true
    generateClient = true
}
```

## Examples

### Basic use

Given the following function:

```kotlin
@RADDDMethod
fun createUser(userName: String, email: String) : Boolean
{
    ...
}
```

RADDD will, on compilation, generate the following Ktor route:

```kotlin
post("/api/createUser/") {
    val request = call.receive<CreateUserRequest>()
    val userName: String = request.userName
    val email: String = email = request.email
    val result = createUser(userName, email)
    call.respond(result)
}
```

and the following API client method:

```kotlin
suspend fun createUser(userName: String, email: String) : Boolean {
    val client = HttpClient()
    val response = client.post<Boolean>("api/createUser") {
        body = json.write(CreateUserRequest(userName, email))
    }
    client.close()
    return response.result
}
```

both of which use the following request object:

```kotlin
data class CreateUserRequest(val userName: String, val email: String)
```

### Authorisations

Adding the ```RADDDAuthorize``` annotation to the ```createUser``` function with a list of allowed roles:

```kotlin
@RADDDMethod
@RADDDAuthorize(["Admin", "Superuser"])
fun createUser(userName: String, email: String) : Boolean
{
    ...
}
```

causes RADDD to install the following Ktor authorisation:

```kotlin
install(Authentication) {
    basic(name = "Admin") {
        realm = "Server"
        validate { credentials -> ... }
    }
}
```

This also modifies the Web API endpoint:

```kotlin
authenticate("Admin") {
    post("/api/createUser/") {
        val request = call.receive<CreateUserRequest>()
        val userName: String = request.userName
        val email: String = email = request.email
        val result = createUser(userName, email)
        call.respond(result)
    }
}
```

and the API client method:

```kotlin
suspend fun createUser(userName: String, email: String) : Boolean {
    val client = HttpClient() {
        install(Auth) {
            basic {
                ...
            }
        }
    }

    val response = client.post<Boolean>("api/createUser") {
        body = json.write(CreateUserRequest(userName, email))
    }
    client.close()
    return response.result
}
```
