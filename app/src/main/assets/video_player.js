var __byfrost_browser_object__ = function() {
    var objects = document.getElementsByTagName('video');
    if (typeof objects === "undefined" || objects === null
        || objects.length !== 1) {
        return null;
    }
    var object = objects[0];
    if (typeof object === "undefined" || object === null) {
        return null;
    }
    return object;
};
var __byfrost_browser__ = {
    getMediaDuration : function() {
    var videoObject = __byfrost_browser_object__();
        if (videoObject === null) {
            return -1;
        }
        var duration = videoObject.duration;
        if (!isNaN(duration)) {
            __byfrost__.getMediaDuration(duration);
        }
    },

    seek : function(time) {
       var videoObject = __byfrost_browser_object__();
        if (videoObject === null) {
            return -1;
        }
        videoObject.currentTime += time;
    },

    seekTo : function(time) {
      var videoObject = __byfrost_browser_object__();
        if (videoObject === null) {
            return -1;
        }
        videoObject.currentTime = time;
    },

    getCurrentPlayTime : function() {
       var videoObject = __byfrost_browser_object__();
        if (videoObject === null) {
            return -1;
        }
        var currentTime = videoObject.currentTime;
        if (!isNaN(currentTime)) {
            __byfrost__.getCurrentPlayTime(currentTime);
        }
    },

     getVideoUrl : function() {
            var videoObject = __byfrost_browser_object__();
            if (videoObject === null) {
                return -1;
            }
            var videoUrl = videoObject.currentSrc;
            var videosrc = videoObject.src;
            __byfrost__.getVideoUrl(videoUrl,videosrc);
        },

    retrieveVideo : function() {
        var videoObject = __byfrost_browser_object__();
        if (videoObject === null) {
            __byfrost__.canGetVideoElement(false);
        } else {
            __byfrost__.canGetVideoElement(true);
        }
    },

    exitFullScreen : function() {
        document.webkitCancelFullScreen();
    },

    pause : function() {
        var videoObject = __byfrost_browser_object__();
        if (videoObject === null) {
            return -1;
        }
        videoObject.pause();
    },

    isPaused : function() {
       var videoObject = __byfrost_browser_object__();
        if (videoObject === null) {
            return -1;
        }
        var paused = videoObject.paused;
        if (paused != "undefined" && paused != null) {
            __byfrost__.isPaused(paused);
        }
    },

    play : function() {
        var videoObject = __byfrost_browser_object__();
        if (videoObject === null) {
            return -1;
        }
        videoObject.play();
    },

    playEvent : function() {
        __byfrost__.isPaused(false);
        __byfrost__.played();
    },

    pauseEvent : function() {
        __byfrost__.isPaused(true);
        __byfrost__.paused();
    },

    timeupdateEvent : function() {
        var currentTime = __byfrost_browser_object__().currentTime;
        if (!isNaN(currentTime)) {
            __byfrost__.getCurrentPlayTime(currentTime);
        }
    },

    registerListener : function() {
        var videoObject = __byfrost_browser_object__();
        if (videoObject === null) {
            return -1;
        }
        videoObject.addEventListener('play', this.playEvent);
        videoObject.addEventListener('pause', this.pauseEvent);
        videoObject.addEventListener('timeupdate', this.timeupdateEvent);
    },

    unregisterListener : function() {
        var videoObject = __byfrost_browser_object__();
        if (videoObject === null) {
            return -1;
        }
        videoObject.removeEventListener('play', this.playEvent);
        videoObject.removeEventListener('pause', this.pauseEvent);
        videoObject.removeEventListener('timeupdate', this.timeupdateEvent);
    },


};

var __byfrost_media_hack__ = {
        getVideoShadowStyle : function() {
            if (__byfrost_browser_object__() === null) {
                return -1;
            }
            var style = window.getComputedStyle(__byfrost_browser_object__(), '-webkit-media-controls');
            return style;
        },

        replaceVideoShadowStyle : function() {
            var videoObject = __byfrost_browser_object__();
            if (videoObject.hasAttribute("controls")) {
                videoObject.removeAttribute("controls");
                webview_version_low = true;
            }

            var tagHead = document.documentElement.firstChild;
            tagStyle = document.createElement("style");
            tagStyle.setAttribute("type", "text/css");
            tagStyle.appendChild(document.createTextNode("video::-webkit-media-controls {display:none !important;}"));

            tagHead.appendChild(tagStyle);
        },

        resetVideoShadowStyle : function() {
            var videoObject = __byfrost_browser_object__();
            if (videoObject === null) {
                return -1;
            }

            var tagHead = document.documentElement.firstChild;
            var child = tagHead.firstChild;
            while (child) {
                if (child === tagStyle) {
                    tagHead.removeChild(tagStyle);
                    break;
                }
                child = child.nextSibling;
            }

            if (webview_version_low) {
                videoObject.setAttribute("controls","");
            }
        },
}