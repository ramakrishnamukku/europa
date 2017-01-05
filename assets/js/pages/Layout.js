/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component} from 'react'
import {Link} from 'react-router'
import ReactTooltip from 'react-tooltip'
import ActionBinder from './../util/ActionBinder'

// Actions
import * as GeneralActions from './../actions/GeneralActions'
import * as AddRegistryActions from './../actions/AddRegistryActions'
import * as RepoActions from './../actions/RepoActions'
import * as NotificationActions from './../actions/NotificationActions'
import * as RegistryActions from './../actions/RegistryActions'
import * as SettingsActions from './../actions/SettingsActions'

export default class Layout extends Component {
	constructor(props) {
		super(props);
		
		// Main State Store
		this.state = {
			registries: [],
			registriesMap: {},
			repos: [],
			reposFilterQuery: null,
			reposMap: {},
			registriesXHR: false,
			repositories: [],
			registry: {
				...RegistryActions.registryState()
			},
			addRegistry: {
				...AddRegistryActions.addRegistryState()
			},
			addRepo: {
				...RepoActions.addRepoState()
			},
			notif: {
				...NotificationActions.notifState()
			},
			repoDetails: {
				...RepoActions.repoDetailsState()
			},
			settings: {
				...SettingsActions.settingsState()
			}
		};
	}
	componentDidMount() {
		RegistryActions.listRegistries.call(this)
		RepoActions.listRepos.call(this)
	}
	componentDidUpdate(prevProps, prevState) {
		ReactTooltip.hide();
		ReactTooltip.rebuild();
	}
	getChildContext() {

		let actions = [ AddRegistryActions, 
					    RepoActions,
					    NotificationActions, 
					    RegistryActions, 
					    SettingsActions 
					  ];
		return {
			actions: ActionBinder(actions, this),
			state: this.state,
			router: this.context.router
		};
	}
	render() {
		return (
			<div className="PageContainer">
				<nav className="TopNav">
				 <div className="MaxWidthContainer">
					<div className="Logo">
						<Link to="/">
							<img src="/public/images/distelli-europa-logo.svg"
									 alt="Distelli Europa" />
						</Link>
					</div>
					<div className="FlexRow NavButtonContainer">
						<div className="Flex1">
							<Link to="/settings" data-tip="Settings" data-for="ToolTipBottom">
								<i className="icon icon-dis-settings"/>
							</Link>
						</div>
					</div>
					</div>
				</nav>
				<div className="PageContent">
					<div className="MaxWidthContainer">
						{this.props.children}
						<ReactTooltip id="ToolTipBottom" place="top" type="dark" effect="float"/>
						<ReactTooltip id="ToolTipTop" place="top" type="dark" effect="float"/>
					</div>
				</div>
			</div>
		);
	}
}

Layout.contextTypes = {
	router: React.PropTypes.object
};

Layout.childContextTypes = {
	actions: React.PropTypes.object,
	state: React.PropTypes.object,
	router: React.PropTypes.object
};




