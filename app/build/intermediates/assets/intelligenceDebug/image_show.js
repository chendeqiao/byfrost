var __byfrost_browser__ = {
    openimages : function(targetImageUrl) {
      try {
        var images = document.getElementsByTagName('img');
        var imgSrcList = [];
        for(var i = 0; i < images.length; i++) {
             imgSrcList.push(images[i].src);
           }
        if(imgSrcList.length === 0){
           imgSrcList.push(targetImageUrl);
         }
        imagelistener.receiveImagesArray(JSON.stringify(imgSrcList));
        }catch (e) {
          imagelistener.receiveImagesArray(JSON.stringify([targetImageUrl]));
        }
    },
     getBlobAsBase64 : function(blobUrl) {
            var xhr = new XMLHttpRequest();
                   xhr.open('GET', blobUrl, true);
                   xhr.responseType = 'blob';
                   xhr.onload = function() {
                       var reader = new FileReader();
                       reader.readAsDataURL(xhr.response);
                       reader.onload = function() {
                        imagelistener.onBase64ImageReceived(reader.result);
                           callback();
                       };
                   };
                   xhr.send();
        },
  }