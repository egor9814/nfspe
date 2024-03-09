import kotlinx.cinterop.*
import ru.egor9814.app.Main

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) = memScoped {
    try {
        val code = Main(args)
        if (code != 0) {
            println("Application exited with code $code")
        }
    } catch (err: Throwable) {
        println("Application crashed with error: ${err.message}")
        err.printStackTrace()
    }
}
