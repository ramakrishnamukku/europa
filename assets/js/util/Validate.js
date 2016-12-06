export default function validate(postData, required) {
  let names = [];
  let keys = [];

  Object.keys(required).forEach((key) => {
    if (!postData.hasOwnProperty(key) || (postData.hasOwnProperty(key) && !postData[key])) {
      names.push(required[key]);
      keys.push(key);
    }
  });

  return {
    names,
    keys
  };
}