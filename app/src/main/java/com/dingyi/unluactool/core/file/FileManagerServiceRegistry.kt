package com.dingyi.unluactool.core.file

class FileManagerServiceRegistry {

    fun createOpenedFileHistoryManager() = OpenedFileManager()

    fun createOpenFileManager() = OpenFileManager()

}