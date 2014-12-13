
#cocos2dx 播放视频插件

##停止维护

貌似看到cocos2dx v3 版已经自带了一个videoplayer, 所以这个就停止维护了!

##特性:

* lua绑定
* 播放完成事件回调
* 支持android, ios平台

##使用示例:

    -- 播放完之后的回调函数
    local function videoFinish()
        print("================videoFinish")
        --恢复游戏, 和原来的声音
        cc.Director:getInstance():startAnimation();
        cc.SimpleAudioEngine:getInstance():resumeMusic();
    end
    
    -- 原游戏, 声音暂停
    cc.Director:getInstance():stopAnimation();
    cc.SimpleAudioEngine:getInstance():pauseMusic();
    VideoView:play("res/video2.mp4",videoFinish)


##添加步骤:

1. 一下文件拷贝到对应的目录里

		Classes/VideoView.h
		Classes/lua_videoview_extends.h
		Classes/lua_videoview_extends.cpp
	
		#android
		proj.android/src/cn/sharedream/game/VideoView.java
		proj.android/jni/VideoViewAndroidImp.cpp
	
		#ios
		proj.ios_mac/ios/VideoViewIOSImp.h
		proj.ios_mac/ios/VideoViewIOSImp.mm

2. AppDelegate.cpp   注册VideoView 的lua 扩展

		#include "lua_videoview_extends.h"
	
		bool AppDelegate::applicationDidFinishLaunching()
		{
		    ...
	
		    auto engine = LuaEngine::getInstance();
		    ScriptEngineManager::getInstance()->setScriptEngine(engine);
	
		    //==============添加lua 扩展
		    lua_State* L = engine->getLuaStack()->getLuaState();
		    tolua_videoview_extension_open(L);
	        //==============添加lua 扩展end
	        
		    if (engine->executeScriptFile("src/main.lua")) {
		        return false;
		    }
	
		    return true;
		}

3. **android部分** 修改jni 的 Android.mk  添加VideoViewAndroidImp.cpp 和 lua_videoview_extends.cpp 编译

		LOCAL_SRC_FILES := ... \
			VideoViewAndroidImp.cpp \
			../../Classes/lua_videoview_extends.cpp \
	        ...
	        

##文件说明

	Classes/VideoView.h:

			VideoView类接口定义

	Classes/lua_videoview_extends.h(cpp): 

			将VideoView类绑定到lua
	
	proj.android/src/cn/sharedream/game/VideoView.java
			
			android 原生视频播放器实现, 播放完毕回调native callback

	proj.android/jni/VideoViewAndroidImp.cpp

			android实现, 通过jni调用 VideoView.java 的播放方法, 并实现native callback

	proj.ios_mac/ios/VideoViewIOSImp.h
	proj.ios_mac/ios/VideoViewIOSImp.mm

			ios实现, 通过 MPMoviePlayerController 进行播放
	        


