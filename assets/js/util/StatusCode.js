export default function statusCode(statusCode){
  let returnValue = 'UNKNOWN';

  if (200 <= statusCode && statusCode <= 299) returnValue = 'SUCCESS';
  if ((0 <= statusCode && statusCode <= 199) || (300 <= statusCode && statusCode <= 399)) returnValue = 'WARNING';
  if (400 <= statusCode) returnValue = 'ERROR';

  return returnValue;
}