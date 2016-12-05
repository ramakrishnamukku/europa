import Reducers from './../reducers/AddRegistryReducers'
import * as RAjax from './../util/RAjax'

export function updateNewRegistryField(prop, e, eIsValue = false) {
  let value = (eIsValue) ? e : e.target.value;

  this.setState(
    Reducers(this.state, {
      type: 'UPDATE_NEW_REGISTRY',
      data: {
        prop,
        value
      }
    })
  );
};

export function addRegistryRequest() {
  let missing = addRegistryValidator.call(this)

  if(missing.length) {
  	console.log(missing);
  	return;
  }

  RAjax.POST('SaveRegistryCreds', this.state.newRegistry)
    .then((res) => {
      console.log(res);
    })
    .catch((err) => {
      console.log(err);
    });
}

function addRegistryValidator() {
  let required = {
    provider: 'Registry Provider',
    region: 'Region',
    key: 'Public Key',
    secret: 'Secret Key'
  }

  return validate.call(this, this.state.newRegistry, required);
}

function validate(postData, required) {
  return Object.keys(required)
    .reduce((cur, key) => {
      if (!postData.hasOwnProperty(key) || (postData.hasOwnProperty(key) && !postData[key])) {
        cur.push(required[key])
      }
      return cur
    }, []);
}

export function listRegistries() {
  RAjax.GET('ListRegistryCreds', {
      name: "Sam"
    })
    .then((res) => {
      console.log(res);
    })
    .catch((err) => {
      console.log(err);
    });
}