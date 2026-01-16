package fr.accoradd.gitspine.infrastructure.filesystem

import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.Guid.CLSID
import com.sun.jna.platform.win32.Guid.IID
import com.sun.jna.ptr.PointerByReference
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Path

object FileDialogs {

    private val isWindows = System.getProperty("os.name").lowercase().contains("windows")
    private val isMac = System.getProperty("os.name").lowercase().contains("mac")

    fun openDirectory(title: String = "Open Repository"): Path? {
        return when {
            isWindows -> openDirectoryWindows(title)
            isMac -> openDirectoryMac(title)
            else -> openDirectoryLinux(title)
        }
    }

    private fun openDirectoryWindows(title: String): Path? {
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
                return fallbackDirectoryChooser(title)
            }

            val fileDialog = IFileOpenDialog(pFolder.value)

            // Set options to pick folders
            val options = IntArray(1)
            fileDialog.getOptions(options)
            fileDialog.setOptions(options[0] or FOS_PICKFOLDERS or FOS_FORCEFILESYSTEM)

            // Set title
            fileDialog.setTitle(WString(title))

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

    private fun openDirectoryMac(title: String): Path? {
        System.setProperty("apple.awt.fileDialogForDirectories", "true")
        try {
            val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
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

    private fun openDirectoryLinux(title: String): Path? {
        // Try zenity first (GTK), then kdialog (KDE)
        return tryZenity(title) ?: tryKDialog(title) ?: fallbackDirectoryChooser(title)
    }

    private fun tryZenity(title: String): Path? {
        return try {
            val process = ProcessBuilder("zenity", "--file-selection", "--directory", "--title=$title")
                .redirectErrorStream(true)
                .start()
            val result = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            if (exitCode == 0 && result.isNotEmpty()) Path.of(result) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun tryKDialog(title: String): Path? {
        return try {
            val process = ProcessBuilder("kdialog", "--getexistingdirectory", "--title", title)
                .redirectErrorStream(true)
                .start()
            val result = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            if (exitCode == 0 && result.isNotEmpty()) Path.of(result) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun fallbackDirectoryChooser(title: String): Path? {
        val chooser = javax.swing.JFileChooser().apply {
            fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
            dialogTitle = title
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
