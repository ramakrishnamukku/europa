///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// CreateOrSetPropertyValue
//
// Usage: SetPropertyValue(param1, param2, param3)
//        param1 = the JS object
//        param2 = the nested property path in JSON pointer syntax, https://tools.ietf.org/html/rfc6901
//        param3 = the new property value
//
//
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

export default function(object, nestedProps, value) {
  let nested = nestedProps.split("/");
  let val = object;
  let finalProp = null;

  while ( nested.length > 0 ) {
    let prop = nested.shift();
    if (!val.hasOwnProperty(prop)) val[prop] = {};
    if (nested.length > 0) {
      val = val[prop];
    }
   finalProp = prop;
  }

  val[finalProp] = value;
}