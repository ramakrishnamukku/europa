/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import Registries from './../components/Registries'
import APITokens from './../components/APITokens'
import StorageSettings from './../components/StorageSettings'

let credKey = 'CREDENTIALS';
let apiTokensKey = "API_TOKENS";
let storageKey = "STORAGE";

export default class Settings extends Component {
	constructor(props) {
		super(props);
		this.state = {};
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
	renderContent(){
		let section = this.props.settings.section;
		switch(section) {
			case 'CREDENTIALS':
				return this.renderRegistries();
			break;
			case 'API_TOKENS':
				return this.renderAPITokens();
			break;

			case 'STORAGE':
				return this.renderStorage();
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
