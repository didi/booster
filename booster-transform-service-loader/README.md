# booster-transform-service-loader

This module is used for `ServiceLoader` optimization on Android, as we known, JDK provided `ServiceLoader` since Java 1.5, due the different implementation, using `ServiceLoader` on Android platform will cause performance issue, booster trying to fix this problem by manipulate the bytecode.

Android R8 also provides `ServiceLoader` optimization, due to the compatibility issues, there are still some apps have not migrate from Proguard to R8, in this case, this module can be an alternative of R8.

To enable `ServiceLoader` optimization, the invocation of `ServiceLoader` must be follow the following pattern:

```java
ServiceLoader.load(Service.class).iterator()
```

After optimization, the code will be

```java
Arrays.asList(new Service[] { new A(), new B() }).iterator()
```
