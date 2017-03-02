/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/
import NPECheck from './NPECheck'

const baseURL = '/ajax'

export function POST(operation = '', content = {}, params = {}, url = baseURL) {
  let username = NPECheck(this, 'state/ctx/username', false);

  if (url == baseURL && username && this.state.isEnterprise) {
    url = `/${username}${url}`
  }

  let request = new Request(url, {
    method: 'POST',
    mode: 'cors',
    body: JSON.stringify({
      op: operation,
      content: content,
      params: params
    }),
    headers: new Headers({
      'Content-Type': 'application/json',
      'Accept': 'json'
    })
  });

  return fetch(request)
    .then((response) => {
      let json;

      try {
        json = response.json();
      } catch(e) {
        return Promise.reject.bind(Promise, e);
      }

      if (response.status >= 200 && response.status < 300) {
        return json;
      } else {
        return json.then(Promise.reject.bind(Promise));
      }
    });
}

export function GET(operation = '', params = {}, url = baseURL) {
  let username = NPECheck(this, 'state/ctx/username', false);

  if (url == baseURL && username && this.state.isEnterprise) {
    url = `/${username}${url}`
  }

  params = {
    op: operation,
    ...params,
  };

  let finalUrl = Object.keys(params)
    .reduce((cur, key) => {
      return `${cur}${key}=${params[key]}&`
    }, url + '?');

  let request = new Request(finalUrl, {
    method: 'GET',
    mode: 'cors',
    headers: new Headers({
      'Content-Type': 'application/json',
      'Accept': 'json'
    })
  });

  return fetch(request)
    .then((response) => {
      let json;

      try {
        json = response.json();
      } catch(e) {
        return Promise.reject.bind(Promise, e);
      }
      
      if (response.status >= 200 && response.status < 300) {
        return json;
      } else {
        return json.then(Promise.reject.bind(Promise));
      }
    });
}