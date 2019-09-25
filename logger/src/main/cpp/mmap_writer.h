/* DO NOT EDIT THIS FILE - it is machine generated */
/* Header for class com_erlei_logger_writer_MMAPLogWriter */

#define TAG "jni-log" // 这个是自定义的LOG的标识
//#define LOG_ENABLE true
#define LOG_ENABLE false

#if LOG_ENABLE
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__)
#else
#define LOGD(...)
#define LOGI(...)
#define LOGW(...)
#define LOGE(...)
#define LOGF(...)
#endif

#include "../../../../../../android-sdk/ndk-bundle/sysroot/usr/include/jni.h"
#include <android/log.h>
#include <sys/mman.h>
#include <android/log.h>
#include <sys/mman.h>

#ifdef __cplusplus
extern "C" {
#endif

class MMAPWriter {

protected:
    const char *path = NULL;
    JNIEnv *env;
    size_t bufferSize = 0;
    char *buffer = NULL;
    off_t fileLength = 0;
    int fd = -1;
    long writtenBytes = 0;
    long totalBytes = 0;
    long page_size = sysconf(_SC_PAGE_SIZE);

public:
    MMAPWriter(JNIEnv *pEnv);

    ~MMAPWriter();

    /**
     * 初始化mmapWriter
     * @param path 文件路径
     * @param pageCount 内存页数量
     * @return 0 初始化成功
     */
    int init(const char *path, size_t pageCount);

    int write(jbyte *bytes, jint offset, jlong len);

    int flush();

    int close();

    long getWrittenBytes();

    /**
     * 替换因异常情况导致文件末尾产生的 NUL 字符
     * 已知没有调用close会导致文件末尾产生NUL字符
     * @param path 文件路径
     * @param replacement 要替换NUL的文字
     * @return 0 success
     */
    static int replaceLastNulChar(const char *path, const char *replacement);
};

JNIEXPORT jlong JNICALL
Java_com_erlei_logger_writer_MMAPLogWriter_nInit(JNIEnv *env, jobject, jstring path_,
                                                 jint pageCount);

JNIEXPORT void JNICALL
Java_com_erlei_logger_writer_MMAPLogWriter_nClose(JNIEnv *env, jobject,
                                                  jlong nativeWriter);

JNIEXPORT void JNICALL
Java_com_erlei_logger_writer_MMAPLogWriter_nWrite(JNIEnv *env, jobject, jlong nativeWriter,
                                                  jbyteArray buffer_, jint off, jint len);


JNIEXPORT void JNICALL
Java_com_erlei_logger_writer_MMAPLogWriter_nReplaceLastNulChar(JNIEnv *env, jclass,
                                                               jstring path_,
                                                               jstring replacement_);

JNIEXPORT jlong JNICALL
Java_com_erlei_logger_writer_MMAPLogWriter_nGetWrittenBytes(JNIEnv *env, jobject instance,
                                                            jlong nativeWriter);

#ifdef __cplusplus
}
#endif