/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component} from 'react'
import Registries from './../components/Registries'
import Btn from './../components/Btn'

export default class Settings extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderRegistries(){
		return (
			<Registries />
		);
	}
	getSideBarItemClassName(key){
		let section = this.context.state.settings.section;
		return (key == section) ? 'SideBarItem Active' : 'SideBarItem';
	}	
	renderSideBar(){
		let credKey = 'CREDENTIALS';
		let dbKey = "DB";
		let settingsKey = "SETTINGS"

		return (
			<div className="SideBarContainer">
				<div className={this.getSideBarItemClassName(credKey)} 
					 onClick={() => this.context.actions.setSettingsSection(credKey)}>
					Credentials
				</div>
				<div className={this.getSideBarItemClassName(dbKey)} 
					 onClick={() => this.context.actions.setSettingsSection(dbKey)}>
					DB
				</div>
				<div className={this.getSideBarItemClassName(settingsKey)} 
					 onClick={() => this.context.actions.setSettingsSection(settingsKey)}>
					Settings
				</div>
			</div>
		);
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
					<div className="Flex3">
						{this.renderRegistries()}
					</div>
				</div>
			</div>
		);
	}
}

Settings.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

Settings.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

// <div className="Flex1 Column">
// 						{this.renderSideBar()}
// 					</div>