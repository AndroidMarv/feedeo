/*
 * Timeline
 */

var weekday = ['Sunday', 'Monday', 'Tuesday', 'Wednesday',
               'Thusday', 'Friday', 'Saturday'];
var month = ['January', 'February', 'March', 'April', 'May',
             'June', 'July', 'August', 'September', 'October',
             'November', 'December']; 
var onehour = 1000*60*60;
var oneday = onehour*24;


var AppendToTimeline = function(liitem, created) {
    var d1 = new Date();
    var d2 = new Date(created);
    var diff = d1.getTime()-d2.getTime();
    if (diff < 0) { // append to vl_old
        InsertInTimeline (liitem, 'vl_today', 'Today');
        myapp.Log('Createtime: ' + created + ' ---> ' + d2);
        myapp.Log('Now: ' + d1);
        myapp.Log('Error: diff < 0 . (' + diff + ')');
        return;
    }
    diff = diff / oneday;
    if (d2.getYear() == d1.getYear()) {
        if (d2.getMonth() == d1.getMonth()) {
            if (diff<1 && d2.getDay() == d1.getDay()) {
                InsertInTimeline (liitem, 'vl_today', 'Today');
                return;
            } else if (diff<2 && d2.getDay()+1 == d1.getDay()) {
                InsertInTimeline (liitem, 'vl_yesterday', 'Yesterday');
                return;
            }
            if (diff < 7) {
                if (d2.getDay() < d1.getDay()) {
                    InsertInTimeline (liitem, 'vl_d' + d2.getDay(), weekday[d2.getDay()]);
                    return;
                } else {
                    InsertInTimeline (liitem, 'vl_lastweek', 'Last Week');
                    return;
                }
            }
            InsertInTimeline (liitem, 'vl_thismonth', 'This Month');
            return;
        } 
        InsertInTimeline (liitem, 'vl_m' + d2.getMonth(), month[d2.getMonth()]);
        return;
    }
    InsertInTimeline (liitem, 'vl_y' + d2.getYear() , d2.getYear());
    return;
}

var InsertInTimeline = function(liitem, id, text) { 
    var tdiv = document.getElementById(id);
    if (tdiv == null) {
        var vl = document.getElementById('videolist');
        tdiv = document.createElement('div');
        tdiv.setAttribute('id', id);
        tdiv.setAttribute('class', 'zerodiv');
        tdiv.innerHTML = "<li class='createdtime'><span>"+ text + "</span></li>";
        vl.appendChild( tdiv );
    }
    tdiv.appendChild( liitem );
}

var CustomTime = function(epoch) {
    var d1 = new Date();
    var d2 = new Date(0);
    d2.setUTCSeconds(epoch);
    var diff = d1.getTime()-epoch*1000;
    if (diff < 0) { // append to vl_old
        return 'today';
    }
    diff = diff / oneday;
    if (d2.getYear() == d1.getYear()) {
        if (d2.getMonth() == d1.getMonth()) {
            if (diff<1 && d2.getDay() == d1.getDay()) {
                return 'today'; 
            } else if (diff<2 && d2.getDay()+1 == d1.getDay()) {
                return 'yesterday';
            }
            if (diff < 7) {
                if (d2.getDay() < d1.getDay()) {
                    return weekday[d2.getDay()];
                } else {
                    return 'last week';
                }
            }
            return 'this month';
        } 
        return month[d2.getMonth()];
    }
    return month[d2.getMonth()].substr(0,3) + ', ' + d2.getFullYear();
}
