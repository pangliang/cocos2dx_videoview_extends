#include "VideoView.h"
#include "tolua_fix.h"
#include "cocos2d.h"
#include "CCLuaStack.h"
#include "CCLuaValue.h"
#include "CCLuaEngine.h"

using namespace cocos2d;

#if (CC_TARGET_PLATFORM == CC_PLATFORM_ANDROID)
#include <jni.h>
#include "platform/android/jni/JniHelper.h"
extern "C"  {
	void Java_cn_sharedream_game_VideoView_doLuaFinishCallback(JNIEnv *env, jobject thiz,jint handle)
	{
		LuaEngine::getInstance()->getLuaStack()->executeFunctionByHandler(handle, 0);
	}
}
#endif

static int tolua_VideoView_play00(lua_State* tolua_S)
{
    tolua_Error tolua_err;
    if (
            !tolua_isusertable(tolua_S,1,"VideoView",0,&tolua_err) ||
            !tolua_isstring(tolua_S,2,0,&tolua_err) ||
            !toluafix_isfunction(tolua_S,3,"LUA_FUNCTION",0,&tolua_err) ||
            !tolua_isnoobj(tolua_S,4,&tolua_err)
            ){
    	 tolua_error(tolua_S,"#ferror in function 'VideoView'.",&tolua_err);
    	 return 0;
    }
    else
    {
        {
        	const char* vedioFileName = ((const char*)  tolua_tostring(tolua_S,2,0));
        	LUA_FUNCTION funcID = (toluafix_ref_function(tolua_S,3,0));
			#if (CC_TARGET_PLATFORM == CC_PLATFORM_ANDROID)
					JniMethodInfo t;
					if (JniHelper::getStaticMethodInfo(t,
						"cn/sharedream/game/VideoView",
						"playVideo",
						"(Ljava/lang/String;I)V"))
					{
						t.env->CallStaticVoidMethod(t.classID, t.methodID,
							t.env->NewStringUTF(vedioFileName),funcID);
					}
			#endif
        }
    }
    return 1;
}


static void tolua_reg_types (lua_State* tolua_S)
{
    tolua_usertype(tolua_S,"VideoView");
}

TOLUA_API int tolua_videoview_extension_open (lua_State* tolua_S)
{
    tolua_open(tolua_S);
    tolua_reg_types(tolua_S);  //注册自定义类型名字
    tolua_module(tolua_S,NULL,0);
    tolua_beginmodule(tolua_S,NULL);

    tolua_cclass(tolua_S, "VideoView", "VideoView", "CCObject", NULL);
    tolua_beginmodule(tolua_S,"VideoView");
    tolua_function(tolua_S,"play",tolua_VideoView_play00);
    tolua_endmodule(tolua_S);

    tolua_endmodule(tolua_S);
    return 1;
}




