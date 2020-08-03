#### Logger

基于[orhanobut logger](https://github.com/orhanobut/logger) 保留原有功能的基础上修改而来的日志库，主要优化了一些性能。例如如果关闭了日志输出，将不会进行字符串拼接，还有异步LogAdapter，
增强了日志存储相关的功能，可以方便的将日志存储到文件。可以选择存储为json或者txt 文件，使用了mmap技术。（根据手机差异，性能相比于FileWriter有不同程度的提升）

还有就是

不用每次设置tag！！！

不用每次设置tag！！！

不用每次设置tag！！！

```java
LoggerFactory#printer()
LoggerFactory#argsFormatter()
LoggerFactory#addLogAdapter()
LoggerFactory#clearLogAdapters()
LoggerFactory#create()
LoggerFactory#create(Object obj)
LoggerFactory#create(Class clazz)
LoggerFactory#create(String tag)
```



Logger.java

```JAVA
Logger#d(String msg, Object... args)
Logger#e(String msg, Object... args)
Logger#e(java.lang.Throwable, String msg, Object... args)
Logger#i(String msg, Object... args)
Logger#v(String msg, Object... args)
Logger#w(String msg, Object... args)
Logger#json(String msg)
Logger#wtf(String msg, Object... args)
Logger#xml(String msg)
Logger#args(Object... args)//自动格式化
```






#### TODO

- [ ] 添加 lint 检查 避免直接使用 android.util.Log.java 和 System.out.print

- [ ] 日志加密存储
   
   1. 把 openssl 的 aes 模块（AES-NI使用硬件指令集加密）抽取出来。作为日志加密模块
- [ ] 日志删除策略
   
   1. 设置缓存大小，缓存天数，过期自动删除
- [ ] 日志自动上传
   
   1. 方便的日志上传api，可根据id，日期上传（当收到透传消息的时候进行上传）
- [ ] 换一种mmap的使用姿势。
   1. 目前是直接映射内存到指定日志文件，当内存写满之后重新映射到文件的下一部分。没有使用buffer . 然而不异步写的话，与美团的[Logan](https://github.com/Meituan-Dianping/Logan/) 还是有很大性能差距 （主要是Logan实时加密日志，只需要写很少的数据即可）
   
      ```
      30万行日志。不压缩的情况下json文件26M ， logan 压缩之后只有750KB, logan日志文件内容没有看过，也可能跟这个有关
      ```
   
      ```
      D/PerformanceTester: cost 8397ms 	35/s 	memory 3.5M 	json_file 
      D/PerformanceTester: -----------------------------------------------------
      D/PerformanceTester: cost 7425ms 	40/s 	memory 3.9M 	json_mmap 
      D/PerformanceTester: -----------------------------------------------------
      D/PerformanceTester: cost 2639ms 	113/s 	memory 2.8M 	logan
      D/PerformanceTester: -----------------------------------------------------
      D/PerformanceTester: cost 9109ms 	32/s 	memory 2.6M 	txt_file
      D/PerformanceTester: -----------------------------------------------------
      D/PerformanceTester: cost 6786ms 	44/s 	memory 3.6M 	txt_mmap
      D/PerformanceTester: -----------------------------------------------------
      D/PerformanceTester: cost 766ms 	391/s 	memory 74.7M 	async_json_file
      D/PerformanceTester: -----------------------------------------------------
      D/PerformanceTester: cost 769ms 	390/s 	memory 70.6M 	async_json_mmap
      D/PerformanceTester: -----------------------------------------------------
      ```
- [] JNI 日志转发到Java层  
      

