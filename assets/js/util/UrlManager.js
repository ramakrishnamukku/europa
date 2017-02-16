/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

export function parseUrl(url) {
	let parse_url = /^(?:([A-Za-z]+):)?(\/{0,3})([0-9.\-A-Za-z]+)(?::(\d+))?(?:\/([^?#]*))?(?:\?([^#]*))?(?:#(.*))?$/;
	let result = parse_url.exec(url);
	return {
		url: result[0],
		scheme: result[1],
		slash: result[2],
		host: result[3],
		port: result[4],
		path: result[5],
		query: result[6],
		hash: result[7]
	}
};

export function parseQueryString(url) {
	let currentQuery = parseUrl(url).query;

	if(!currentQuery) {
		return {};
	}

	return currentQuery.split('&')
		.reduce((cur, next) => {
			let keyAndValue = next.split('=');
			cur[keyAndValue[0]] = keyAndValue[1];
			return cur;
		}, {});
};

export function updateUrlParams(paramObj, urlToParse) {
	let workingUrl = (urlToParse) ? urlToParse : window.location;
	let currentParams = parseQueryString(workingUrl);

	let newParams = {
		...currentParams,
		...paramObj
	};

	let queryString = Object.keys(newParams)
		.map((key) => {
			return (newParams[key]) ? key + '=' + newParams[key] : null;
		})
		.filter(item => !!item)
		.join('&');

	if (history.pushState) {
		let newUrl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?' + queryString;
		window.history.pushState({
			path: newUrl
		}, '', newUrl);

		return newUrl;
	}
}