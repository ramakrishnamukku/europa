/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

export default function statusCode(statusCode) {
	let returnValue = 'UNKNOWN';

	if (200 <= statusCode && statusCode <= 399) {
		returnValue = 'SUCCESS';
	} else {
		returnValue = 'ERROR';
	}

	return returnValue;
}
// Can also return 'WARNING' for warning status codes