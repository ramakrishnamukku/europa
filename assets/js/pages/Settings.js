/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import Registries from './../components/Registries'
import APITokens from './../components/APITokens'
import StorageSettings from './../components/StorageSettings'
import SSLSettings from './../components/SSLSettings'
import { parseQueryString } from './../util/UrlManager'

let credKey = 'creds';
let apiTokensKey = "tokens";
let storageKey = "storage";
let sslKey = "ssl";

export default class Settings extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentWillMount() {
		let section = parseQueryString(window.location).section;
		if(section) {
			this.context.actions.setSettingsSection(section);	
		}
	}
	getSideBarItemClassName(key){
		let section = this.props.settings.section;
		return (key == section) ? 'SideBarItem Active' : 'SideBarItem';
	}
	componentWillUnmount() {
		this.context.actions.resetSettingsState();
	}
	renderSideBar(){
		return (
			<div className="SideBarContainer">
				<div className={this.getSideBarItemClassName(credKey)} 
					 onClick={() => this.context.actions.setSettingsSection(credKey)}>
					Registry Credentials
				</div>
				<div className={this.getSideBarItemClassName(apiTokensKey)} 
					 onClick={() => this.context.actions.setSettingsSection(apiTokensKey)}>
					API Tokens
				</div>
				<div className={this.getSideBarItemClassName(storageKey)} 
					 onClick={() => this.context.actions.setSettingsSection(storageKey)}>
					Storage
				</div>
				<div className={this.getSideBarItemClassName(sslKey)} 
					 onClick={() => this.context.actions.setSettingsSection(sslKey)}>
					SSL
				</div>
			</div>
		);
	}
	renderRegistries(){
		return (
			<Registries {...this.props}/>
		);
	}
	renderAPITokens(){
		return (
			<APITokens {...this.props}/>
		);
	}
	renderStorage(){
		return (
			<StorageSettings {...this.props} />
		);
	}
	renderSSL(){
		return (
			<SSLSettings {...this.props} />
		);
	}
	renderContent(){
		let section = this.props.settings.section;
		switch(section) {
			case credKey:
				return this.renderRegistries();
			break;
			case apiTokensKey:
				return this.renderAPITokens();
			break;

			case storageKey:
				return this.renderStorage();
			break;

			case sslKey:
				return this.renderSSL();
			break;

			default:
				return this.renderRegistries();
		}
	}
	render() {
		return (
			<div className="ContentContainer">
				<div className="PageHeader">
					<h2>
						Settings
					</h2>
				</div>
				<div className="FlexRow RowPadding">
					<div className="Flex1 Column">
	 						{this.renderSideBar()}
	 				</div>
					<div className="Flex3">
						{this.renderContent()}
					</div>
				</div>
			</div>
		);
	}
}

Settings.childContextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

Settings.contextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};
