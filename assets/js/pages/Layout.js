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
import * as PipelineActions from './../actions/PipelineActions'
import * as NotificationActions from './../actions/NotificationActions'
import * as RegistryActions from './../actions/RegistryActions'
import * as SettingsActions from './../actions/SettingsActions'
import * as SSLActions from './../actions/SSLActions'

import Footer from './../components/Footer'

export default class Layout extends Component {
	constructor(props) {
		super(props);
		// Main State Store
		this.state = {
			...this.getBaseState()
		};
	}
	getBaseState() {
		return {
			storage: PAGE_PROPS.storage,
			dnsName: PAGE_PROPS.dnsName,
			registries: [],
			registriesMap: {},
			repos: [],
			reposFilterQuery: null,
			reposMap: {},
			reposNameMap: {},
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
			pipelinesStore: {
				...PipelineActions.pipelinesState()
			},
			pipelineStore: {
				...PipelineActions.singlePipelineState()
			},
			notif: {
				...NotificationActions.notifState()
			},
			settings: {
				...SettingsActions.settingsState()
			},
			ssl: {
				...SSLActions.sslState()
			},
			intervals: {
				registriesInterval: null,
				reposInterval: null
			}
		}
	}
	componentDidMount() {}
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
	getChildContextActions() {
		return [
			  RegistryActions,
		      RepoActions,
		      PipelineActions,
		      NotificationActions,
		      SettingsActions,
		      SSLActions,
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
			return { background: "#00A79D" }
		}
	}
	renderNav(){
		if(NPECheck(this.props, 'location/pathname', '') == '/') {
			return (
				<Link to="/">
					<div className="LandingPageNav">
						<img className="PremiumLogo" src="/public/images/distelli-europa-community-logo.svg"/>
					</div>
				</Link>
			);
		}

		return (
			<nav className="TopNav">
			 	<div className="MaxWidthContainer">
					<div className="FlexRow MainNav">
						<Link to="/repositories">
							<img src="/public/images/distelli-europa-community-logo.svg"
									 alt="Distelli Europa" />
						</Link>
						<Link to="/repositories"
							    className="MainNavLink"
							    style={this.highlightNav(["repository", "new-repository", "repositories", "create-repository"], true) }>
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
								<i className="icon icon-dis-settings" 
								   style={(NPECheck(this.props, 'location/pathname', null) == '/settings' ? {color: '#EA3F67'} : {})}/>
							</Link>
						</div>
					</div>
				</div>
			</nav>
		);
	}
	renderFooter(){	
		return (
			<Footer />
		);
	}
	render() {
		let pageContainerClassName = 'PageContainer';

		if((NPECheck(this.props, 'location/pathname', '') == '/')) pageContainerClassName += ' Dark';

		return (
			<div className="TopLevelContainer">
				<div className={pageContainerClassName}>
					{this.renderNav()}
					<div className="PageContent">
						<div className="MaxWidthContainer">
							{React.cloneElement(this.props.children, {...this.state}, null)}
							<ReactTooltip id="ToolTipBottom" place="top" type="dark" effect="float"/>
							<ReactTooltip id="ToolTipTop" place="top" type="dark" effect="float"/>
						</div>
					</div>
					
				</div>
				{this.renderFooter()}
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