package com.dingyi.unluactool.core.file

class FileManagerServiceRegistry {

    fun createOpenedFileHistoryManager() = OpenedFileHistoryManager()

    fun createOpenFileManager() = OpenFileManager()

}