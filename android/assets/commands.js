/* 
 * Local data - persists
 */

var searchtxt;
var videoidlist = []; // a list of video object id's
var vidstream;

/* Temp
 */

var movspinner;

/*
 * Event handlers
 */

var onload = false;
var onLoad = function() {
    if (onload == true) return; // avoid being called multiple times
    onload = true;

    //myapp.Log('onLoad()');
    AndroidAPIVer();
    InitLoader();
    var stream = retreivestream(); // stream == -1  =>  NO LOCAL STORAGE
    //myapp.Log('retreivestream() = ' + stream);
    if ( myapp.LoadDataStream(stream) ) {
        ShowLoader();
    }
    InitScrollLoading();
    myapp.Log('exit onLoad()');
}

var jsSetDataStream = function(stream) {
    //myapp.Log('jsSetDataStream( ' + stream + ' )');
    vidstream = stream;
    //myapp.Log('jsSetDataStream('+vidstream+')');
    ResetHTMLVideoList();
    retreivevideoidlist();
    //myapp.Log('Cache has ' + videoidlist.length + ' items.');
    for (var i = 0; i < videoidlist.length; i++) {
        var objid = videoidlist[i];
        insiderUpdateVideo ( objid );
    }
    if ( CalculateAndCheckTrigger() == false &&
         TriggerIfTooLessData() == false) {
        HideLoader();
    }
}

var jsPrepareForRefresh = function() {
    //myapp.Log('jsPrepareForRefresh()');
    ResetHTMLVideoList();
    videoidlist = [];
    savevideoidlist();
}

var jsStartLoading = function() {
    //myapp.Log('jsStartLoading('+vidstream+')');
    InitHeightVar(); // workaround: orientation changes may cause incorrect winH 
    ShowLoader(); 
    if (videolist.size == 0) {
        var htmlelement = document.getElementById('novideosmessage');
        htmlelement.style.display = 'none';
    }
    savestream();
}

var jsDoneLoading = function() {
    //myapp.Log('jsDoneLoading('+vidstream+')');
    savevideoidlist();
    if (videolist.size == 0) {
        var htmlelement = document.getElementById('novideosmessage');
        htmlelement.style.display = 'block';
    }
    if ( CalculateAndCheckTrigger() == false ) { 
        HideLoader();
    }
}

var jsUpdateVideo = function(objid) {
    //myapp.Log('jsUpdateVideo('+objid+')');
    var newitem = insiderUpdateVideo(objid);
    if (newitem) {
        videoidlist.push(objid);
        if ( CheckFetchNoMore() ) {
            //myapp.Log ("Reached MAXFETCH");
            myapp.FetchNoMore();
        }
    }
}

var jsLogout = function () {
    localStorage.clear();
}

var jsSaveVideoList = function () {
    savevideoidlist();
}

var jsSnapshot = function() {
    //myapp.Log('jsSnapshot()');
    var htmldata = document.getElementsByTagName('html')[0].innerHTML;
    myapp.DumpHTMLtoFile(htmldata);
}

var jsQueryEpochUpdate = function(stream, epoch) {
    //myapp.Log('jsQueryEpochUpdate( ' + stream + ' , ' + epoch + ' )' );
    if ( stream == vidstream ) {
        var tstr = CustomTime(epoch);
        //myapp.Log('jsQueryEpochUpdate() -> ' + tstr);
        var msg = 'searching ' + tstr + ' ...';
        UpdateLoader (msg);
    }
}

/*
 * internal functions
 */

/* Utils
 */

var insiderUpdateVideo = function(objid) {
    var jstr = myapp.GetVideoMeta(objid);
    if (jstr == null)  return; 
    //myapp.Log('received Video metadata');
    var jobj = JSON.parse(jstr);
    var id = 'j'+objid; 
    var li = document.getElementById(id);
    if ( li == null ) { // New video added to UI
        //myapp.Log('     ...new video ' + jstr);
        var li = document.createElement('li');
            li.setAttribute('class', 'videoelement');
            li.setAttribute('id', id);
        CreateVideoBlock(li,jobj);
        AppendToTimeline(li,jobj.createdtime);
        return true;
    } else { // Update existing video
        //myapp.Log('     ...update existing ' + jstr);
        li.innerHTML = '';
        CreateVideoBlock(li,jobj);
    }
    return false;
}

var CreateVideoBlock = function(lielem, jobj) {
    var duration;
    var mins = Math.floor(Number(jobj.duration)/60);
    if (mins > 0) {
        duration = mins + 'm';
    } else {
        if (Number(jobj.duration) > 0) {
            duration = jobj.duration + 's';
        } else {
            duration = '';
        }
    }
    var clikes = Number(jobj.likes);
    var canplay = jobj.canplay; 
    var title = jobj.title;
    var thumbnail = jobj.thumbnail;
    var id = jobj.objectid;
    var ownerid = jobj.ownerid;
    var islikedbyme = jobj.islikedbyme;

    /*   li --+-- thumb
     *        +-- overlaid_details 
     *        +-- details
     *        +-- desciption --+-- title
     */
    var thumb = document.createElement('img');
    if (canplay == true) {
        thumb.setAttribute('class', 'thumbnail');
    } else {
        thumb.setAttribute('class', 'thumbnail transparent');
    }
    if (!(thumbnail == "" || thumbnail == null)) {
        thumb.setAttribute('src', thumbnail);
    } else {
        //thumb.setAttribute('src', 'images/blank.png');
    }

    var overlaid_details = document.createElement('div');
        overlaid_details.setAttribute('class', 'overlaid_details');
        overlaid_details.setAttribute('id', 'o'+id);
    var ovduration = document.createElement('label');
    var ovbutton = document.createElement('img');
    if (canplay == true) {
        overlaid_details.setAttribute('onclick', 'onPlayClick("'+id+'");');
        ovbutton.setAttribute('class', 'button transparent');
        ovbutton.setAttribute('src', 'images/playbutton.png');
        ovduration.innerHTML = '<span class="duration">'+duration+'</span>';
    } else {
        ovbutton.setAttribute('class', 'button');
        ovbutton.setAttribute('src', 'images/noplaybutton.png');
        ovduration.innerHTML = '<span class="duration transparent">'+duration+'</span>';
    }

    var desc = document.createElement('div');
        desc.setAttribute('class', 'description');
        var ownerpic = myapp.GetUserPic(ownerid);
        var desctitle = document.createElement('span');
            desctitle.setAttribute('class', 'mytitle alignleft');
            desctitle.innerHTML = title;
        desc.appendChild(desctitle);
        if (ownerpic != null && ownerpic != undefined) { 
        var descpic = document.createElement('img');
            descpic.setAttribute('class', 'ownerpic');
            descpic.setAttribute('src', ownerpic);
            descpic.setAttribute('onclick', 'onOwnerClick("' + ownerid + '");');
        desc.appendChild(descpic); 
        } 
    var actions = document.createElement('div');
        actions.setAttribute('class', 'actions');
        var heart = document.createElement('div');
            heart.setAttribute('class', 'interaction notpressed alignright');
            heart.setAttribute('onclick', 'LikeonFB("'+id+'");');
        if (islikedbyme == true) {
            heart.innerHTML = 
                    '<span style="margin-left:5px;margin-top:8px;"> <font size="3">' + clikes + '</font>' + 
                    '<img class="heart" src="images/heart_fill.png" /> </span>';
        } else { 
            heart.innerHTML = 
                    '<span style="margin-left:5px;margin-top:8px;"> <font size="3">' + clikes + '</font>' + 
                    '<img class="heart" src="images/heart_empty.png" /> </span>';
        }
        var share = document.createElement('div');
            if (vidstream != 0) {
            share.setAttribute('class', 'interaction notpressed alignright');
	    share.setAttribute('onclick', 'ShareonFB("'+id+'");'); 
            } else {
            share.setAttribute('class', 'interaction disabled alignright');
            }
            share.innerHTML = 
                    '<img class="share" src="images/share.png" />' ;
/*
        var mesg = document.createElement('div');
            mesg.setAttribute('class', 'interaction notpressed alignleft');
            mesg.setAttribute('onclick', 'SharebySMS("'+id+'");'); 
            mesg.innerHTML = 
                    '<img class="sms" src="images/sms.png" />';
        actions.appendChild(mesg);
*/
        actions.appendChild(heart);
        actions.appendChild(share);
/*    var bkimg = document.createElement('img');
        bkimg.setAttribute('class', 'backimage');
        bkimg.setAttribute('src', 'images/border.png');
    actions.appendChild(bkimg);
*/

    // does this match with current search
    var found = (title.search(new RegExp(searchtxt,'i')) >= 0);
    if (found) {
        lielem.style.display = 'inline';
    } else {
        lielem.style.display = 'none';
    }

    overlaid_details.appendChild(ovbutton);
    if(duration!='') {
    overlaid_details.appendChild(ovduration);
    }
    lielem.appendChild(thumb);
    lielem.appendChild(overlaid_details);
    lielem.appendChild(desc);
    lielem.appendChild(actions);
}

/* localstorage
 */

// saves vidstream
var savevideoidlist = function() {
    //myapp.Log('savevideoidlist(' + vidstream + ')');
    if ( vidstream == undefined ) return;
    var store = "videoidlist" + vidstream;
    localStorage[store] = videoidlist;
}

var retreivevideoidlist = function() {
    var store = "videoidlist" + vidstream;
    videoidstr = localStorage[store];
    if (videoidstr == undefined || videoidstr == '') { 
        videoidlist = [];
    } else {
        videoidlist = videoidstr.split(",");
    }
}

var savestream = function() {
    localStorage["stream"] = vidstream;
}

var retreivestream = function() {
    var stream = localStorage["stream"];
    if (stream == undefined || stream == 'null') {
        return -1;
    }
    return parseInt(stream);
}

/* spinner/loader functions 
 */

var loader;

var InitLoader = function() {
    loader = document.getElementById('loader');
    if (loader.childNodes.length < 3) { // do we have a spinner already?
        loader.insertBefore(Spinner(),loader.childNodes[1]);
    }
    loader.style.display = 'none';
    //movspinner = Spinner('#fff',20);
}

var ShowLoader = function() {
    loader.childNodes[2].innerHTML = 'loading ...'; 
    loader.style.display = 'block';
}

var HideLoader = function() {
    loader.style.display = 'none';
}

var UpdateLoader = function(msg) {
    loader.childNodes[2].innerHTML = msg;
}

var Spinner = function() {
    var circle = new Sonic({
    width: 40,
    height: 40,
    padding: 0,
    strokeColor: '#000',
    pointDistance: .05,
    stepsPerFrame: .1,
    trailLength: .9,
    step: 'fader',
    setup: function() {
    	this._.lineWidth = 5;
    },
    path: [
        ['arc', 20, 20, 6, 0, 360]
    ]
    });
    circle.play();
    return circle.canvas;
}

/*
 * Misc
 */

var AndroidAPIVer = function () {
    var api = myapp.AndroidAPIVer();
    //myapp.Log('API ' + api);
    if ( api < 8 ) { // fixed does not work in android 2.1
        document.getElementById('feedback').style.position = 'absolute';
        document.getElementById('coverbody').style.position = 'absolute';
    }
} 

var ResetHTMLVideoList = function () {
    var vl = document.getElementById('videolist');
    vl.innerHTML = '';
}

