//
// Created by lll on 2019/8/23.
//

#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <sys/stat.h>
#include <errno.h>
#include <stdio.h>
#include <sys/file.h>
#include <cstdint>
#include <regex>
#include <regex.h>
#include "mmap_writer.h"


jint throwIOException(JNIEnv *env, char *message) {
    jclass clazz;
    const char *className = "java/io/IOException";
    clazz = env->FindClass(className);
    return env->ThrowNew(clazz, message);
}

int MMAPWriter::close() {
    totalBytes += writtenBytes;
    int result = 0;
    if (ftruncate(fd, totalBytes) != 0) {
        result = -1;
    }
    if (msync(buffer, bufferSize, MS_ASYNC) != 0) {
        result = -1;
    }
    if (munmap(buffer, bufferSize) != 0) {
        result = -1;
    }
    if (fd != -1) {
        flock(fd, LOCK_UN);
    }
    buffer = NULL;
    LOGD("flush close writtenBytes %ld fileLength %d", writtenBytes, (int) fileLength);
    writtenBytes = 0;
    totalBytes = 0;
    return result;
}

int MMAPWriter::write(jbyte *bytes, jint offset, jlong len) {
    if (buffer == NULL) {
        return -1;
    } else {
        //如果剩余空间不够，那么递归写，填充完剩余空间之后重新映射，然后再写剩余的数据
        if (writtenBytes + len > bufferSize) {
            size_t i = len - ((writtenBytes + len) - bufferSize);
            memcpy(buffer + writtenBytes, bytes + offset, i);
            writtenBytes += i;
            LOGD("write tmp %ld writtenBytes %ld i %ld len %ld", (long) len, writtenBytes, i, len);
            if (flush() != 0) {
                return -2;
            } else {
                write(bytes, static_cast<jint>(offset + i), len - i);
            }
        } else {
            memcpy(buffer + writtenBytes, bytes + offset, (size_t) len);
            writtenBytes += len;
        }
        LOGD("write %ld writtenBytes %ld", (long) len, writtenBytes);
    }
    return 0;
}

MMAPWriter::~MMAPWriter() {
    if (buffer != NULL) {
        munmap(buffer, bufferSize);
    }
    if (fd != -1) {
        flock(fd, LOCK_UN);
        ::close(fd);
        fd = -1;
    }
    LOGD("release %ld", writtenBytes);
}

int MMAPWriter::init(const char *path, size_t pageCount) {
    this->bufferSize = page_size * pageCount;
    this->path = path;

    //打开文件
    fd = open(path, O_RDWR | O_CREAT/* | S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP*/);
    if (fd == -1) {
        return -1;
    }
    //加锁，防止同一个文件被多次映射
    if (flock(fd, LOCK_EX | LOCK_NB) != 0) {
        ::close(fd);
        return -2;
    }
    LOGD("open file success %d", fd);

    fileLength = lseek(fd, 0, SEEK_END);
    LOGD("truncate file success fileLength %d", (int) fileLength);

    //获取文件大小，并且用内存页的大小取模，
    //让下次写的时候不要覆盖之前的数据
    writtenBytes = fileLength % page_size;
    //将fileLength 对齐到内存页的倍数
    fileLength = lseek(fd, fileLength - writtenBytes, SEEK_SET);
    totalBytes = fileLength;
    //分配空文件
    if (ftruncate(fd, fileLength + bufferSize) != 0) {
        ::close(fd);
        return -3;
    }

    LOGD("truncate file success fileLength %d", (int) fileLength);
    fsync(fd);

    char *tempBuffer = (char *) mmap(0, bufferSize, PROT_WRITE | PROT_READ, MAP_SHARED, fd,
                                     fileLength);
    if (tempBuffer == MAP_FAILED) {
        return -4;
    }
    LOGD("mmap success %d", (int) bufferSize);
    buffer = tempBuffer;

    return 0;
}

int MMAPWriter::flush() {
    if (writtenBytes == 0)
        return 0;
    LOGD("flush fileLength %ld \t totalBytes %ld \t writtenBytes %ld", fileLength, totalBytes,
         writtenBytes);
    if (writtenBytes == bufferSize) {
        fileLength += bufferSize;
        totalBytes += writtenBytes;
        if (ftruncate(fd, fileLength + bufferSize) != 0) {
            return -1;
        }
        if (msync(buffer, bufferSize, MS_ASYNC) != 0) {
            return -2;
        }
        if (munmap(buffer, bufferSize) != 0) {
            return -3;
        }
        buffer = (char *) mmap(0, bufferSize, PROT_WRITE | PROT_READ, MAP_SHARED, fd,
                               fileLength);
        if (buffer == MAP_FAILED) {
            return -4;
        }
        LOGD("flush writtenBytes %ld fileLength %ld %ld totalBytes = %ld",
             writtenBytes, fileLength, (fileLength + bufferSize), totalBytes);
        writtenBytes = 0;
    } else {
        LOGD("skip flush fileLength %ld \t totalBytes %ld \t writtenBytes %ld", fileLength,
             totalBytes, writtenBytes);
    }
    return 0;
}

MMAPWriter::MMAPWriter(JNIEnv *pEnv) {
    env = pEnv;
}

/**
 * 使用二分查找从整个文件中查找满足  [^\0]\0\0 此正则的字符串
 *
 * @param file
 * @return
 */
long binarySearch(FILE *file, long low, long high) {
    char buf[3] = {0};
    size_t size = sizeof(char);

    while (low <= high) {
        long middle = low + (high - low) / 2;
        fseek(file, middle, SEEK_SET);
        fread(buf, size, 3, file);
        if (buf[0] == 0) {
            high = middle - 1;
            LOGD("search high %ld", high);
        } else {
            low = middle + 1;
            LOGD("search low %ld", low);
        }
        if (buf[0] != 0 && buf[1] == 0 && buf[2] == 0) {
            LOGD("search success %ld", middle);
            return middle + 1;
        }
    }
    return -1;
}

int MMAPWriter::replaceLastNulChar(const char *path, const char *replacement) {
    FILE *file = fopen(path, "rw+");
    if (file == NULL) {
        return -1;
    }
    //如果文件的最后一个byte是 NUL char ，那么才执行文件裁减逻辑
    struct stat statbuf;
    stat(path, &statbuf);
    off_t fileLength = statbuf.st_size;
    LOGD("fileLength %s %ld", path, fileLength);
    if (fileLength <= 0) {
        fclose(file);
        return 0;
    }

    if (fseek(file, -1, SEEK_END) != 0) {
        fclose(file);
        return -2;
    }
    char buf[1] = {0};
    if (fread(buf, sizeof(char), 1, file) != 1) {
        fclose(file);
        return -3;
    }
    if (buf[0] != 0) {
        LOGD("The last character %d is not NUL and does not need to be cut", buf[0]);
        fclose(file);
        return 0;
    } else {
        LOGD("The last character %d is NUL and need to be cut", buf[0]);
    }

    off_t position = binarySearch(file, 0, fileLength);
    if (position < 0) {
        LOGD("search result %ld", position);
        fclose(file);
        return -4;
    }
    size_t replacementLength = strlen(replacement);
    if (position > 0) {
        LOGD("cut  0-%ld,fileLength %ld", (long) position, fileLength);
        if (ftruncate(fileno(file), position + replacementLength) != 0) {
            LOGD("cut error 0-%ld,fileLength %ld", (long) position, fileLength);
            return -5;
        }
    }
    if (replacementLength > 0 && fseek(file, position, SEEK_SET) == 0) {
        fwrite(replacement, sizeof(char), replacementLength, file);
    }
    fclose(file);
    return 0;
}

long MMAPWriter::getWrittenBytes() {
    return writtenBytes;
}

extern "C" void
Java_com_erlei_logger_writer_MMAPLogWriter_nClose(JNIEnv *env, jobject, jlong writer_) {
    LOGD("Java_com_erlei_logger_writer_MMAPLogWriter_nClose %ld", (long) writer_);
    if (writer_ == 0)return;
    MMAPWriter *writer = (MMAPWriter *) writer_;
    int result = writer->close();
    delete writer;
    if (result != 0) throwIOException(env, strerror(errno));
    LOGD("Java_com_erlei_logger_writer_MMAPLogWriter_nClose");
}


extern "C" jlong
Java_com_erlei_logger_writer_MMAPLogWriter_nInit(JNIEnv *env, jobject, jstring path_,
                                                 jint pageCount_) {
    const char *path = env->GetStringUTFChars(path_, 0);
    size_t pageCount = static_cast<size_t>(pageCount_);
    long result = -1;
    MMAPWriter *writer = new MMAPWriter(env);
    if (writer->init(path, pageCount) == 0) {
        result = 0;
    } else {
        delete writer;
    }

    env->ReleaseStringUTFChars(path_, path);
    LOGD("Java_com_erlei_logger_writer_MMAPLogWriter_nInit");
    if (result == -1) {
        return throwIOException(env, strerror(errno));
    } else {
        return reinterpret_cast<jlong>(writer);
    }
}

extern "C" void
Java_com_erlei_logger_writer_MMAPLogWriter_nWrite(JNIEnv *env, jobject, jlong writer_,
                                                  jbyteArray buffer_, jint off, jint len) {
    jbyte *buffer = env->GetByteArrayElements(buffer_, NULL);
    MMAPWriter *writer = (MMAPWriter *) writer_;
    int result = writer->write(buffer, off, len);
    env->ReleaseByteArrayElements(buffer_, buffer, 0);
    if (result != 0) throwIOException(env, strerror(errno));
}

extern "C" void
Java_com_erlei_logger_writer_MMAPLogWriter_nReplaceLastNulChar(JNIEnv *env, jclass,
                                                               jstring path_,
                                                               jstring replacement_) {
    const char *path = env->GetStringUTFChars(path_, 0);
    const char *replacement = env->GetStringUTFChars(replacement_, 0);

    int result = MMAPWriter::replaceLastNulChar(path, replacement);

    env->ReleaseStringUTFChars(path_, path);
    env->ReleaseStringUTFChars(replacement_, replacement);
    LOGD("replaceLastNulChar %d", result);
    if (result != 0) throwIOException(env, strerror(errno));
}

extern "C" jlong
Java_com_erlei_logger_writer_MMAPLogWriter_nGetWrittenBytes(JNIEnv *, jobject ,
                                                            jlong writer_) {
    if (writer_ == 0)return 0;
    MMAPWriter *writer = (MMAPWriter *) writer_;
    return writer->getWrittenBytes();

}
