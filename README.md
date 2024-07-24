# NilMM / micromixin-platform-nilloader

NilMM is a nilmod that integrates Micromixin support for Nilloader.

## Usage

In order to use the Mixin functionality a Mixin config must be registerd.
This can be done through `NilMicromixin.addMixin(ClassLoader, String)`.
The `String` argument is the content of the Mixin config, the `ClassLoader`
is the modularity attachment used for Micromixin. It should be the classloader
of the source mod.

<b>Mixin configs are not automatically searched for.</b> They need to be
specified manually using above steps.

## Maven coordinates

NilMM can be declared as a dependency in maven through follows:

```xml
        <repository>
            <id>stianloader</id>
            <url>https://stianloader.org/maven</url>
        </repository>

        <!-- [...] -->

        <dependency>
            <groupId>org.stianloader</groupId>
            <artifactId>micromixin-platform-nilloader</artifactId>
            <version>0.2.0-a20240724</version>
            <scope>provided</scope>
        </dependency>
```

Hint: Chances are you may also want to depend on the micromixin-annotations
artifact. NilMM only ships the transformer and runtime modules.
For more information about micromixin's modularity, see it's repository and
the attached documentation.
