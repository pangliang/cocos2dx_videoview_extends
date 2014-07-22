#include "lua_videoview_extends.h"
#include "tolua_fix.h"
#include "cocos2d.h"
#include "CCLuaStack.h"
#include "VideoView.h"
using namespace cocos2d;

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
        	const char* filename = ((const char*)  tolua_tostring(tolua_S,2,0));
        	LUA_FUNCTION funcID = (toluafix_ref_function(tolua_S,3,0));
            VideoView::playVideo(filename,funcID);
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




