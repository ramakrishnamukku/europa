export function POST(operation = '', content = {}, url = '/ajax') {
  let request = new Request(url, {
    method: 'POST',
    mode: 'cors',
    body: JSON.stringify({
      op: operation,
      content: content
    }),
    headers: new Headers({
      'Content-Type': 'application/json',
      'Accept': 'json'
    })
  });

  return fetch(request).then(res => res.json())
}

export function GET(operation = '', params = {}, url = '/ajax') {
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

  return fetch(request).then(res => res.json())
}
