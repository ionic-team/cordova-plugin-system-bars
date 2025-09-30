#import <Cordova/CDV.h>
#import <UIKit/UIKit.h>
#import <objc/runtime.h>

@interface CDVViewController (SystemBars)
@property (nonatomic, strong) NSNumber *systemBarsStyle;
@property (nonatomic, strong) NSNumber *systemBarsHidden;
@end

@implementation CDVViewController (SystemBars)

- (NSNumber *)systemBarsStyle {
    return objc_getAssociatedObject(self, @selector(systemBarsStyle));
}

- (void)setSystemBarsStyle:(NSNumber *)style {
    objc_setAssociatedObject(self, @selector(systemBarsStyle), style, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
    [self setNeedsStatusBarAppearanceUpdate];
}

- (NSNumber *)systemBarsHidden {
    return objc_getAssociatedObject(self, @selector(systemBarsHidden));
}

- (void)setSystemBarsHidden:(NSNumber *)hidden {
    objc_setAssociatedObject(self, @selector(systemBarsHidden), hidden, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
    [self setNeedsStatusBarAppearanceUpdate];
}

+ (void)safeSwizzle:(SEL)originalSelector withMethod:(SEL)swizzledSelector {
    Class class = [self class];  
    Method originalMethod = class_getInstanceMethod(class, originalSelector);
    Method swizzledMethod = class_getInstanceMethod(class, swizzledSelector);

    BOOL didAddMethod =
    class_addMethod(class, originalSelector, method_getImplementation(swizzledMethod), method_getTypeEncoding(swizzledMethod));

    if (didAddMethod) {
        class_replaceMethod(class, swizzledSelector, method_getImplementation(originalMethod), method_getTypeEncoding(originalMethod));
    } else {
        method_exchangeImplementations(originalMethod, swizzledMethod);
    }
}

+ (void)load {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{      
        [self safeSwizzle:@selector(preferredStatusBarStyle) withMethod:@selector(systemBars_preferredStatusBarStyle)];
        [self safeSwizzle:@selector(prefersStatusBarHidden) withMethod:@selector(systemBars_prefersStatusBarHidden)];
    });
}

- (UIStatusBarStyle)systemBars_preferredStatusBarStyle {
    if (self.systemBarsStyle != nil) {
        return [self.systemBarsStyle integerValue];
    }
    return [self systemBars_preferredStatusBarStyle];
}

- (BOOL)systemBars_prefersStatusBarHidden {
    if (self.systemBarsHidden != nil) {
        return [self.systemBarsHidden boolValue];
    }
    return [self systemBars_prefersStatusBarHidden];
}

@end

@interface SystemBars : CDVPlugin
- (void)setStyle:(CDVInvokedUrlCommand*)command;
- (void)setHidden:(CDVInvokedUrlCommand*)command;
@end

@implementation SystemBars

- (void)pluginInitialize {
    [super pluginInitialize];
    
    NSString *currentStyle = [self.commandDelegate.settings objectForKey:[@"SystemBarsStyle" lowercaseString]];

    if (!currentStyle) {
        currentStyle = @"DEFAULT";
    }

    
    CDVInvokedUrlCommand *mockJavascriptCall = [[CDVInvokedUrlCommand alloc] initWithArguments:@[currentStyle, @"ALL"] 
                                                                             callbackId:nil 
                                                                             className:@"SystemBars" 
                                                                             methodName:@"setStyle"];
    [self setStyle:mockJavascriptCall];
}

- (void)setStyle:(CDVInvokedUrlCommand*)command {
    NSString *style = [command.arguments objectAtIndex:0];
    id insetArg = [command.arguments objectAtIndex:1];
    NSString *inset = (insetArg == nil || insetArg == [NSNull null]) ? @"ALL" : insetArg;

    // Check for DEFAULT first and convert to DARK or LIGHT based on device theme
    if ([style caseInsensitiveCompare:@"DEFAULT"] == NSOrderedSame) {
        if (@available(iOS 13.0, *)) {
            UIUserInterfaceStyle interfaceStyle = self.viewController.traitCollection.userInterfaceStyle;
            if (interfaceStyle == UIUserInterfaceStyleDark) {
                style = @"LIGHT";
            } else {
                style = @"DARK";
            }
        } else {
            style = @"DEFAULT"; // This is a nicer behavior, but we want it consistent between android and iOS. This will make the status bar style itself based on the background color of the app.
        }
    }

    UIStatusBarStyle newStyle = UIStatusBarStyleDefault;
    if ([style caseInsensitiveCompare:@"DARK"] == NSOrderedSame) {
        if (@available(iOS 13.0, *)) {
            newStyle = UIStatusBarStyleDarkContent;
        }
    } else if ([style caseInsensitiveCompare:@"LIGHT"] == NSOrderedSame) {
        newStyle = UIStatusBarStyleLightContent;
    }

    if([inset caseInsensitiveCompare:@"TOP"] == NSOrderedSame || [inset caseInsensitiveCompare:@"ALL"] == NSOrderedSame) {
        ((CDVViewController*)self.viewController).systemBarsStyle = @(newStyle);
        if (command.callbackId) {
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
        }
    }
}

- (void)setHidden:(CDVInvokedUrlCommand*)command {
    BOOL hidden = [[command.arguments objectAtIndex:0] boolValue];
    id insetArg = [command.arguments objectAtIndex:1];
    NSString *inset = (insetArg == nil || insetArg == [NSNull null]) ? @"ALL" : insetArg;

    if([inset caseInsensitiveCompare:@"TOP"] == NSOrderedSame || [inset caseInsensitiveCompare:@"ALL"] == NSOrderedSame) {
        ((CDVViewController*)self.viewController).systemBarsHidden = @(hidden);
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
    }
}

@end
