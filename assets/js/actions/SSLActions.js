import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import NPECheck from './../util/NPECheck'
import Debounce from './../util/Debounce'
import Validate from './../util/Validate'

// *************************************************
// General SSL Actions
// *************************************************

export function sslState() {
	return {
		sslEnabled: false,
		ogSslEnabled: false,
		hasChanges: false,
		sslCreds: {
			dnsName: '',
			serverPrivateKey: '',
			serverCertificate: '',
			authorityCertificate: '',
		},
		errorFields: {
			names: [],
			keys: []
		},
		getXHR: false,
		getError: '',
		saveSuccess: false,
		saveXHR: false,
		saveError: '',
	};
}

export function resetUsersState() {
	this.setState({
		ssl: usersState()
	});
}

export function clearSSLErrors() {
	this.setState({
		ssl: GA.modifyProperty(this.state.ssl, {
			saveError: '',
			errorFields: {}
		})
	});
}


export function toggleEnableSSL() {
	this.setState({
		ssl: GA.modifyProperty(this.state.ssl, {
			saveSuccess: false,
			sslEnabled: !this.state.ssl.sslEnabled
		})
	});
}

export function updateSSLCreds(prop, e, eIsValue = false) {
	let value = (eIsValue) ? e : e.target.value;

	this.setState({
		ssl: GA.modifyProperty(this.state.ssl, {
			saveSuccess: false,
			hasChanges: true,
			sslCreds: {
				...this.state.ssl.sslCreds,
				[prop]: value
			}
		})
	});
}


export function updateDNSName(dnsName){
	this.setState({
		dnsName: dnsName
	});
}

export function saveSSLSettings() {
	return new Promise((resolve, reject) => {

		let creds = this.state.ssl.sslCreds
		let isEnabled = NPECheck(this.state, 'ssl/sslEnabled', false)

		if (isEnabled) {
			let isValid = isSSLValid(creds);

			if (isValid.names.length) {
				this.setState({
					ssl: GA.modifyProperty(this.state.ssl, {
						errorFields: isValid
					})
				}, () => {
					reject();
				});
				return
			}


		} else {
			creds = {
				dnsName: NPECheck(this.state, 'ssl/sslCreds/dnsName')
			}
		}

		this.setState({
			ssl: GA.modifyProperty(this.state.ssl, {
				saveXHR: true
			})
		}, () => {

			RAjax.POST.call(this, 'SaveSslSettings', creds)
				.then((res) => {
					this.setState({
						ssl: GA.modifyProperty(this.state.ssl, {
							saveXHR: false,
							saveSuccess: true,
						})
					}, () => resolve(res));

				})
				.catch((err) => {
					console.error(err);
					let errorMsg = NPECheck(err, 'error/message', 'There was an error saving your SSL settings.');

					this.setState({
						ssl: GA.modifyProperty(this.state.ssl, {
							saveXHR: false,
							saveError: errorMsg,
							saveSuccess: false,
						})
					}, () => reject(err));

				});
		});
	});
}

export function getSSLSettings() {
	return new Promise((resolve, reject) => {
		this.setState({
			ssl: GA.modifyProperty(this.state.ssl, {
				getXHR: true
			})
		}, () => {

			RAjax.GET.call(this, 'GetSslSettings')
				.then((res) => {
					let sslEnabled = isSSLEnabled(res);

					this.setState({
						ssl: GA.modifyProperty(this.state.ssl, {
							getXHR: false,
							sslCreds: res,
							ogSslEnabled: sslEnabled,
							hasChanges: false,
							sslEnabled,

						})
					}, () => resolve(res));

				})
				.catch((err) => {
					console.error(err);
					let errorMsg = NPECheck(err, 'error/message', 'There was an error retreiving your SSL settings.');

					this.setState({
						ssl: GA.modifyProperty(this.state.ssl, {
							getXHR: false,
							getError: errorMsg
						})
					}, () => reject(err));

				});
		});
	});
}

export function isSSLValid(sslSettings) {
	let required = {
		dnsName: 'DNS Name',
		serverPrivateKey: 'Server Private Key',
		serverCertificate: 'Server Certificate',
		authorityCertificate: 'Authority Certificate',
	};

	let isValid = Validate(sslSettings, required);

	return isValid;
}

function isSSLEnabled(sslSettings) {
	let s = sslSettings;
	return !!(s.serverPrivateKey && s.serverCertificate && s.authorityCertificate)
}