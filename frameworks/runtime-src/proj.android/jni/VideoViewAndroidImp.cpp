
#include "VideoView.h"

#include <jni.h>
#include "platform/android/jni/JniHelper.h"

extern "C"  {
	void Java_cn_sharedream_game_VideoView_doLuaFinishCallback(JNIEnv *env, jobject thiz,jint handle)
	{
		LuaEngine::getInstance()->getLuaStack()->executeFunctionByHandler(handle, 0);
	}
}

void VideoView::playVideo(const char* filename,int funcID)
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t,
		"cn/sharedream/game/VideoView",
		"playVideo",
		"(Ljava/lang/String;I)V"))
	{
		t.env->CallStaticVoidMethod(t.classID, t.methodID,
			t.env->NewStringUTF(filename),funcID);
	}
}
