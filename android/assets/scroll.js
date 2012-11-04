/* Scroll triggered loading */

var flag = true;
var MAXFETCH = 0; // max videos fetched at one go
var TRIGGER = 0;
var winH = 460;

function InitScrollLoading() {
    //myapp.Log("InitScrollLoading()");
    window.onscroll = onScroll;
    var supportsOrientationChange = "onorientationchange" in window;
    var orientationEvent = supportsOrientationChange ? "orientationchange" : "resize";
    window.addEventListener(orientationEvent, InitScrollValues, false);
    InitHeightVar();
}

function InitScrollValues() {
    InitHeightVar();
    CalculateAndCheckTrigger();
}

function onScroll() {
     //myapp.Log("onScroll()");
     if ( flag && CheckFetchMore() ) {
        //myapp.Log("FetchMore... triggered");
        flag = false; // trigger only once
        myapp.FetchMore();
    }
}

var CalculateAndCheckTrigger = function() {
    TRIGGER = Math.max ( winH , document.height - winH ); 
    MAXFETCH = Math.max ( 2*winH , document.height + winH );
    //DebugFunction();
    flag = true;
    if ( CheckFetchMore() ) {
        //myapp.Log("FetchMore... triggered");
        flag = false; // trigger only once
        myapp.FetchMore();
        return true;
    }
    return false;
}

var TriggerIfTooLessData = function() {
    if ( TRIGGER == winH ) {
        flag = false;
        myapp.FetchMore(); // data is less than screen, get more
        return true;
    }
    return false;
}

var CheckFetchMore = function() {
    //var visibleTop = document.body.scrollTop;
    var visibleBottom = window.scrollY + winH;
    //myapp.Log("visibleBottom=" + visibleBottom);
    if ( TRIGGER < visibleBottom ) {
        //myapp.Log("hit trigger");
        return true;
    } else { // hit the end of document
        var mawinH = Math.max ( winH , document.body.scrollHeight - 2); // leaving a small margin 
        if ( visibleBottom > mawinH ) { 
            //myapp.Log("hit EOD");
            return true;
        }
    }
    return false;
}

var CheckFetchNoMore = function() {
    if ( document.height <= winH ) {
        return false;
    }
    if ( document.height >= MAXFETCH ) { // another screenful
        return true; // no more - fetched enough 
    }
    var more = CheckFetchMore();
    return !more;
}

function InitHeightVar() {
  winH = null;
  if(window.screen != null)
    winH = window.screen.availHeight;
  //myapp.Log("winH=" + winH);
 
  if(window.innerHeight != null)
    winH =   window.innerHeight;
  //myapp.Log("winH=" + winH);
 
  if(document.body != null)
    winH = document.body.clientHeight;
  //myapp.Log("winH=" + winH);
  return winH;
}

var DebugFunction = function() {
    myapp.Log("document.height=" + document.height);
    myapp.Log("TRIGGER=" + TRIGGER);
    myapp.Log("MAXFETCH=" + MAXFETCH);
}

