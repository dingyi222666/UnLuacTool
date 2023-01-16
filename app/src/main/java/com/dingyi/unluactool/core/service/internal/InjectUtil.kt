package com.dingyi.unluactool.core.service.internal

import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.lang.reflect.Type


internal object InjectUtil {
    /**
     * Selects the single injectable constructor for the given type.
     * The type must either have only one public or package-private default constructor,
     * or it should have a single non-private constructor annotated with [Inject].
     *
     * @param type the type to find the injectable constructor of.
     */
    fun selectConstructor(type: Class<*>): Constructor<*> {
        if (isInnerClass(type)) {
            // The DI system doesn't support injecting non-static inner classes.
            error(
                String.format(
                    "Unable to select constructor for non-static inner class %s.",
                    format(type)
                )
            )
        }
        val constructors = type.declaredConstructors
        if (constructors.size == 1) {
            val constructor = constructors[0]
            if (isPublicOrPackageScoped(constructor)) {
                // If there is a single constructor, and that constructor is public or package private we select it.
                return constructor
            }
            if (constructor.getAnnotation(Inject::class.java) != null) {
                // Otherwise, if there is a single constructor that is annotated with `@Inject`, we select it (short-circuit).
                return constructor
            }
        }

        // Search for a valid `@Inject` constructor to use instead.
        var match: Constructor<*>? = null
        for (constructor in constructors) {
            if (constructor.getAnnotation(Inject::class.java) != null) {
                if (match != null) {
                    // There was a previously found a match. This means a second constructor with `@Inject` has been found.
                    error(
                        String.format(
                            "Multiple constructor annotated with @Inject for %s.",
                            format(type)
                        )
                    )
                }
                // A valid match was found.
                match = constructor
            }
        }
        if (match == null) {
            // No constructor annotated with `@Inject` was found.
            error(
                String.format(
                    "Expected a single non-private constructor, or one constructor annotated with @Inject for %s.",
                    format(type)
                )
            )
        }
        return match
    }

    private fun isInnerClass(clazz: Class<*>): Boolean {
        return clazz.isMemberClass && !Modifier.isStatic(clazz.modifiers)
    }

    private fun isPublicOrPackageScoped(constructor: Constructor<*>): Boolean {
        return Modifier.isPublic(constructor.modifiers) || isPackagePrivate(constructor.modifiers)
    }

    fun isPackagePrivate(modifiers: Int): Boolean {
        return !Modifier.isPrivate(modifiers) && !Modifier.isProtected(modifiers) && !Modifier.isPublic(
            modifiers
        )
    }

    private fun format(type: Type): String {
        return TypeStringFormatter.format(type)
    }
}



/**
 * Identifies injectable constructors, methods, and fields. May apply to static
 * as well as instance members. An injectable member may have any access
 * modifier (private, package-private, protected, public). Constructors are
 * injected first, followed by fields, and then methods. Fields and methods
 * in superclasses are injected before those in subclasses. Ordering of
 * injection among fields and among methods in the same class is not specified.
 *
 *
 * Injectable constructors are annotated with `@Inject` and accept
 * zero or more dependencies as arguments. `@Inject` can apply to at most
 * one constructor per class.
 *
 *
 * <tt></tt><blockquote style="padding-left: 2em; text-indent: -2em;">@Inject
 * *ConstructorModifiers<sub>opt</sub>*
 * *SimpleTypeName*(*FormalParameterList<sub>opt</sub>*)
 * *Throws<sub>opt</sub>*
 * *ConstructorBody*</blockquote>
 *
 *
 * `@Inject` is optional for public, no-argument constructors when no
 * other constructors are present. This enables injectors to invoke default
 * constructors.
 *
 *
 * <tt></tt><blockquote style="padding-left: 2em; text-indent: -2em;">
 * @Inject<sub>*opt*</sub>
 * *Annotations<sub>opt</sub>*
 * public
 * *SimpleTypeName*()
 * *Throws<sub>opt</sub>*
 * *ConstructorBody*</blockquote>
 *
 *
 * Injectable fields:
 *
 *  * are annotated with `@Inject`.
 *  * are not final.
 *  * may have any otherwise valid name.
 *
 *
 * <tt></tt><blockquote style="padding-left: 2em; text-indent: -2em;">@Inject
 * *FieldModifiers<sub>opt</sub>*
 * *Type*
 * *VariableDeclarators*;</blockquote>
 *
 *
 * Injectable methods:
 *
 *  * are annotated with `@Inject`.
 *  * are not abstract.
 *  * do not declare type parameters of their own.
 *  * may return a result
 *  * may have any otherwise valid name.
 *  * accept zero or more dependencies as arguments.
 *
 *
 * <tt></tt><blockquote style="padding-left: 2em; text-indent: -2em;">@Inject
 * *MethodModifiers<sub>opt</sub>*
 * *ResultType*
 * *Identifier*(*FormalParameterList<sub>opt</sub>*)
 * *Throws<sub>opt</sub>*
 * *MethodBody*</blockquote>
 *
 *
 * The injector ignores the result of an injected method, but
 * non-`void` return types are allowed to support use of the method in
 * other contexts (builder-style method chaining, for example).
 *
 *
 * Examples:
 *
 * <pre>
 * public class Car {
 * // Injectable constructor
 * &#064;Inject public Car(Engine engine) { ... }
 *
 * // Injectable field
 * &#064;Inject private Provider&lt;Seat> seatProvider;
 *
 * // Injectable package-private method
 * &#064;Inject void install(Windshield windshield, Trunk trunk) { ... }
 * }</pre>
 *
 *
 * A method annotated with `@Inject` that overrides another method
 * annotated with `@Inject` will only be injected once per injection
 * request per instance. A method with *no* `@Inject` annotation
 * that overrides a method annotated with `@Inject` will not be
 * injected.
 *
 *
 * Injection of members annotated with `@Inject` is required. While an
 * injectable member may use any accessibility modifier (including
 * <tt>private</tt>), platform or injector limitations (like security
 * restrictions or lack of reflection support) might preclude injection
 * of non-public members.
 *
 * <h3>Qualifiers</h3>
 *
 *
 * A [qualifier][Qualifier] may annotate an injectable field
 * or parameter and, combined with the type, identify the implementation to
 * inject. Qualifiers are optional, and when used with `@Inject` in
 * injector-independent classes, no more than one qualifier should annotate a
 * single field or parameter. The qualifiers are bold in the following example:
 *
 * <pre>
 * public class Car {
 * &#064;Inject private **@Leather** Provider&lt;Seat> seatProvider;
 *
 * &#064;Inject void install(**@Tinted** Windshield windshield,
 * **@Big** Trunk trunk) { ... }
 * }</pre>
 *
 *
 * If one injectable method overrides another, the overriding method's
 * parameters do not automatically inherit qualifiers from the overridden
 * method's parameters.
 *
 * <h3>Injectable Values</h3>
 *
 *
 * For a given type T and optional qualifier, an injector must be able to
 * inject a user-specified class that:
 *
 *
 *  1. is assignment compatible with T and
 *  1. has an injectable constructor.
 *
 *
 *
 * For example, the user might use external configuration to pick an
 * implementation of T. Beyond that, which values are injected depend upon the
 * injector implementation and its configuration.
 *
 * <h3>Circular Dependencies</h3>
 *
 *
 * Detecting and resolving circular dependencies is left as an exercise for
 * the injector implementation. Circular dependencies between two constructors
 * is an obvious problem, but you can also have a circular dependency between
 * injectable fields or methods:
 *
 * <pre>
 * class A {
 * &#064;Inject B b;
 * }
 * class B {
 * &#064;Inject A a;
 * }</pre>
 *
 *
 * When constructing an instance of `A`, a naive injector
 * implementation might go into an infinite loop constructing an instance of
 * `B` to set on `A`, a second instance of `A` to set on
 * `B`, a second instance of `B` to set on the second instance of
 * `A`, and so on.
 *
 *
 * A conservative injector might detect the circular dependency at build
 * time and generate an error, at which point the programmer could break the
 * circular dependency by injecting [Provider&amp;lt;A&gt;][Provider] or `Provider<B>` instead of `A` or `B` respectively. Calling [ ][Provider.get] on the provider directly from the constructor or
 * method it was injected into defeats the provider's ability to break up
 * circular dependencies. In the case of method or field injection, scoping
 * one of the dependencies (using [singleton scope][Singleton], for
 * example) may also enable a valid circular relationship.
 *
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FIELD
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Inject