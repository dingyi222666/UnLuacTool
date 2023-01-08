package com.dingyi.unluactool.engine.filesystem

import com.dingyi.unluactool.core.event.internal.EventConnectionImpl
import org.apache.commons.lang3.SystemUtils
import org.apache.commons.vfs2.*
import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.provider.AbstractFileProvider
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider
import org.apache.commons.vfs2.provider.LocalFileProvider
import org.apache.commons.vfs2.provider.UriParser
import org.apache.commons.vfs2.provider.local.GenericFileNameParser
import org.apache.commons.vfs2.provider.local.LocalFileName
import org.apache.commons.vfs2.provider.local.LocalFileNameParser
import org.apache.commons.vfs2.provider.local.WindowsFileNameParser
import java.io.File

class UnLuacFileProvider : LocalFileProvider,AbstractOriginatingFileProvider() {

    init {
        //Compatible with windows
        fileNameParser = if (SystemUtils.IS_OS_WINDOWS) {
            WindowsFileNameParser()
        } else {
            GenericFileNameParser()
        }
    }

    companion object {
         val allCapability = listOf(
            Capability.CREATE,
            Capability.DELETE,
            Capability.RENAME,
            Capability.GET_TYPE,
            Capability.GET_LAST_MODIFIED,
            Capability.SET_LAST_MODIFIED_FILE,
            Capability.SET_LAST_MODIFIED_FOLDER,
            Capability.LIST_CHILDREN,
            Capability.READ_CONTENT,
            Capability.URI,
            Capability.WRITE_CONTENT,
            Capability.APPEND_CONTENT,
            Capability.RANDOM_ACCESS_READ,
            Capability.RANDOM_ACCESS_SET_LENGTH,
            Capability.RANDOM_ACCESS_WRITE
        )
    }


    override fun getCapabilities(): List<Capability> = allCapability


    /**
     * Creates the file system.
     */
    @Throws(FileSystemException::class)
    override fun doCreateFileSystem(
        name: FileName,
        fileSystemOptions: FileSystemOptions?
    ): FileSystem {
        // Create the file system
        val rootName = name as LocalFileName
        return UnLuacFileSystem(rootName, rootName.rootFile, fileSystemOptions).apply {
            init()
        }
    }

    /**
     * Finds a local file.
     *
     * @param file The File to locate.
     * @return the located FileObject.
     * @throws FileSystemException if an error occurs.
     */
    @Throws(FileSystemException::class)
    override fun findLocalFile(file: File): FileObject? {
        return findLocalFile(UriParser.encode(file.absolutePath))
        // return findLocalFile(file.getAbsolutePath());
    }

    /**
     * Finds a local file, from its local name.
     *
     * @param name The name of the file to locate.
     * @return the located FileObject.
     * @throws FileSystemException if an error occurs.
     */
    @Throws(FileSystemException::class)
    override fun findLocalFile(name: String): FileObject? {
        val scheme = "unluac:"
        val uri = StringBuilder(name.length + scheme.length)
        uri.append(scheme)
        uri.append(name)
        val fileName = parseUri(null, uri.toString())
        return findFile(fileName, null)
    }


    /**
     * Determines if a name is an absolute file name.
     *
     * @param name The file name.
     * @return true if the name is absolute, false otherwise.
     */
    override fun isAbsoluteLocalName(name: String?): Boolean {
        return (fileNameParser as LocalFileNameParser).isAbsoluteName(name)
    }


    /**
     * Locates a file object, by absolute URI.
     *
     * @param baseFileObject The base file object.
     * @param uri The URI of the file to locate
     * @param fileSystemOptions The FileSystem options.
     * @return The located FileObject
     * @throws FileSystemException if an error occurs.
     */

    override fun findFile(
        baseFileObject: FileObject,
        uri: String,
        fileSystemOptions: FileSystemOptions
    ): FileObject? {
        // Parse the URI
        val name = runCatching {
            parseUri(baseFileObject.name, uri)
        }.getOrElse {
            throw FileSystemException("vfs.provider/invalid-absolute-uri.error", uri, it)
        }

        // Locate the file
        return findFile(name, fileSystemOptions)
    }



}