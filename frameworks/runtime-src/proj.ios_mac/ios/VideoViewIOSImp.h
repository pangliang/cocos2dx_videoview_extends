//
// Created by liangwei on 14-7-21.
//

@interface VideoViewIOSImp : NSObject{
    void (*movieFinishedCallback)(int);
    int luaFuncID;
}
-(void) playVideo:(const char*) filename luaFuncID:(int) funcID callback:(void (*)(int)) callback;
-(void) doMovieFinishedNotification:(NSNotification*)aNotification;
@end
