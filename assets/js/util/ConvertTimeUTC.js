/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

/**
 * Convert Date Object to UTC time string.
 * @param {Date} dateObj - The Date object to be converted
 * @param {boolean} withoutTime - Whether to exclude the hour and minutes.
 */

function convertTime(dateObj, withoutTime) {

    if(!dateObj || !(dateObj instanceof Date)) return null;

    var d = dateObj;

    var month = d.getMonth();
    var day =  d.getDate();
    var year = d.getFullYear();
    var hours = d.getHours();
    var minutes = d.getMinutes();
    minutes = (minutes.toString().length == 1) ? '0' + minutes : minutes;
    var amPm = 'AM';

    if(hours > 12) {
      hours = hours - 12;
      amPm = 'PM';
    }

    var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

    var finalDate = hours + ':' + minutes + amPm + ' ' + months[month] + ' ' + day + ', ' + year;

    if(withoutTime) {
        finalDate = months[month] + ' ' + day + ', ' + year;        
    }

    if(finalDate.toLowerCase().indexOf('nan') > -1) return 'Unknown';

    return finalDate;
}

export default convertTime;