/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

// Convert milliseconds since epoch to friendly time i.e. "123456789" => "x months ago"

export default function(millis){
  var seconds = Math.floor((new Date() - millis) / 1000);
  var interval = Math.floor(seconds / 31104000);


  if(interval >= 1) {
    return interval + ` year${(interval > 1) ? 's' : '' } ago`;
  }

  interval  = Math.floor(seconds / 2592000);
  if(interval >= 1) {
    return interval + ` month${(interval > 1) ? 's' : '' } ago`;
  }

  interval  = Math.floor(seconds / 86400);
  if (interval >= 1) {
    return interval + ` day${(interval > 1) ? 's' : '' } ago`;
  }

  interval = Math.floor(seconds / 3600);
  if (interval >= 1) {
    return interval + ` hour${(interval > 1) ? 's' : '' } ago`;
  }

  interval = Math.floor(seconds / 60);
  if (interval >= 1) {
    return interval + ` minute${(interval > 1) ? 's' : '' } ago`;
  }

  if(seconds > 0) {
    return interval + ` second${(interval > 1) ? 's' : '' } ago`;
  }

  return 'Moments ago';
}