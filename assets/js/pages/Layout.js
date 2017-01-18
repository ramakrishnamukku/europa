/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import {Link} from 'react-router'
import ReactTooltip from 'react-tooltip'
import ActionBinder from './../util/ActionBinder'
import NPECheck from './../util/NPECheck'

// Actions
import * as GeneralActions from './../actions/GeneralActions'
import * as RepoActions from './../actions/RepoActions'
import * as NotificationActions from './../actions/NotificationActions'
import * as RegistryActions from './../actions/RegistryActions'
import * as SettingsActions from './../actions/SettingsActions'

export default class Layout extends Component {
	constructor(props) {
		super(props);

		// Main State Store
		this.state = {
			...this.getBaseState()
		};
	}
	getBaseState(){
		return {
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
				...RegistryActions.addRegistryState()
			},
			addRepo: {
				...RepoActions.addRepoState()
			},
			repoDetails: {
				...RepoActions.repoDetailsState()
			},
			notif: {
				...NotificationActions.notifState()
			},
			settings: {
				...SettingsActions.settingsState()
			},
			intervals: {
				registriesInterval: null,
				reposInterval: null
			}
		}
	}
	componentDidMount() {
		this.setState({
			intervals: {
				registriesInterval: setInterval(() => {
					RegistryActions.listRegistries.call(this)
				}, 30000),
				reposInterval: setInterval(() => {
					RepoActions.listRepos.call(this)
				}, 30000)
			}
		});
	}
	componentDidUpdate(prevProps, prevState) {
		ReactTooltip.hide();
		ReactTooltip.rebuild();
	}
	componentWillUnmount() {
		clearInterval(this.state.intervals.registriesInterval);
		clearInterval(this.state.intervals.reposInterval);
	}
	getChildContext() {
		let actions = this.getChildContextActions();

		return {
			actions: ActionBinder(actions, this),
			router: this.context.router
		};
	}
	getChildContextActions(){
		return [
			RegistryActions,
      RepoActions,
      NotificationActions,
      SettingsActions
    ];
	}
	highlightNav(sections, root=null) {
		let shouldHighlight = false;
		let location = NPECheck(this.props, 'location/pathname', []);

		if (location) {
			let split = location.split("/");
			if (sections.indexOf(split[1]) != -1) {
				shouldHighlight = true;
			}
		}

		if (shouldHighlight
				|| location.length == 1 && location[0] == "/" && root) {
			return { background: "#25a69c" }
		}
	}
	render() {
		return (
			<div className="PageContainer">
				<nav className="TopNav">
				 <div className="MaxWidthContainer">

					<div className="FlexRow MainNav">
						<Link to="/">
							<img src="/public/images/distelli-europa-logo.svg"
									 alt="Distelli Europa" />
						</Link>
						<Link to="/"
							    className="MainNavLink"
							    style={this.highlightNav(["repository", "new-repository"], true) }>
							<span>Repositories</span>
						</Link>
						<Link to="/pipelines"
							    className="MainNavLink"
							    style={ this.highlightNav(["pipelines"]) }>
							<span>Pipelines</span>
						</Link>
					</div>

					<div className="FlexRow FlexEnd SettingsNav">
						<div className="Flex1">
							<Link to="/settings"
										data-tip="Settings"
										data-for="ToolTipBottom">
								<i className="icon icon-dis-settings"/>
							</Link>
						</div>
					</div>
					</div>
				</nav>
				<div className="PageContent">
					<div className="MaxWidthContainer">
						{React.cloneElement(this.props.children, {...this.state}, null)}
						<ReactTooltip id="ToolTipBottom" place="top" type="dark" effect="float"/>
						<ReactTooltip id="ToolTipTop" place="top" type="dark" effect="float"/>
					</div>
				</div>
			</div>
		);
	}
}

Layout.contextTypes = {
	router: PropTypes.object
};

Layout.childContextTypes = {
	actions: PropTypes.object,
	router: PropTypes.object
};