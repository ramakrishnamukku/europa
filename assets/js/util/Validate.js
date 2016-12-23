/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

export default function validate(postData, required, curKey = "") {
  let names = [];
  let keys = [];

  Object.keys(required).forEach((key) => {

    if(typeof postData[key] == 'object') {
      let nestedCheck = validate(postData[key], required[key], `${key}/`)
      names = names.concat(nestedCheck.names)
      keys = keys.concat(nestedCheck.keys)
    }

    if (!postData.hasOwnProperty(key) || (postData.hasOwnProperty(key) && !postData[key])) {
      names.push(required[key]);
      keys.push(`${curKey}${key}`);
    }
  });

  return {
    names,
    keys
  };
}