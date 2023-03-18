# NilMM / NilMicromixin

NilMM is a nilmod that integrates Micromixin support for Nilloader.

## Usage

In order to use the Mixin functionality a Mixin config must be registerd.
This can be done through `NilMicromixin.addMixin(ClassLoader, String)`.
The `String` argument is the content of the Mixin config, the `ClassLoader`
is the modularity attachment used for Micromixin. It should be the classloader
of the source mod.

<b>Mixin configs are not automatically searched for.</b>

## Maven coordinates

As of now, NilMM and the Micromixin framework overall is not hosted
at a maven repository. It must be built manually using maven.

NilMM can be declared as a dependency in maven through follows:

```xml
        <dependency>
            <groupId>de.geolykt.starloader</groupId>
            <artifactId>nilmm</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>
```





