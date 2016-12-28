// Convert milliseconds since epoch to friendly time i.e. "123456789" => "x days ago"

export default function(millis){
  var seconds = Math.floor((new Date() - millis) / 1000);
  var interval = Math.floor(seconds / 86400);

  if (interval >= 1) {
    return interval + " day(s) ago";
  }

  interval = Math.floor(seconds / 3600);
  if (interval >= 1) {
    return interval + " hour(s) ago";
  }

  interval = Math.floor(seconds / 60);
  if (interval >= 1) {
    return interval + " minute(s) ago";
  }

  if(seconds > 0) {
    return seconds + " second(s) ago";  
  }

  return 'Moments ago';
}
