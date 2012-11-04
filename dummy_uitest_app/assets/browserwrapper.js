
function myapp() {
    myapp.GetAllFriendsFeed();
};

/*
 * Wrappers
 */

var con;
myapp.Log = function(msg) {
    if ( con != undefined )
         con.Log(msg);
}

myapp.LoadUserInfo = function() {
    username = 'Naval Saini';
    userpic = './mylocal_files/currentuser.jpg';
}

myapp.LoadDataStream = function(stream) {
    jsSetDataStream(stream);
    jsStartLoading();
    myapp.FetchMore();
    jsQueryEpochUpdate ( vidstream, 1339543604 ); 
    return true;
}

myapp.LoadFriendsList = function() {
    jsEnableCommunityFeed();
}

myapp.SetDataStream = function(stream) {
    jsSetDataStream (vidstream);
}

myapp.FetchNoMore = function() {
}

myapp.FetchMore = function() {
    myapp.Log('Using cached videoidlist ... looping');
    for (var i=0; i<testvideoidlist.length; i++) {
        jsUpdateVideo ( testvideoidlist[i] );
    }
    intestupdate = true;
    for (var i=0; i<testupdateidlist; i++) {
        jsUpdateVideo ( testupdateidlist[i] );
    } 
}

myapp.OpenSettings = function() {
    alert ('Open settings');
}

myapp.GetVideoMeta = function(objid) {
    if ( !intestupdate ) {
        for (var i=0; i<testvideometadata.length; i++) {
            var jobj = testvideometadata[i];
            if ( jobj.objectid == objid ) {
                return JSON.stringify(jobj);
            }
        }
    } else {
        for (var i=0; i<testupdatemetadata.length; i++) {
            var jobj = testupdatemetadata[i];
            if ( jobj.objectid == objid ) {
                return JSON.stringify(jobj);
            }
        }
    }
}

myapp.PlayVideo = function(objid) {
    alert ('Play video with id ' + objid);
}

myapp.AndroidAPIVer = function() {
    if ( con != undefined )
        return 7;
    return 8; // this is a browser
}

myapp.Feedback = function(cmd) {
    alert ('cmd');
}

myapp.GetUserPic = function(usrid) {
    return 'mylocal_files/currentuser.jpg'; 
}

myapp.SharebySMS = function(objid) {
    alert ('SMS about ' + objid);
}

myapp.ShareonFB = function(objid) {
    alert ('Share on fb ' + objid);
}

myapp.LikeonFB = function(objid) {
    alert ('Like on fb ' + objid);
}

/*
 * Functions/Data for testing offline
 */

var intestupdate = false;

// Data

var testvideoidlist = ["1", "2", "3", "7", "8", "4", "5", "6"];
var testupdateidlist = ["1", "4", "6"];

/*var testvideometadata[];
var testupdatemetadata[]; */

var testvideometadata = [ 
 {"thumbnail": "./mylocal_files/safe_image.jpg", 
  "duration": "0",
  "likes": "2",
  "createdtime": "June 5, 2012",
  "canplay": true,
  "objectid": "1",
  "title": "Dil Chahta Hai - Koi Kahe Kehta Rahe Great Quality - Lyrics" }, 

 {"thumbnail": "./mylocal_files/safe_image(1).jpg", 
  "duration": "230",
  "likes": "5",
  "createdtime": "June 4, 2012",
  "canplay": true,
  "objectid": "2",
  "title": "New Video Clip - Kamani Auditorium Delhi - Live Music" }, 

 {"thumbnail": "./mylocal_files/safe_image(2).jpg", 
  "duration": "45",
  "likes": "0",
  "createdtime": "June 1, 2012",
  "canplay": false,
  "objectid": "3",
  "title": "A DRAMATIC SURPRISE ON A QUIET SQUARE" }, 

 {"thumbnail": "./mylocal_files/safe_image(3).jpg", 
  "duration": "400",
  "likes": "1",
  "createdtime": "April 01, 2012",
  "canplay": true,
  "objectid": "4",
  "title": "http://www.pocketjourney.com/downloads/pj/video/famous.3gp" }, 

 {"thumbnail": "./mylocal_files/safe_image(4).jpg", 
  "duration": "10",
  "likes": "12",
  "createdtime": "April 24, 2012",
  "canplay": true,
  "objectid": "5",
  "title": "Will Whole Foods Destroy Brooklyn?" }, 

 {"thumbnail": "./mylocal_files/safe_image(5).jpg", 
  "duration": "91",
  "likes": "3",
  "createdtime": "April 15, 2012",
  "canplay": true,
  "objectid": "6",
  "title": "Shit Delhi Boys Say" },

 {"thumbnail": "",
  "duration": "12",
  "likes": "7",
  "createdtime": "May 15, 2012",
  "canplay": true,
  "objectid": "7",
  "title": "Blank" },

 {"thumbnail": "",
  "duration": "12",
  "likes": "7",
  "createdtime": "May 15, 2012",
  "canplay": true,
  "objectid": "8",
  "title": "Blank1" },

]; // next objectid ... 9

var testupdatemetadata = [ 
 {"thumbnail": "./mylocal_files/safe_image.jpg", 
  "duration": "31",
  "likes": "2",
  "createdtime": "March 13, 2012",
  "canplay": true,
  "objectid": "1",
  "title": "Dil Chahta Hai - Koi Kahe Kehta Rahe Great Quality - Lyrics" }, 

 {"thumbnail": "./mylocal_files/safe_image(3).jpg", 
  "duration": "37",
  "likes": "1",
  "createdtime": "January 01, 2012",
  "canplay": true,
  "objectid": "4",
  "title": "http://www.pocketjourney.com/downloads/pj/video/famous.3gp" }, 

 {"thumbnail": "./mylocal_files/safe_image(5).jpg", 
  "duration": "41",
  "likes": "3",
  "createdtime": "April 15, 2012",
  "canplay": true,
  "objectid": "6",
  "title": "Shit Delhi Boys Say" }
];

