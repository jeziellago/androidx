package androidx.navigation.compose

import androidx.compose.runtime.InternalComposeApi
import androidx.navigation.NavArgument
import androidx.navigation.NavDestinationDsl
import androidx.navigation.NavType

@InternalComposeApi
interface NamedNavType<T> {
    val type: T
}

/**
 * Expose only primitives [NavType].
 */
@OptIn(InternalComposeApi::class)
public enum class ComposableNavType(override val type: NavType<*>) : NamedNavType<NavType<*>> {
    StringType(NavType.StringType),
    BoolType(NavType.BoolType),
    FloatType(NavType.FloatType),
    IntType(NavType.IntType),
    LongType(NavType.LongType);
}

/**
 * DSL for constructing a new [NavArgument]
 */
@NavDestinationDsl
public class NamedNavArgumentBuilder {
    private val builder = NavArgument.Builder()
    private var _type: ComposableNavType? = null

    /**
     * Sets the ComposeNavArgType for this argument.
     *
     * If you don't set a type explicitly, it will be inferred
     * from the default value of this argument.
     */
    public var type: ComposableNavType
        set(value) {
            _type = value
            builder.setType(value.type)
        }
        get() {
            return _type ?: throw IllegalStateException("NavType has not been set on this builder.")
        }

    /**
     * Controls if this argument allows null values.
     */
    public var nullable: Boolean = false
        set(value) {
            field = value
            builder.setIsNullable(value)
        }

    /**
     * An optional default value for this argument.
     *
     * Any object that you set here must be compatible with [type], if it was specified.
     */
    public var defaultValue: Any? = null
        set(value) {
            field = value
            builder.setDefaultValue(value)
        }

    /**
     * Builds the NavArgument by calling [NavArgument.Builder.build].
     */
    public fun build(): NavArgument {
        return builder.build()
    }
}
