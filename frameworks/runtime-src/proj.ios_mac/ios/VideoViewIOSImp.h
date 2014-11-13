//
// Created by liangwei on 14-7-21.
//

#import <MediaPlayer/MediaPlayer.h>

@interface VideoViewIOSImp : NSObject{
    void (*movieFinishedCallback)(int);
    int luaFuncID;
    MPMoviePlayerController *player;
}
-(void) handleTap:(UITapGestureRecognizer *)recognizer;
-(void) playVideo:(const char*) filename luaFuncID:(int) funcID callback:(void (*)(int)) callback;
-(void) doMovieFinishedNotification:(NSNotification*)aNotification;
@end
