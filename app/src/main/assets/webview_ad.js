var __byfrost_browser__ = {
    closead : function(targetImageUrl) {
        var images = Array.from(document.getElementsByTagName('img'));
        for(var i = 0; i < images.length; i++) {
            if (images[i].src.toLowerCase() === targetImageUrl.toLowerCase()) {
                images[i].parentNode.removeChild(images[i]);
                if (typeof imagelistener.closeAdSuccuess === 'function') {
                   imagelistener.closeAdSuccuess(images[i].src);
                  }
               imagelistener.closeAdSuccuess(images[i].src);
               break;
              }
        }
    },

    closeads : function(imageArray) {
           var adsSet = new Set(imageArray.map(function(image) {
                       return image.toLowerCase();
                   }));
           var images = Array.from(document.getElementsByTagName('img'));
           var imagesToDelete = images.filter(function(img) {
                      return adsSet.has(img.src.toLowerCase());
               });
           imagesToDelete.forEach(function(img) {
           if (img.parentNode) {
                img.parentNode.removeChild(img);
                    }
               });
        }
};
