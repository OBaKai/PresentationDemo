Android5.0+ 实现系统级、app级截屏、录屏  

1、 系统级别截屏、录屏的实现   
MediaProjection创建VirtualDisplay，并使用ImageReader进行截屏、录屏    
该方法需弹出权限框提醒确认  
![image]( https://github.com/OBaKai/PresentationDemo/blob/master/gif/mp.gif?raw=true)

2、 App级别的截屏、录屏的实现    
通过DisplayManager创建VirtualDisplay，并使用ImageReader进行截屏、录屏     
该方法无需弹出权限框提醒  
但是只能截取到VirtualDisplay内自己进程的内容，比如presentation。（该逻辑可以实现双屏异显的截屏）  
![image]( https://github.com/OBaKai/PresentationDemo/blob/master/gif/dm.gif?raw=true)
