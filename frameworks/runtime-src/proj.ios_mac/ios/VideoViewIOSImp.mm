//
// Created by liangwei on 14-7-21.
//
#include "VideoView.h"
#import "VideoViewIOSImp.h"
#import <MediaPlayer/MediaPlayer.h>

@implementation VideoViewIOSImp

-(void) doMovieFinishedNotification:(NSNotification*)aNotification{
    MPMoviePlayerController *movieplayer = [aNotification object];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:MPMoviePlayerPlaybackDidFinishNotification object:movieplayer];
    
    [movieplayer.view removeFromSuperview];
    [movieplayer release];
    
    movieFinishedCallback(luaFuncID);
    [self release];
    
}

- (void)handleTap:(UITapGestureRecognizer *)recognizer {
    CCLOG("tap:%f",[player currentPlaybackTime]);
    if([player currentPlaybackTime] > 5)
        [player stop];
    
}

-(void) playVideo:(const char*) filename luaFuncID:(int) funcID callback:(void (*)(int)) callback{
    
    movieFinishedCallback = callback;
    luaFuncID = funcID;
    
    player = [ [ MPMoviePlayerController alloc]initWithContentURL:[NSURL fileURLWithPath:[NSString stringWithUTF8String:filename]]];
    
    bool showControls= false;
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(doMovieFinishedNotification:)
                                                 name:MPMoviePlayerPlaybackDidFinishNotification
                                               object:player];
    
    UIView* currentView = [UIApplication sharedApplication].keyWindow;
    
    player.fullscreen = true;
    player.movieSourceType = MPMovieSourceTypeFile;
    player.scalingMode = MPMovieScalingModeAspectFill;
    player.controlStyle = showControls ? MPMovieControlStyleFullscreen : MPMovieControlStyleNone;
    
    CGRect bounds = [[UIScreen mainScreen] bounds];
    [player.view setCenter:CGPointMake(bounds.size.width/2, bounds.size.height/2)];
    
    if (bounds.size.width>bounds.size.height){
        [player.view setBounds:CGRectMake(0, 0, bounds.size.width, bounds.size.height)];
    }else{
        [player.view setBounds:CGRectMake(0, 0, bounds.size.height, bounds.size.width)];
        [player.view setTransform:CGAffineTransformMakeRotation(M_PI / 2)];
    }
    [player prepareToPlay];
    
    if ([player respondsToSelector:@selector(view)]) {
        
        [currentView addSubview:player.view];
        player.shouldAutoplay=TRUE;
    }
    [player play];
    
    for (UIView * view in player.view.subviews) {
        
        UITapGestureRecognizer * recognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
        [view addGestureRecognizer:recognizer];
    }
    
}
@end

extern "C"  {
    void doLuaFinishCallback(int handle)
    {
        LuaEngine::getInstance()->getLuaStack()->executeFunctionByHandler(handle, 0);
    }
}

void VideoView::playVideo(const char* filename,int funcID)
{
    std::string filePath = CCFileUtils::getInstance()->fullPathForFilename(filename);
    VideoViewIOSImp* ply =[[VideoViewIOSImp alloc] init];
    [ply playVideo : filePath.c_str() luaFuncID:funcID callback:doLuaFinishCallback];
}
