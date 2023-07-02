package th.`in`.jamievl.oauthclidemo

import java.nio.file.*

class RootDirectoryContext(private val rootDirectory: Path) {
    companion object {
        private const val defaultRootDirName = ".oauthdemocli"
        private const val doneFileName = ".done"
        private const val codeFileName = ".oauthcode"

        private fun getDefaultWorkingDirectory(): Path {
            val userRoot = System.getProperty("user.home")
            return Paths.get(userRoot, defaultRootDirName).normalize()
        }

        fun createWithDefaultDirectory(): RootDirectoryContext {
            return RootDirectoryContext(getDefaultWorkingDirectory())
        }
    }

    private val doneFilePath = rootDirectory.resolve(doneFileName)
    private val codeFilePath = rootDirectory.resolve(codeFileName)

    val rootDirectoryName: String
        get() = rootDirectory.normalize().toString()

    fun cleanOAuthWorkingFiles() {
        Files.deleteIfExists(doneFilePath)
        Files.deleteIfExists(codeFilePath)
    }

    fun readOAuthCode(): String {
        return Files.readString(codeFilePath)!!.trim()
    }

    fun waitForDoneFile() {
        if (Files.exists(doneFilePath)) {
            // file already exists, return immediately
            return
        }

        val watcher = FileSystems.getDefault().newWatchService()!!
        rootDirectory.register(watcher, StandardWatchEventKinds.ENTRY_CREATE)

        while (true) {
            val key = watcher.take()
            val doneFileExists = key?.pollEvents()
                ?.filterNotNull()
                ?.filter { ev -> ev.kind() != StandardWatchEventKinds.OVERFLOW}
                ?.any {ev ->
                    val evCtx = ev.context() as? Path

                    evCtx?.let {
                        doneFilePath == rootDirectory.resolve(it)
                    } ?: false
                } ?: false

            if (doneFileExists) {
                break
            }

            // else, reset key
            if (!key.reset()) {
                throw RuntimeException("Directory is no longer accessible")
            }
        }
    }
}
