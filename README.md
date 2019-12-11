通过VirtualDisplay截屏

1、
MediaProjection创建VirtualDisplay  
该方法需弹出权限框提醒  
可以实现系统截屏  

2、
通过DisplayManager创建VirtualDisplay
该方法无需弹出权限框提醒
但是只能截取到自己进程的东西，比如自己写的presentation。（该逻辑应该可以实现双屏异显的截屏吧）
