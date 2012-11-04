/* 
 * local vars
 */

/* 
 * toolbar related 
 */

var onUserFeedReq = function() {
    toolbar_select('userpic btnuser'); // select both
    myapp.SetDataStream (0);
}

var onAllFeedReq = function() {
    toolbar_select('btncommunity');
    myapp.SetDataStream (1);
}

var tbarstatus;
var onFeedback = function() {
    var feedback = document.getElementById('feedback');
    var state = feedback.style.display;
    if (state == 'block') {
        toolbar_reset(tbarstatus);
        feedback.style.display = 'none';
    } else {
        tbarstatus = toolbar_select('btnfeedback');
        feedback.style.display = 'block';
        enterdialogmode(onFeedback);
    }
}

var onSettings = function() {
    toolbar_select('btnsettings');
    myapp.OpenSettings();
}

var onFeedback_Share = function() {
    myapp.Feedback("share");
}

var onFeedback_Mail = function() {
    myapp.Feedback("mail");
}

var onFeedback_Review = function() {
    myapp.Feedback("review");
}

var onSearchUpdate = function(text) {
    searchtxt = text; 
    var match = document.getElementsByClassName('mytitle');
    for (var i=0; i<match.length; i++) {
        var line = match[i].innerText;
        var lielem = match[i].parentNode.parentNode;
        var found = (line.search(new RegExp(searchtxt,'i')) >= 0);
        if (found) { // found
            lielem.style.display = 'block';
        } else {
            lielem.style.display = 'none';
        }
    } 
    TriggerIfTooLessData();
}

/*
 * Funtions called from Java files
 */

/*
 * helper functions
 */

var toolbar_select = function(selections) {
    var prevs = [];
    var tools = document.getElementsByClassName('tool');
    enddialogmode(); // if we were in a dialog mode, get out of it
    for (var i=0; i < tools.length; i++) {
        var elem = tools[i];
        prevs[elem.id] = elem.getAttribute('class');
        if (prevs[elem.id].indexOf('disabled') != -1) continue;
        var myclasses = prevs[elem.id].replace('notpressed','').replace('pressed','').trim();
        if (selections.indexOf(elem.id) != -1) {
            myclasses = myclasses.concat(' pressed');
        } else {
            myclasses = myclasses.concat(' notpressed');
        }
        elem.setAttribute('class', myclasses);
    }
    return prevs;
}

var toolbar_reset = function(prevs) {
    var tools = document.getElementsByClassName('tool');
    enddialogmode(); // if we were in a dialog mode, get out of it
    for (var i=0; i < tools.length; i++) {
        var elem = tools[i];
        if ( prevs[elem.id] != null )
            elem.setAttribute ( 'class' , prevs[elem.id] );    
    }
}

/* dialog mode */

var disable_dialog_function;
var dialogmode = false;

var enterdialogmode = function (dfunc) {
    var element = document.getElementById("coverbody");
    element.style.display = 'block';
    document.addEventListener( 'touch' , stopScrolling , false );
    document.addEventListener( 'touchmove' , stopScrolling , false );
    dialogmode = true;
    disable_dialog_function = dfunc;
}

var enddialogmode = function() {
    if (dialogmode) {
        var element = document.getElementById("coverbody");
        element.style.display = 'none';
        document.removeEventListener( 'touch' , stopScrolling , false );
        document.removeEventListener( 'touchmove' , stopScrolling , false );
        dialogmode = false;
        disable_dialog_function();
    }
}

function stopScrolling( touchEvent ) { 
    touchEvent.preventDefault(); 
}

/* misc */

String.prototype.trim = function() {
    return this.replace(/^\s+|\s+$/g, "");
};
