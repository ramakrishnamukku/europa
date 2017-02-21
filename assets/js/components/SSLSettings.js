/*
  @author Sam Heutmaker [sam@distelli.com]
*/

import React, { Component, PropTypes } from 'react'
import {Link} from 'react-router'

import Btn from './Btn'
import Loader from './Loader'
import Checkbox from './Checkbox'
import Msg from './Msg'
import NPECheck from './../util/NPECheck'

const dnsNameKey = 'dnsName';
const serverPrivateKey = 'serverPrivateKey';
const serverCertKey = 'serverCertificate';
const caKey = 'authorityCertificate';

export default class SSLSettings extends Component {
	constructor(props) {
		super(props);

		this.state = {};
	}
	componentDidMount() {
		this.context.actions.getSSLSettings();
		if(this.refs['dnsName']) {
			this.refs['dnsName'].focus()
		}
	}
	saveSSLSettings(){
		this.context.actions.saveSSLSettings()
		.then(this.context.actions.getSSLSettings)
		.then((sslSettings) => this.context.actions.updateDNSName(sslSettings.dnsName))
		.catch((err) => {
			console.error(err);
		});
	}
	getTextareaClassName(key){
		let hasSelector = NPECheck(this.props, 'ssl/errorFields/keys', []).includes(key);
		let className;

		if(hasSelector) {
			className =  "BlueBorder FullWidth Error";
		} else {
		    className =  "BlueBorder FullWidth";
		}

		if(this.props.isLoggedIn) {

			className += " White";
		}

		return className;
	}
	renderSSLTextarea(config, i){
		let value = NPECheck(this.props, `ssl/sslCreds/${config.key}`, '');
		return (
			<div className="FlexColumn" key={i}>
				<label>{config.label}</label>
				<textarea className={this.getTextareaClassName(config.key)} 
						  value={value} 
						  onChange={(e) => this.context.actions.updateSSLCreds(config.key, e)}>
				</textarea>
			</div>
		);
	}
	renderSSLInputs(){
		let dnsValue = NPECheck(this.props, `ssl/sslCreds/${dnsNameKey}`, '');
		let className = "BlueBorder FullWidth";

		if(this.props.isLoggedIn) {
			className += " White";
		}

		return (
			<div className="FlexColumn">
				<div className="FlexColumn">
					<label>DNS Name</label>
					<input ref="dnsName" className={className} value={dnsValue} onChange={(e) => this.context.actions.updateSSLCreds(dnsNameKey, e)} />
				</div>
				<div className="FlexRow">
					<Checkbox onClick={() => this.context.actions.toggleEnableSSL()} label="SSL Enabled" isChecked={NPECheck(this.props, 'ssl/sslEnabled', false)}/>
					{this.renderUnsavedChanges()}
				</div>
				{this.renderTextAreas()}
				{this.renderButton()}
				{this.renderSuccesssMsg()}
			</div>
		);
	}
	renderTextAreas(){
		if(NPECheck(this.props, 'ssl/sslEnabled', false)) {

			let textareaConfigs = [
				{
					label: 'Server Private Key',
					key: serverPrivateKey
				},
				{
					label: 'Server Certificate',
					key: serverCertKey
				},
				{
					label: 'CA Certificate',
					key: caKey
				}
			];
			return textareaConfigs.map((config, i) => this.renderSSLTextarea(config, i))	
		}
	}
	renderButton(){
		if(NPECheck(this.props, 'ssl/saveXHR', false)) {
			return (
				<Loader />
			);
		}

		let errorMsg = NPECheck(this.props, 'ssl/saveError', false);

		let errorMsg2 = NPECheck(this.props, 'ssl/errorFields/names', []).join(', ')

		if(errorMsg || errorMsg2) {
			errorMsg2 = (errorMsg2) ? `Missing Required Fields: ${errorMsg2}` : null;

			return (
				<Msg text={(errorMsg || errorMsg2)} 
    				 close={() => this.context.actions.clearSSLErrors()}
    				 style={{padding: '1rem 0'}}/> 
			);
		}

		return (
			<div className="FlexRow AlignCenter JustifyCenter">
				<Btn onClick={() => this.saveSSLSettings()}
					 text="Save SSL Settings"
					 canClick={true} />
			</div>
		);
	}
	renderUnsavedChanges(){
		let hasChanges = NPECheck(this.props, 'ssl/hasChanges', false);
		let changedEnabled = NPECheck(this.props, 'ssl/sslEnabled', null) != NPECheck(this.props, 'ssl/ogSslEnabled', null)

		if(hasChanges || changedEnabled) {
			return (
				<span className="UnsavedChanges">You have unsaved changes.</span>
			);
		}
	}
	renderSuccesssMsg(){
		let success = NPECheck(this.props, 'ssl/saveSuccess', false);

		if(success) {
			return (
				<Msg text="Successfully saved SSL settings." 
					 isSuccess={true}
    				 style={{padding: '1rem 0'}}/> 
			);
		}
	}
	renderEnableStatus() {
		let sslEnabled = NPECheck(this.props, 'ssl/sslEnabled', false);

		if (sslEnabled) {
			return (
		    	<div className="SSLStatus"
		             style={ {color: "#75C05B"} }>
		          <span>SSL is enabled</span>
		        </div>
		      );

		    } else {
		      return (
		        <div className="SSLStatus"
		             style={ {color: "#F7739C"} }>
		          <span>SSL is disabled</span>
		        </div>
		      );
		    }
		}
	render(){
		return (
			<div className="ContentContainer" style={(this.props.ctx) ? {marginTop: '2rem'} : {}}>
		        <div className="SSLHeader">
		             SSL Configuration
		        </div>
		        {this.renderEnableStatus()}
		        <div className="SSLSettings">
		        	{this.renderSSLInputs()}
		    	</div>
		    </div>
		);
	}
}

SSLSettings.contextTypes = {
	router: PropTypes.object,
	actions: PropTypes.object
};

SSLSettings.childContextTypes = {
	actions: PropTypes.object,
	router: PropTypes.object
};
