
var onPlayClick = function(id) {
    if ( selectVideo (id) ) 
        myapp.PlayVideo(id);
}

var onOwnerClick = function(id) {
    myapp.ToastOwner(id);
}

var SharebySMS = function(id) {
    if ( selectVideo (id) ) 
        myapp.SharebySMS(id);
}

var ShareonFB = function(id) {
    if ( selectVideo (id) ) 
        myapp.ShareonFB(id);
}

var LikeonFB = function(id) {
    if ( selectVideo (id) ) 
        myapp.LikeonFB(id);
}

// ---- internal functions --------

var poverlay;
var selectVideo = function(id) {
    var overlay = document.getElementById('o'+id);
    if (overlay != null && overlay != undefined) {
        if (poverlay != null) {
            poverlay.setAttribute('class', 'overlaid_details');
        }
        overlay.setAttribute('class', 'overlaid_details glow');
        // overlay.appendChild(movspinner);
        poverlay = overlay;
        return true;
    }
    return false;
}
