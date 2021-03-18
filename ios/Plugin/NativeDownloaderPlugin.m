#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(NativeDownloaderPlugin, "NativeDownloader",
           CAP_PLUGIN_METHOD(echo, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(add, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(start, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(pause, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(resume, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(cancel, CAPPluginReturnPromise);
)
