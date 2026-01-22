package fr.accoradd.gitspine.infrastructure.filesystem

import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.Guid.CLSID
import com.sun.jna.platform.win32.Guid.IID
import com.sun.jna.ptr.PointerByReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Path

object FileDialogs {

    private val isWindows = System.getProperty("os.name").lowercase().contains("windows")
    private val isMac = System.getProperty("os.name").lowercase().contains("mac")

    suspend fun openDirectory(
        title: String = "Open Repository",
        initialDirectory: Path? = null
    ): Path? = withContext(Dispatchers.IO) {
        when {
            isWindows -> openDirectoryWindows(title, initialDirectory)
            isMac -> openDirectoryMac(title, initialDirectory)
            else -> openDirectoryLinux(title, initialDirectory)
        }
    }

    private fun openDirectoryWindows(title: String, initialDirectory: Path?): Path? {
        Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_APARTMENTTHREADED)
        try {
            val pFolder = PointerByReference()

            // CLSID_FileOpenDialog
            val clsid = CLSID("{DC1C5A9C-E88A-4dde-A5A1-60F82A20AEF7}")
            // IID_IFileOpenDialog
            val iid = IID("{d57c7288-d4ad-4768-be02-9d969532d960}")

            var hr = Ole32.INSTANCE.CoCreateInstance(
                clsid,
                null,
                WTypes.CLSCTX_INPROC_SERVER,
                iid,
                pFolder
            )

            if (COMUtils.FAILED(hr)) {
                return fallbackDirectoryChooser(title, initialDirectory)
            }

            val fileDialog = IFileOpenDialog(pFolder.value)

            // Set options to pick folders
            val options = IntArray(1)
            fileDialog.getOptions(options)
            fileDialog.setOptions(options[0] or FOS_PICKFOLDERS or FOS_FORCEFILESYSTEM)

            // Set title
            fileDialog.setTitle(WString(title))

            // Set initial directory if provided
            if (initialDirectory != null) {
                try {
                    val pShellItem = PointerByReference()
                    val initialPath = initialDirectory.toAbsolutePath().toString()
                    hr = SHCreateItemFromParsingName(
                        WString(initialPath),
                        Pointer.NULL,
                        IID("{43826d1e-e718-42ee-bc55-a1e261c37bfe}"), // IID_IShellItem
                        pShellItem
                    )
                    if (COMUtils.SUCCEEDED(hr)) {
                        fileDialog.setFolder(pShellItem.value)
                    }
                } catch (_: Exception) {
                    // Ignore if setting initial directory fails
                }
            }

            // Show dialog
            hr = fileDialog.show(null)

            if (COMUtils.SUCCEEDED(hr)) {
                val pItem = PointerByReference()
                hr = fileDialog.getResult(pItem)
                if (COMUtils.SUCCEEDED(hr)) {
                    val shellItem = IShellItem(pItem.value)
                    val pathPtr = PointerByReference()
                    shellItem.getDisplayName(SIGDN_FILESYSPATH, pathPtr)
                    val path = pathPtr.value.getWideString(0)
                    Ole32.INSTANCE.CoTaskMemFree(pathPtr.value)
                    shellItem.release()
                    fileDialog.release()
                    return Path.of(path)
                }
            }

            fileDialog.release()
            return null
        } finally {
            Ole32.INSTANCE.CoUninitialize()
        }
    }

    private fun openDirectoryMac(title: String, initialDirectory: Path?): Path? {
        System.setProperty("apple.awt.fileDialogForDirectories", "true")
        try {
            val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
            if (initialDirectory != null) {
                dialog.directory = initialDirectory.toAbsolutePath().toString()
            }
            dialog.isVisible = true
            val directory = dialog.directory
            val file = dialog.file
            return if (directory != null && file != null) {
                File(directory, file).toPath()
            } else {
                null
            }
        } finally {
            System.setProperty("apple.awt.fileDialogForDirectories", "false")
        }
    }

    private fun openDirectoryLinux(title: String, initialDirectory: Path?): Path? {
        // Try zenity first (GTK), then kdialog (KDE)
        return tryZenity(title, initialDirectory)
            ?: tryKDialog(title, initialDirectory)
            ?: fallbackDirectoryChooser(title, initialDirectory)
    }

    private fun tryZenity(title: String, initialDirectory: Path?): Path? {
        return try {
            val args = mutableListOf("zenity", "--file-selection", "--directory", "--title=$title")
            if (initialDirectory != null) {
                args.add("--filename=${initialDirectory.toAbsolutePath()}/")
            }
            val process = ProcessBuilder(args)
                .redirectErrorStream(true)
                .start()
            val result = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            if (exitCode == 0 && result.isNotEmpty()) Path.of(result) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun tryKDialog(title: String, initialDirectory: Path?): Path? {
        return try {
            val args = mutableListOf("kdialog", "--getexistingdirectory", "--title", title)
            if (initialDirectory != null) {
                args.add(initialDirectory.toAbsolutePath().toString())
            }
            val process = ProcessBuilder(args)
                .redirectErrorStream(true)
                .start()
            val result = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            if (exitCode == 0 && result.isNotEmpty()) Path.of(result) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun fallbackDirectoryChooser(title: String, initialDirectory: Path? = null): Path? {
        val chooser = javax.swing.JFileChooser().apply {
            fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
            dialogTitle = title
            if (initialDirectory != null) {
                currentDirectory = initialDirectory.toFile()
            }
        }
        val result = chooser.showOpenDialog(null)
        return if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile.toPath()
        } else {
            null
        }
    }

    // Windows COM constants
    private const val FOS_PICKFOLDERS = 0x20
    private const val FOS_FORCEFILESYSTEM = 0x40
    private const val SIGDN_FILESYSPATH = 0x80058000.toInt()

    // SHCreateItemFromParsingName from shell32.dll
    private fun SHCreateItemFromParsingName(
        pszPath: WString,
        pbc: Pointer?,
        riid: IID,
        ppv: PointerByReference
    ): WinNT.HRESULT {
        val shell32 = com.sun.jna.Native.load("shell32", Shell32Extended::class.java)
        return shell32.SHCreateItemFromParsingName(pszPath, pbc, riid, ppv)
    }

    private interface Shell32Extended : com.sun.jna.Library {
        fun SHCreateItemFromParsingName(
            pszPath: WString,
            pbc: Pointer?,
            riid: IID,
            ppv: PointerByReference
        ): WinNT.HRESULT
    }

    // IFileOpenDialog wrapper
    private class IFileOpenDialog(pointer: Pointer) : com.sun.jna.platform.win32.COM.Unknown(pointer) {
        fun show(hwnd: WinDef.HWND?): WinNT.HRESULT {
            return WinNT.HRESULT(_invokeNativeInt(3, arrayOf(pointer, hwnd)))
        }

        fun setOptions(options: Int): WinNT.HRESULT {
            return WinNT.HRESULT(_invokeNativeInt(9, arrayOf(pointer, options)))
        }

        fun getOptions(options: IntArray): WinNT.HRESULT {
            return WinNT.HRESULT(_invokeNativeInt(10, arrayOf(pointer, options)))
        }

        fun setTitle(title: WString): WinNT.HRESULT {
            return WinNT.HRESULT(_invokeNativeInt(17, arrayOf(pointer, title)))
        }

        fun setFolder(psi: Pointer): WinNT.HRESULT {
            return WinNT.HRESULT(_invokeNativeInt(12, arrayOf(pointer, psi)))
        }

        fun getResult(pItem: PointerByReference): WinNT.HRESULT {
            return WinNT.HRESULT(_invokeNativeInt(20, arrayOf(pointer, pItem)))
        }

        fun release() {
            _invokeNativeInt(2, arrayOf(pointer))
        }
    }

    // IShellItem wrapper
    private class IShellItem(pointer: Pointer) : com.sun.jna.platform.win32.COM.Unknown(pointer) {
        fun getDisplayName(sigdnName: Int, name: PointerByReference): WinNT.HRESULT {
            return WinNT.HRESULT(_invokeNativeInt(5, arrayOf(pointer, sigdnName, name)))
        }

        fun release() {
            _invokeNativeInt(2, arrayOf(pointer))
        }
    }
}
