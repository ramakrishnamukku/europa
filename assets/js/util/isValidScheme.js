/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

export default function(url){
	return (url.substr(0, 7) == 'http://' || url.substr(0, 8) == 'https://');
}